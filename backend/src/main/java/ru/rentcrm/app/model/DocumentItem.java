package ru.rentcrm.app.model;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

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
    @Schema(accessMode=Schema.AccessMode.READ_ONLY)
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    @Schema(accessMode=Schema.AccessMode.READ_ONLY)
    public UUID accountId;

    public UUID objectId;
    public UUID tenantId;
    public UUID contractId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    @NotBlank @Size(max=200)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DocumentType type = DocumentType.OTHER;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    @NotBlank @Size(max=2000)
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
    @Schema(accessMode=Schema.AccessMode.READ_ONLY)
    public Instant createdAt = Instant.now();
}
