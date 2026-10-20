package ru.rentcrm.app.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Listing;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
  List<Listing> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
