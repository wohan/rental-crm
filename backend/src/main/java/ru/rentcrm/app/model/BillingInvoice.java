package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "billing_invoices")
public class BillingInvoice {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Tariff tariff;

    @Column(nullable = false)
    public BigDecimal amount;

    @Column(nullable = false)
    public Integer objectsLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BillingProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BillingInvoiceStatus status = BillingInvoiceStatus.CREATED;

    public String providerPaymentId;
    public String confirmationUrl;

    @Column(columnDefinition = "text")
    public String providerResponse;

    public Instant createdAt = Instant.now();
}
