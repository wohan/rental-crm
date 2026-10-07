package dev.textagent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.*;
import java.util.*;

/** Bounded single-process store. A session lock serializes edits, confirmation and cancellation. */
public final class AgentEngine {
    public record View(String sessionId, String state, String reply, List<String> errors, String confirmationId,
                       String expiresAt, String operationId, String method, String path, JsonNode arguments, Integer httpStatus) {}
    private static final class Session {
        final String id = UUID.randomUUID().toString(), owner;
        final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
        volatile Instant touched; Instant expires; JsonNode draft; Operation operation; String confirmation; View view;
        Session(String owner, Instant now) { this.owner = owner; this.touched = now; }
    }
    private final Map<String, Session> sessions = new HashMap<>();
    private final OpenApiCatalog catalog; private final Planner planner; private final Executor executor;
    private final Clock clock; private final Duration confirmationTtl, sessionTtl; private final int maxSessions;
    public AgentEngine(OpenApiCatalog catalog, Planner planner, Executor executor, Clock clock,
                       Duration confirmationTtl, Duration sessionTtl, int maxSessions) {
        if (confirmationTtl.isNegative() || confirmationTtl.isZero() || sessionTtl.compareTo(confirmationTtl) < 0 || maxSessions < 1)
            throw new IllegalArgumentException("Invalid session limits");
        this.catalog = catalog; this.planner = planner; this.executor = executor; this.clock = clock;
        this.confirmationTtl = confirmationTtl; this.sessionTtl = sessionTtl; this.maxSessions = maxSessions;
    }
    public View message(String owner, String sessionId, String message) {
        if (message == null || message.isBlank() || message.length() > 8000) throw new AgentException(400, "Введите сообщение длиной до 8000 символов");
        Session s = session(owner, sessionId, true);
        s.lock.lock();
        try {
            if (s.view != null && Set.of("DONE", "FAILED", "UNKNOWN", "CANCELLED", "EXPIRED").contains(s.view.state())) { s.draft = null; s.operation = null; }
            s.touched = clock.instant(); s.confirmation = null; // Every edit invalidates the old confirmation, even if planning fails.
            s.view = view(s, "COLLECTING_FIELDS", "Уточняю запрос", List.of(), null);
            JsonNode previous = s.draft == null ? JsonNodeFactory.instance.objectNode() : s.draft.deepCopy();
            Planner.Proposal proposal = planner.plan(catalog.operations(), previous, message);
            if (proposal == null) throw new AgentException(502, "Модель не вернула предложение");
            if (proposal.operationId() == null || proposal.operationId().isBlank()) {
                s.operation = null; s.draft = null;
                return s.view = view(s, "COLLECTING_FIELDS", safeQuestion(proposal.question()), List.of(), null);
            }
            Operation op = catalog.require(proposal.operationId());
            JsonNode args = proposal.arguments();
            if (args == null || !args.isObject() || args.toString().length() > 32000) throw new AgentException(502, "Некорректный ответ модели");
            s.operation = op;
            s.draft = JsonNodeFactory.instance.objectNode().put("operationId", op.id()).set("arguments", args.deepCopy());
            List<String> errors = Schema.validate(op.schema(), args);
            if (!errors.isEmpty()) return s.view = view(s, "COLLECTING_FIELDS", "Уточните данные для операции «" + op.description() + "»", errors, null);
            s.confirmation = UUID.randomUUID().toString(); s.expires = clock.instant().plus(confirmationTtl);
            return s.view = view(s, "READY_FOR_CONFIRMATION", "Подтвердите: " + op.description(), List.of(), null);
        } finally { s.lock.unlock(); }
    }
    public View confirm(String owner, String sessionId, String confirmationId, String authorization) {
        Session s = session(owner, sessionId, false);
        s.lock.lock();
        try {
            if (confirmationId == null || !confirmationId.equals(s.confirmation)) throw new AgentException(409, "Подтверждение устарело");
            if (Set.of("DONE", "FAILED", "UNKNOWN").contains(s.view.state())) return copy(s.view);
            if (!clock.instant().isBefore(s.expires)) { s.confirmation = null; throw new AgentException(409, "Срок подтверждения истёк. Отправьте запрос заново"); }
            if (!s.view.state().equals("READY_FOR_CONFIRMATION")) throw new AgentException(409, "Действие не готово");
            List<String> errors = Schema.validate(s.operation.schema(), s.draft.get("arguments"));
            if (!errors.isEmpty()) throw new AgentException(422, "Данные не прошли проверку");
            s.touched = clock.instant(); s.view = view(s, "EXECUTING", "Выполняется", List.of(), null);
            try {
                Executor.Result result = executor.execute(s.operation, s.draft.get("arguments").deepCopy(), authorization, confirmationId);
                String state = result.status() >= 200 && result.status() < 300 ? "DONE" : result.status() >= 500 ? "UNKNOWN" : "FAILED";
                s.view = view(s, state, result.message(), List.of(), result.status());
            } catch (RuntimeException ex) {
                s.view = view(s, "UNKNOWN", "Ответ не получен. Проверьте результат в приложении перед повторной операцией", List.of(), null);
            }
            return copy(s.view);
        } finally { s.lock.unlock(); }
    }
    public View cancel(String owner, String sessionId) {
        Session s = session(owner, sessionId, false);
        s.lock.lock();
        try {
            if (s.view != null && Set.of("DONE", "FAILED", "UNKNOWN").contains(s.view.state())) throw new AgentException(409, "Выполненную попытку нельзя отменить");
            s.confirmation = null; s.draft = null; s.operation = null; s.touched = clock.instant();
            return s.view = view(s, "CANCELLED", "Действие отменено", List.of(), null);
        } finally { s.lock.unlock(); }
    }
    public View status(String owner, String sessionId) {
        Session s = session(owner, sessionId, false);
        s.lock.lock();
        try {
            if (s.view != null && s.view.state().equals("READY_FOR_CONFIRMATION") && !clock.instant().isBefore(s.expires)) {
                s.confirmation = null; s.view = view(s, "EXPIRED", "Срок подтверждения истёк", List.of(), null);
            }
            return copy(s.view);
        } finally { s.lock.unlock(); }
    }
    private synchronized Session session(String owner, String id, boolean create) {
        if (owner == null || owner.isBlank()) throw new AgentException(401, "Требуется авторизация");
        // Avoid blocking unrelated conversations while a model or target request is in flight.
        sessions.values().removeIf(s -> {
            if (!s.lock.tryLock()) return false;
            try { return s.touched.plus(sessionTtl).isBefore(clock.instant()); }
            finally { s.lock.unlock(); }
        });
        if (id == null && create) {
            if (sessions.size() >= maxSessions) throw new AgentException(429, "Лимит сессий. Попробуйте позже");
            Session s = new Session(owner, clock.instant()); sessions.put(s.id, s); return s;
        }
        Session s = sessions.get(id);
        if (s == null || !s.owner.equals(owner)) throw new AgentException(404, "Сессия не найдена");
        s.touched = clock.instant();
        return s;
    }
    private static String safeQuestion(String q) { return q == null || q.isBlank() ? "Какое действие нужно выполнить?" : q.substring(0, Math.min(q.length(), 2000)); }
    private View view(Session s, String state, String reply, List<String> errors, Integer code) {
        return new View(s.id, state, reply, errors, s.confirmation, s.confirmation == null ? null : s.expires.toString(),
            s.operation == null ? null : s.operation.id(), s.operation == null ? null : s.operation.method(),
            s.operation == null ? null : s.operation.path(), s.draft == null ? null : s.draft.get("arguments").deepCopy(), code);
    }
    private static View copy(View v) { return v == null ? null : new View(v.sessionId(), v.state(), v.reply(), v.errors(), v.confirmationId(), v.expiresAt(), v.operationId(), v.method(), v.path(), v.arguments() == null ? null : v.arguments().deepCopy(), v.httpStatus()); }
}
