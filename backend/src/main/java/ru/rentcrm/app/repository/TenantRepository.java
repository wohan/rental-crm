package ru.rentcrm.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
  List<Tenant> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

  Optional<Tenant> findByPublicRequestToken(String publicRequestToken);
}
