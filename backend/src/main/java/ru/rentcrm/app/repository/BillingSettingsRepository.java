package ru.rentcrm.app.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.BillingSettings;

public interface BillingSettingsRepository extends JpaRepository<BillingSettings, UUID> {
  Optional<BillingSettings> findByAccountId(UUID accountId);
}
