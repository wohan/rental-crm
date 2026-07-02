package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentItem {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID accountId;

    public UUID objectId;
    public UUID tenantId;
    public UUID contractId;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DocumentType type = DocumentType.OTHER;

    @Column(nullable = false)
    public String fileUrl;

    public String storageKey;
    public String originalFileName;
    public String contentType;
    public Long sizeBytes;
    public String scanStatus = "NOT_SCANNED";

    public LocalDate expiresAt;
    public Instant createdAt = Instant.now();
}
