package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

@Entity
@Table(name = "payments")
public class Payment extends AccountEntity {
  @Column(nullable = false)
  @NotNull
  public UUID objectId;

  public UUID tenantId;
  public UUID contractId;

  @Column(nullable = false)
  @NotNull
  public LocalDate dueDate;

  public LocalDate paidDate;

  @Column(nullable = false)
  @PositiveOrZero
  public BigDecimal amount = BigDecimal.ZERO;

  @Column(nullable = false)
  @PositiveOrZero
  public BigDecimal paidAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public PaymentStatus status = PaymentStatus.PLANNED;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public PaymentType type = PaymentType.RENT;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(columnDefinition = "text")
  public String method;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(columnDefinition = "text")
  public String comment;
}
