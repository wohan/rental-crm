package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

@Entity
@Table(name = "expenses")
public class Expense extends AccountEntity {
    @Column(nullable = false)
    public UUID objectId;

    @Column(nullable = false)
    public LocalDate expenseDate;

    @Column(nullable = false)
    public String category;

    @Column(nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String vendor;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String documentUrl;

    @Column(nullable = false)
    public String source = "MANUAL";

    public LocalDate periodMonth;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String comment;
}
