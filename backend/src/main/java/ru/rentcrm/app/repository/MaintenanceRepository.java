package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.MaintenanceRequest;
import ru.rentcrm.app.model.MaintenanceStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<MaintenanceRequest, UUID> {
    List<MaintenanceRequest> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<MaintenanceRequest> findByAccountIdAndStatusIn(UUID accountId, Collection<MaintenanceStatus> statuses);
}
