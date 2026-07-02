package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Listing;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
