package ru.rentcrm.app.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.AuthToken;
import ru.rentcrm.app.model.AuthTokenType;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
  Optional<AuthToken> findByTokenHashAndTypeAndUsedAtIsNull(String tokenHash, AuthTokenType type);
}
