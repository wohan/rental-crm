package ru.rentcrm.app.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class AccountEntity {
  @Id
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  public UUID id = UUID.randomUUID();

  @Column(nullable = false)
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  public UUID accountId;

  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  public Instant createdAt = Instant.now();

  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  public Instant updatedAt = Instant.now();

  @PreUpdate
  public void touch() {
    updatedAt = Instant.now();
  }
}
