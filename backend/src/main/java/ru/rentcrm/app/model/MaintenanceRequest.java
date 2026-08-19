package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

@Entity
@Table(name = "maintenance_requests")
public class MaintenanceRequest extends AccountEntity {
    @Column(nullable = false)
    public UUID objectId;

    public UUID tenantId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String title;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MaintenanceStatus status = MaintenanceStatus.NEW;

    @Column(nullable = false)
    public BigDecimal cost = BigDecimal.ZERO;

    public LocalDate dueDate;
    public Instant closedAt;
}
