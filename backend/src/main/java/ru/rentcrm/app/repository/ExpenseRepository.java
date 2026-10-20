package ru.rentcrm.app.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
  List<Expense> findByAccountIdOrderByExpenseDateDesc(UUID accountId);

  Optional<Expense> findByAccountIdAndObjectIdAndSourceAndPeriodMonth(
      UUID accountId, UUID objectId, String source, LocalDate periodMonth);
}
