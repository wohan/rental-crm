package ru.rentalcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentalcrm.model.Lease;
import ru.rentalcrm.model.LeaseStatus;

import java.time.LocalDate;
import java.util.List;

public interface LeaseRepository extends JpaRepository<Lease, Long> {
    long countByStatus(LeaseStatus status);
    List<Lease> findTop10ByStatusAndEndDateBetweenOrderByEndDateAsc(LeaseStatus status, LocalDate from, LocalDate to);
}
