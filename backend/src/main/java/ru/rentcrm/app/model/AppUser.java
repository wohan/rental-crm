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
@Table(name = "users")
public class AppUser {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID accountId;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserRole role = UserRole.OWNER;

    @Column(nullable = false)
    public Boolean enabled = true;

    @Column(nullable = false)
    public Boolean emailVerified = false;

    public Instant emailVerifiedAt;
    public Instant termsAcceptedAt;
    public Instant privacyAcceptedAt;
    public Instant notificationConsentAt;
    public Instant lastPasswordChangeAt;

    public Instant createdAt = Instant.now();
}
