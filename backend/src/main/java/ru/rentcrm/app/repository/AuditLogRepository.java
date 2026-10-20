package ru.rentcrm.app.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
  List<AuditLog> findTop100ByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
