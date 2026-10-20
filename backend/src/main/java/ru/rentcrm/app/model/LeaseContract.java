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
@Table(name = "lease_contracts")
public class LeaseContract extends AccountEntity {
  @Column(nullable = false)
  @NotNull
  public UUID objectId;

  @Column(nullable = false)
  @NotNull
  public UUID tenantId;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  public String number;

  @Column(nullable = false)
  @NotNull
  public LocalDate startDate;

  @Column(nullable = false)
  @NotNull
  public LocalDate endDate;

  @Column(nullable = false)
  @PositiveOrZero
  public BigDecimal rentAmount = BigDecimal.ZERO;

  @Column(nullable = false)
  @Min(1)
  @Max(31)
  public Integer paymentDay = 1;

  @Column(nullable = false)
  @PositiveOrZero
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
