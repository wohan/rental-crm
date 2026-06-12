package ru.rentalcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentalcrm.model.MaintenanceRequest;
import ru.rentalcrm.model.MaintenanceStatus;

import java.util.List;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
    long countByStatusIn(List<MaintenanceStatus> statuses);
    List<MaintenanceRequest> findTop10ByStatusInOrderByDueDateAscCreatedAtAsc(List<MaintenanceStatus> statuses);
}
