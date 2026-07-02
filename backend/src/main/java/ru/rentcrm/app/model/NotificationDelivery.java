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
@Table(name = "notification_deliveries")
public class NotificationDelivery {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID accountId;

    @Column(nullable = false)
    public UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public NotificationChannel channel;

    @Column(nullable = false)
    public Integer remindDaysBefore;

    public String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public NotificationDeliveryStatus status;

    @Column(columnDefinition = "text")
    public String response;

    public Instant createdAt = Instant.now();
}
