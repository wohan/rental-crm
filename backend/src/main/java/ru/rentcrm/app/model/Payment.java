package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment extends AccountEntity {
    @Column(nullable = false)
    public UUID objectId;

    public UUID tenantId;
    public UUID contractId;

    @Column(nullable = false)
    public LocalDate dueDate;

    public LocalDate paidDate;

    @Column(nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false)
    public BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PaymentStatus status = PaymentStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PaymentType type = PaymentType.RENT;

    public String method;

    @Column(columnDefinition = "text")
    public String comment;
}
