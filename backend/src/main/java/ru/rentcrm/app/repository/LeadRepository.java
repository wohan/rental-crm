package ru.rentcrm.app.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Lead;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
  List<Lead> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
