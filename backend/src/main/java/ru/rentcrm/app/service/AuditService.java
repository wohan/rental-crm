package ru.rentcrm.app.service;

import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.AuditLog;
import ru.rentcrm.app.repository.AuditLogRepository;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(AppUser actor, String action, String entityType, UUID entityId, String summary) {
        AuditLog log = new AuditLog();
        log.accountId = actor.accountId;
        log.userId = actor.id;
        log.actorEmail = actor.email;
        log.action = action;
        log.entityType = entityType;
        log.entityId = entityId;
        log.summary = summary;
        repository.save(log);
    }

    public List<AuditLog> latest(UUID accountId) {
        return repository.findTop100ByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
