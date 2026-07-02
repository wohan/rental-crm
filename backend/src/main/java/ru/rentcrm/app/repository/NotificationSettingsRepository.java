package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.NotificationSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, UUID> {
    Optional<NotificationSettings> findByAccountId(UUID accountId);

    List<NotificationSettings> findByEnabledTrue();
}
