package ru.rentcrm.app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.rentcrm.app.model.AppUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService {
    private final SecretKey key;

    public JwtService(@Value("${app.jwt-secret}") String secret) {
        byte[] bytes = secret.repeat(4)
            .substring(0, Math.max(32, secret.length()))
            .getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String issue(AppUser user) {
        return Jwts.builder()
            .subject(user.email)
            .claim("accountId", user.accountId.toString())
            .claim("role", user.role.name())
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(Duration.ofDays(14))))
            .signWith(key)
            .compact();
    }

    public Optional<String> subject(String token) {
        try {
            return Optional.ofNullable(Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
