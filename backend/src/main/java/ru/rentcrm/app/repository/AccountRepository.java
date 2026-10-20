package ru.rentcrm.app.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {}
