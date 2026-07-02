package ru.rentcrm.app.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.AuthToken;
import ru.rentcrm.app.model.AuthTokenType;
import ru.rentcrm.app.repository.AuthTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthTokenService {
    private final AuthTokenRepository repository;

    public AuthTokenService(AuthTokenRepository repository) {
        this.repository = repository;
    }

    public String issue(AppUser user, AuthTokenType type, Duration ttl) {
        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        AuthToken authToken = new AuthToken();
        authToken.userId = user.id;
        authToken.type = type;
        authToken.tokenHash = hash(token);
        authToken.expiresAt = Instant.now().plus(ttl);
        repository.save(authToken);
        return token;
    }

    public AuthToken consume(String token, AuthTokenType type) {
        AuthToken authToken = repository.findByTokenHashAndTypeAndUsedAtIsNull(hash(token), type)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Ссылка недействительна или уже использована"));
        if (authToken.expiresAt.isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Срок действия ссылки истек");
        }
        authToken.usedAt = Instant.now();
        return repository.save(authToken);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash auth token", ex);
        }
    }
}
