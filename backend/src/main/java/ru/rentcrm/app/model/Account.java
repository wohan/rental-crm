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
@Table(name = "accounts")
public class Account {
  @Id public UUID id = UUID.randomUUID();

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  public String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Tariff tariff = Tariff.FREE;

  @Column(nullable = false)
  public Integer objectsLimit = 3;

  public Instant createdAt = Instant.now();
  public Instant updatedAt = Instant.now();
  public Instant subscriptionPaidUntil;
  public Instant subscriptionGraceUntil;
  public Instant blockedAt;
  public String blockedReason;

  @PreUpdate
  public void touch() {
    updatedAt = Instant.now();
  }
}
