package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.Lead;
import ru.rentcrm.app.repository.LeadRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LeadService {
    private static final Logger log = LoggerFactory.getLogger(LeadService.class);

    private final LeadRepository leads;
    private final OwnershipService ownership;
    private final AuditService audit;

    public LeadService(LeadRepository leads, OwnershipService ownership, AuditService audit) {
        this.leads = leads;
        this.ownership = ownership;
        this.audit = audit;
    }

    public List<Lead> list(UUID accountId) {
        log.debug("Listing leads started accountId={}", accountId);
        List<Lead> result = leads.findByAccountIdOrderByCreatedAtDesc(accountId);
        log.debug("Listing leads finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public Lead create(AppUser actor, Lead input) {
        log.debug("Creating lead started accountId={}", actor.accountId);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        Lead saved = leads.save(input);
        audit.record(actor, "CREATE", "LEAD", saved.id, saved.fullName);
        log.debug("Creating lead finished accountId={} leadId={}", actor.accountId, saved.id);
        return saved;
    }

    public Lead update(AppUser actor, UUID id, Lead input) {
        log.debug("Updating lead started accountId={} leadId={}", actor.accountId, id);
        Lead item = ownership.requireOwned(leads.findById(id), actor.accountId);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        input.updatedAt = Instant.now();
        Lead saved = leads.save(input);
        audit.record(actor, "UPDATE", "LEAD", saved.id, saved.fullName);
        log.debug("Updating lead finished accountId={} leadId={}", actor.accountId, saved.id);
        return saved;
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting lead started accountId={} leadId={}", actor.accountId, id);
        Lead item = ownership.requireOwned(leads.findById(id), actor.accountId);
        leads.delete(item);
        audit.record(actor, "DELETE", "LEAD", id, item.fullName);
        log.debug("Deleting lead finished accountId={} leadId={}", actor.accountId, id);
    }
}
