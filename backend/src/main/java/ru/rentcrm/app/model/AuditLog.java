package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID accountId;

    public UUID userId;

    @Column(nullable = false)
    public String action;

    @Column(nullable = false)
    public String entityType;

    public UUID entityId;

    @Column(nullable = false)
    public String actorEmail;

    @Column(columnDefinition = "text")
    public String summary;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();
}
