package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

@Entity
@Table(name = "rental_objects")
public class RentalObject extends AccountEntity {
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ObjectType type;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String address;

    public BigDecimal areaSqm;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
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

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String notes;
}
