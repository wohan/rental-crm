package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AuthTokenType type;

    @Column(nullable = false, unique = true)
    public String tokenHash;

    @Column(nullable = false)
    public Instant expiresAt;

    public Instant usedAt;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();
}
