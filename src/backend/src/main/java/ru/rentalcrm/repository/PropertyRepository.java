package ru.rentalcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentalcrm.model.PropertyStatus;
import ru.rentalcrm.model.RentalProperty;

public interface PropertyRepository extends JpaRepository<RentalProperty, Long> {
    long countByStatus(PropertyStatus status);
}
