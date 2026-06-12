package ru.rentalcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.rentalcrm.model.Payment;
import ru.rentalcrm.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findTop10ByStatusInOrderByDueDateAsc(List<PaymentStatus> statuses);
    List<Payment> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate dueDate);
    long countByStatus(PaymentStatus status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = 'PAID'")
    BigDecimal sumPaid();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status in ('PLANNED','PENDING','OVERDUE')")
    BigDecimal sumReceivable();
}
