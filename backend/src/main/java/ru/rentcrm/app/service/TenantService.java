package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.Tenant;
import ru.rentcrm.app.repository.TenantRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TenantService {
    private static final Logger log = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenants;
    private final OwnershipService ownership;
    private final AuditService audit;

    public TenantService(TenantRepository tenants, OwnershipService ownership, AuditService audit) {
        this.tenants = tenants;
        this.ownership = ownership;
        this.audit = audit;
    }

    public List<Tenant> list(UUID accountId) {
        log.debug("Listing tenants started accountId={}", accountId);
        List<Tenant> result = tenants.findByAccountIdOrderByCreatedAtDesc(accountId);
        log.debug("Listing tenants finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public Tenant create(AppUser actor, Tenant input) {
        log.debug("Creating tenant started accountId={}", actor.accountId);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.publicRequestToken = UUID.randomUUID().toString().replace("-", "");
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        Tenant saved = tenants.save(input);
        audit.record(actor, "CREATE", "TENANT", saved.id, saved.fullName);
        log.debug("Creating tenant finished accountId={} tenantId={}", actor.accountId, saved.id);
        return saved;
    }

    public Tenant update(AppUser actor, UUID id, Tenant input) {
        log.debug("Updating tenant started accountId={} tenantId={}", actor.accountId, id);
        Tenant item = require(actor.accountId, id);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        input.publicRequestToken = item.publicRequestToken == null ? UUID.randomUUID().toString().replace("-", "") : item.publicRequestToken;
        Tenant saved = tenants.save(input);
        audit.record(actor, "UPDATE", "TENANT", saved.id, saved.fullName);
        log.debug("Updating tenant finished accountId={} tenantId={}", actor.accountId, saved.id);
        return saved;
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting tenant started accountId={} tenantId={}", actor.accountId, id);
        Tenant item = require(actor.accountId, id);
        tenants.delete(item);
        audit.record(actor, "DELETE", "TENANT", id, item.fullName);
        log.debug("Deleting tenant finished accountId={} tenantId={}", actor.accountId, id);
    }

    public Tenant require(UUID accountId, UUID id) {
        return ownership.requireOwned(tenants.findById(id), accountId);
    }
}
