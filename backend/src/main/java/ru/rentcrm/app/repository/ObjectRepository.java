package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.ObjectStatus;
import ru.rentcrm.app.model.RentalObject;

import java.util.List;
import java.util.UUID;

public interface ObjectRepository extends JpaRepository<RentalObject, UUID> {
    List<RentalObject> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    long countByAccountId(UUID accountId);

    long countByAccountIdAndStatus(UUID accountId, ObjectStatus status);
}
