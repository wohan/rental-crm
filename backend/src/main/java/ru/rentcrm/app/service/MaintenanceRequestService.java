package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.MaintenanceRequest;
import ru.rentcrm.app.model.MaintenanceStatus;
import ru.rentcrm.app.repository.MaintenanceRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceRequestService {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceRequestService.class);

    private final MaintenanceRepository maintenance;
    private final OwnershipService ownership;
    private final AuditService audit;

    public MaintenanceRequestService(MaintenanceRepository maintenance, OwnershipService ownership, AuditService audit) {
        this.maintenance = maintenance;
        this.ownership = ownership;
        this.audit = audit;
    }

    public List<MaintenanceRequest> list(UUID accountId) {
        log.debug("Listing maintenance requests started accountId={}", accountId);
        List<MaintenanceRequest> result = maintenance.findByAccountIdOrderByCreatedAtDesc(accountId);
        log.debug("Listing maintenance requests finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public MaintenanceRequest create(AppUser actor, MaintenanceRequest input) {
        log.debug("Creating maintenance request started accountId={}", actor.accountId);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        MaintenanceRequest saved = maintenance.save(input);
        audit.record(actor, "CREATE", "MAINTENANCE", saved.id, saved.title);
        log.debug("Creating maintenance request finished accountId={} requestId={}", actor.accountId, saved.id);
        return saved;
    }

    public MaintenanceRequest update(AppUser actor, UUID id, MaintenanceRequest input) {
        log.debug("Updating maintenance request started accountId={} requestId={}", actor.accountId, id);
        MaintenanceRequest item = ownership.requireOwned(maintenance.findById(id), actor.accountId);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        if (input.status == MaintenanceStatus.DONE && input.closedAt == null) {
            input.closedAt = Instant.now();
        }
        MaintenanceRequest saved = maintenance.save(input);
        audit.record(actor, "UPDATE", "MAINTENANCE", saved.id, saved.title);
        log.debug("Updating maintenance request finished accountId={} requestId={}", actor.accountId, saved.id);
        return saved;
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting maintenance request started accountId={} requestId={}", actor.accountId, id);
        MaintenanceRequest item = ownership.requireOwned(maintenance.findById(id), actor.accountId);
        maintenance.delete(item);
        audit.record(actor, "DELETE", "MAINTENANCE", id, item.title);
        log.debug("Deleting maintenance request finished accountId={} requestId={}", actor.accountId, id);
    }

    public List<MaintenanceRequest> open(UUID accountId) {
        return maintenance.findByAccountIdAndStatusIn(accountId, List.of(MaintenanceStatus.NEW, MaintenanceStatus.IN_PROGRESS, MaintenanceStatus.WAITING_TENANT));
    }
}
