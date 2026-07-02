package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.Listing;
import ru.rentcrm.app.repository.ListingRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ListingService {
    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listings;
    private final OwnershipService ownership;
    private final AuditService audit;

    public ListingService(ListingRepository listings, OwnershipService ownership, AuditService audit) {
        this.listings = listings;
        this.ownership = ownership;
        this.audit = audit;
    }

    public List<Listing> list(UUID accountId) {
        log.debug("Listing rental listings started accountId={}", accountId);
        List<Listing> result = listings.findByAccountIdOrderByCreatedAtDesc(accountId);
        log.debug("Listing rental listings finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public Listing create(AppUser actor, Listing input) {
        log.debug("Creating listing started accountId={}", actor.accountId);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        if (Boolean.TRUE.equals(input.published) && input.publishedAt == null) {
            input.publishedAt = Instant.now();
        }
        Listing saved = listings.save(input);
        audit.record(actor, "CREATE", "LISTING", saved.id, saved.title);
        log.debug("Creating listing finished accountId={} listingId={}", actor.accountId, saved.id);
        return saved;
    }

    public Listing update(AppUser actor, UUID id, Listing input) {
        log.debug("Updating listing started accountId={} listingId={}", actor.accountId, id);
        Listing item = ownership.requireOwned(listings.findById(id), actor.accountId);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        input.updatedAt = Instant.now();
        if (Boolean.TRUE.equals(input.published) && input.publishedAt == null) {
            input.publishedAt = item.publishedAt == null ? Instant.now() : item.publishedAt;
        }
        if (!Boolean.TRUE.equals(input.published)) {
            input.publishedAt = null;
        }
        Listing saved = listings.save(input);
        audit.record(actor, "UPDATE", "LISTING", saved.id, saved.title);
        log.debug("Updating listing finished accountId={} listingId={}", actor.accountId, saved.id);
        return saved;
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting listing started accountId={} listingId={}", actor.accountId, id);
        Listing item = ownership.requireOwned(listings.findById(id), actor.accountId);
        listings.delete(item);
        audit.record(actor, "DELETE", "LISTING", id, item.title);
        log.debug("Deleting listing finished accountId={} listingId={}", actor.accountId, id);
    }
}
