package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.AuthToken;
import ru.rentcrm.app.model.AuthTokenType;

import java.util.Optional;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    Optional<AuthToken> findByTokenHashAndTypeAndUsedAtIsNull(String tokenHash, AuthTokenType type);
}
