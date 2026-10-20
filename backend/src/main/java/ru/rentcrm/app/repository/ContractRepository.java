package ru.rentcrm.app.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.LeaseContract;

public interface ContractRepository extends JpaRepository<LeaseContract, UUID> {
  List<LeaseContract> findByAccountIdOrderByEndDateAsc(UUID accountId);

  long countByAccountId(UUID accountId);

  List<LeaseContract> findByAccountIdAndEndDateBetween(
      UUID accountId, LocalDate from, LocalDate to);

  List<LeaseContract> findByTenantIdOrderByEndDateDesc(UUID tenantId);
}
