package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.NotificationDelivery;
import ru.rentcrm.app.model.NotificationSettings;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.AuditService;
import ru.rentcrm.app.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final CurrentUser current;
    private final NotificationService notifications;
    private final AuditService audit;

    public NotificationController(CurrentUser current, NotificationService notifications, AuditService audit) {
        this.current = current;
        this.notifications = notifications;
        this.audit = audit;
    }

    @GetMapping("/settings")
    public NotificationSettings settings() {
        return notifications.getOrCreateSettings(current.accountId());
    }

    @PutMapping("/settings")
    public NotificationSettings saveSettings(@RequestBody NotificationSettings input) {
        NotificationSettings saved = notifications.saveSettings(current.accountId(), input);
        audit.record(current.get(), "UPDATE", "NOTIFICATION_SETTINGS", saved.id, "Настройки уведомлений");
        return saved;
    }

    @GetMapping("/deliveries")
    public List<NotificationDelivery> deliveries() {
        return notifications.deliveries(current.accountId());
    }

    @PostMapping("/send-due")
    public List<NotificationDelivery> sendDue() {
        List<NotificationDelivery> deliveries = notifications.sendDueRemindersForAccount(current.accountId());
        audit.record(current.get(), "SEND", "NOTIFICATION", null, "Ручная отправка уведомлений: " + deliveries.size());
        return deliveries;
    }
}
