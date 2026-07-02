package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Tenant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    List<Tenant> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    Optional<Tenant> findByPublicRequestToken(String publicRequestToken);
}
