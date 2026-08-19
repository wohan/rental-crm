package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

import java.util.UUID;

@Entity
@Table(name = "leads")
public class Lead extends AccountEntity {
    public UUID listingId;
    public UUID objectId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String fullName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String phone;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String email;
    public String source;
    public String status = "NEW";

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String comment;
}
