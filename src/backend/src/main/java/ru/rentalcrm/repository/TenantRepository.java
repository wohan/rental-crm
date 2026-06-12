package ru.rentalcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentalcrm.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
