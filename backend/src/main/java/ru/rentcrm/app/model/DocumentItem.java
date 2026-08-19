package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

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

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DocumentType type = DocumentType.OTHER;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String fileUrl;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String storageKey;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String originalFileName;
    public String contentType;
    public Long sizeBytes;
    public String scanStatus = "NOT_SCANNED";

    public LocalDate expiresAt;
    public Instant createdAt = Instant.now();
}
