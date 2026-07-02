package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.BillingSettings;

import java.util.Optional;
import java.util.UUID;

public interface BillingSettingsRepository extends JpaRepository<BillingSettings, UUID> {
    Optional<BillingSettings> findByAccountId(UUID accountId);
}
