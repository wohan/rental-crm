package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.Payment;
import ru.rentcrm.app.model.PaymentStatus;
import ru.rentcrm.app.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final OwnershipService ownership;
    private final AuditService audit;

    public PaymentService(PaymentRepository payments, OwnershipService ownership, AuditService audit) {
        this.payments = payments;
        this.ownership = ownership;
        this.audit = audit;
    }

    public List<Payment> list(UUID accountId) {
        log.debug("Listing payments started accountId={}", accountId);
        List<Payment> result = payments.findByAccountIdOrderByDueDateAsc(accountId);
        log.debug("Listing payments finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public Payment create(AppUser actor, Payment input) {
        log.debug("Creating payment started accountId={}", actor.accountId);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        Payment saved = payments.save(normalize(input));
        audit.record(actor, "CREATE", "PAYMENT", saved.id, String.valueOf(saved.amount));
        log.debug("Creating payment finished accountId={} paymentId={}", actor.accountId, saved.id);
        return saved;
    }

    public Payment update(AppUser actor, UUID id, Payment input) {
        log.debug("Updating payment started accountId={} paymentId={}", actor.accountId, id);
        Payment item = require(actor.accountId, id);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        Payment saved = payments.save(normalize(input));
        audit.record(actor, "UPDATE", "PAYMENT", saved.id, String.valueOf(saved.amount));
        log.debug("Updating payment finished accountId={} paymentId={}", actor.accountId, saved.id);
        return saved;
    }

    public Payment markPaid(AppUser actor, UUID id) {
        log.info("Mark payment paid started accountId={} paymentId={}", actor.accountId, id);
        Payment item = require(actor.accountId, id);
        item.status = PaymentStatus.PAID;
        item.paidAmount = item.amount;
        item.paidDate = LocalDate.now();
        Payment saved = payments.save(item);
        audit.record(actor, "MARK_PAID", "PAYMENT", saved.id, String.valueOf(saved.amount));
        log.info("Mark payment paid finished accountId={} paymentId={} amount={}", actor.accountId, saved.id, saved.amount);
        return saved;
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting payment started accountId={} paymentId={}", actor.accountId, id);
        Payment item = require(actor.accountId, id);
        payments.delete(item);
        audit.record(actor, "DELETE", "PAYMENT", id, String.valueOf(item.amount));
        log.debug("Deleting payment finished accountId={} paymentId={}", actor.accountId, id);
    }

    public Payment saveScheduled(Payment payment) {
        return payments.save(normalize(payment));
    }

    public boolean existsByContractAndDueDate(UUID contractId, LocalDate dueDate) {
        return payments.existsByContractIdAndDueDate(contractId, dueDate);
    }

    public List<Payment> all(UUID accountId) {
        return payments.findByAccountIdOrderByDueDateAsc(accountId);
    }

    public List<Payment> overdue(UUID accountId) {
        return payments.findByAccountIdAndStatus(accountId, PaymentStatus.OVERDUE);
    }

    public List<Payment> upcoming(UUID accountId, LocalDate from, LocalDate to) {
        return payments.findByAccountIdAndDueDateBetweenOrderByDueDateAsc(accountId, from, to);
    }

    private Payment require(UUID accountId, UUID id) {
        return ownership.requireOwned(payments.findById(id), accountId);
    }

    private Payment normalize(Payment payment) {
        if (payment.status != PaymentStatus.PAID && payment.dueDate != null && payment.dueDate.isBefore(LocalDate.now())) {
            payment.status = PaymentStatus.OVERDUE;
        }
        if (payment.paidAmount == null) {
            payment.paidAmount = BigDecimal.ZERO;
        }
        return payment;
    }
}
