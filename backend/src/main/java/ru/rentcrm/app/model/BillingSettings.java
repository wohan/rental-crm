package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "billing_settings")
public class BillingSettings {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BillingProvider provider = BillingProvider.YOOKASSA;

    public String yookassaShopId;
    public String yookassaSecretKey;
    public String cloudPaymentsPublicId;
    public String cloudPaymentsApiSecret;
    public String robokassaMerchantLogin;
    public String robokassaPassword1;
    public String genericPaymentUrl;

    @Column(nullable = false)
    public Boolean testMode = true;

    public Instant createdAt = Instant.now();
    public Instant updatedAt = Instant.now();

    @PreUpdate
    public void touch() {
        updatedAt = Instant.now();
    }
}
