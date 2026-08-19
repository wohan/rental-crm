package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

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

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String yookassaShopId;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String yookassaSecretKey;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String cloudPaymentsPublicId;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String cloudPaymentsApiSecret;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String robokassaMerchantLogin;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String robokassaPassword1;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
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
