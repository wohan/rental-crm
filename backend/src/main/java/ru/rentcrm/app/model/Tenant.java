package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenants")
public class Tenant extends AccountEntity {
    @Column(nullable = false)
    public String fullName;

    @Column(nullable = false)
    public String phone;

    public String email;
    public String passportMasked;
    public String inn;
    public String telegramChatId;
    public String whatsappPhone;
    public Boolean notificationsEnabled = true;
    public String publicRequestToken;

    @Column(columnDefinition = "text")
    public String notes;
}
