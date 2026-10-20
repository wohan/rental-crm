package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listings")
public class Listing extends AccountEntity {
  @Column(nullable = false)
  @NotNull
  public UUID objectId;

  @Column(nullable = false)
  @NotBlank
  @Size(max = 200)
  public String title;

  @Column(nullable = false, columnDefinition = "text")
  @NotBlank
  @Size(max = 10000)
  public String description;

  @Column(nullable = false)
  @PositiveOrZero
  public BigDecimal price = BigDecimal.ZERO;

  public String publicUrl;
  public Boolean published = false;
  public Instant publishedAt;
}
