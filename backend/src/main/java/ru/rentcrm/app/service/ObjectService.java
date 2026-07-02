package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.Account;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.ObjectStatus;
import ru.rentcrm.app.model.RentalObject;
import ru.rentcrm.app.repository.AccountRepository;
import ru.rentcrm.app.repository.ObjectRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ObjectService {
    private static final Logger log = LoggerFactory.getLogger(ObjectService.class);

    private final ObjectRepository objects;
    private final AccountRepository accounts;
    private final OwnershipService ownership;
    private final AuditService audit;

    public ObjectService(ObjectRepository objects, AccountRepository accounts, OwnershipService ownership, AuditService audit) {
        this.objects = objects;
        this.accounts = accounts;
        this.ownership = ownership;
        this.audit = audit;
    }

    public List<RentalObject> list(UUID accountId) {
        log.debug("Listing rental objects started accountId={}", accountId);
        List<RentalObject> result = objects.findByAccountIdOrderByCreatedAtDesc(accountId);
        log.debug("Listing rental objects finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public RentalObject create(AppUser actor, RentalObject input) {
        UUID accountId = actor.accountId;
        log.debug("Creating rental object started accountId={}", accountId);
        Account account = accounts.findById(accountId).orElseThrow();
        if (objects.countByAccountId(accountId) >= account.objectsLimit) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Достигнут лимит объектов тарифа");
        }
        input.id = UUID.randomUUID();
        input.accountId = accountId;
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        normalizeMoney(input);
        RentalObject saved = objects.save(input);
        audit.record(actor, "CREATE", "OBJECT", saved.id, saved.title);
        log.debug("Creating rental object finished accountId={} objectId={}", accountId, saved.id);
        return saved;
    }

    public RentalObject update(AppUser actor, UUID id, RentalObject input) {
        log.debug("Updating rental object started accountId={} objectId={}", actor.accountId, id);
        RentalObject item = require(actor.accountId, id);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        input.updatedAt = Instant.now();
        normalizeMoney(input);
        RentalObject saved = objects.save(input);
        audit.record(actor, "UPDATE", "OBJECT", saved.id, saved.title);
        log.debug("Updating rental object finished accountId={} objectId={}", actor.accountId, saved.id);
        return saved;
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting rental object started accountId={} objectId={}", actor.accountId, id);
        RentalObject item = require(actor.accountId, id);
        objects.delete(item);
        audit.record(actor, "DELETE", "OBJECT", id, item.title);
        log.debug("Deleting rental object finished accountId={} objectId={}", actor.accountId, id);
    }

    public RentalObject markOccupied(UUID accountId, UUID objectId) {
        RentalObject object = require(accountId, objectId);
        object.status = ObjectStatus.OCCUPIED;
        return objects.save(object);
    }

    public RentalObject require(UUID accountId, UUID objectId) {
        return ownership.requireOwned(objects.findById(objectId), accountId);
    }

    private void normalizeMoney(RentalObject object) {
        if (object.monthlyRent == null) {
            object.monthlyRent = BigDecimal.ZERO;
        }
        if (object.monthlyUtilityAmount == null) {
            object.monthlyUtilityAmount = BigDecimal.ZERO;
        }
        if (object.depositAmount == null) {
            object.depositAmount = BigDecimal.ZERO;
        }
    }
}

