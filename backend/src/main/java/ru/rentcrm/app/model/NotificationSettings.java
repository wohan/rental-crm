package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_settings")
public class NotificationSettings {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    public UUID accountId;

    @Column(nullable = false)
    public Boolean enabled = true;

    @Column(nullable = false)
    public Integer remindDaysBefore = 1;

    @Column(nullable = false)
    public String reminderTime = "10:00";

    @Column(nullable = false)
    public Boolean telegramEnabled = true;

    public String telegramBotToken;
    public String telegramDefaultChatId;

    @Column(nullable = false)
    public Boolean smsRuEnabled = false;

    public String smsRuApiId;
    public String smsRuSender;

    @Column(nullable = false)
    public Boolean smsRuTestMode = true;

    @Column(nullable = false)
    public Boolean whatsappEnabled = false;

    public String whatsappApiUrl;
    public String whatsappToken;
    public String whatsappDefaultRecipient;

    @Column(nullable = false, columnDefinition = "text")
    public String messageTemplate = "Здравствуйте! Напоминаем: платеж по аренде {object} на сумму {amount} ₽ должен быть внесен {dueDate}.";

    public Instant createdAt = Instant.now();
    public Instant updatedAt = Instant.now();

    @PreUpdate
    public void touch() {
        updatedAt = Instant.now();
    }
}
