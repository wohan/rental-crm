package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Payment;
import ru.rentcrm.app.model.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByAccountIdOrderByDueDateAsc(UUID accountId);

    boolean existsByContractIdAndDueDate(UUID contractId, LocalDate dueDate);

    List<Payment> findByAccountIdAndDueDateBetweenOrderByDueDateAsc(UUID accountId, LocalDate from, LocalDate to);

    List<Payment> findByAccountIdAndStatus(UUID accountId, PaymentStatus status);

    List<Payment> findByAccountIdAndDueDateAndStatusNot(UUID accountId, LocalDate dueDate, PaymentStatus status);
}
