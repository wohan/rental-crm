package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listings")
public class Listing extends AccountEntity {
    @Column(nullable = false)
    public UUID objectId;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false, columnDefinition = "text")
    public String description;

    @Column(nullable = false)
    public BigDecimal price = BigDecimal.ZERO;

    public String publicUrl;
    public Boolean published = false;
    public Instant publishedAt;
}
