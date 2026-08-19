package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.rentcrm.app.crypto.EncryptedStringConverter;

@Entity
@Table(name = "tenants")
public class Tenant extends AccountEntity {
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String fullName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    public String phone;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String email;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String passportMasked;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String inn;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String telegramChatId;
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String whatsappPhone;
    public Boolean notificationsEnabled = true;
    public String publicRequestToken;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    public String notes;
}
