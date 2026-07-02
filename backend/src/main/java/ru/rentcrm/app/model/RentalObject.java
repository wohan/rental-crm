package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "rental_objects")
public class RentalObject extends AccountEntity {
    @Column(nullable = false)
    public String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ObjectType type;

    @Column(nullable = false)
    public String address;

    public BigDecimal areaSqm;
    public String cadastralNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ObjectStatus status = ObjectStatus.VACANT;

    @Column(nullable = false)
    public BigDecimal monthlyRent = BigDecimal.ZERO;

    @Column(nullable = false)
    public BigDecimal monthlyUtilityAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    public BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    public String notes;
}
