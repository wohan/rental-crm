package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

@Entity
@Table(name = "lease_contracts")
public class LeaseContract extends AccountEntity {
    @Column(nullable = false)
    public UUID objectId;

    @Column(nullable = false)
    public UUID tenantId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String number;

    @Column(nullable = false)
    public LocalDate startDate;

    @Column(nullable = false)
    public LocalDate endDate;

    @Column(nullable = false)
    public BigDecimal rentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    public Integer paymentDay = 1;

    @Column(nullable = false)
    public BigDecimal depositAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ContractStatus status = ContractStatus.ACTIVE;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String documentUrl;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String notes;
}
