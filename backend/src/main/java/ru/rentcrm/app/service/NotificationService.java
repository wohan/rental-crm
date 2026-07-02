package ru.rentcrm.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.DocumentItem;
import ru.rentcrm.app.model.NotificationChannel;
import ru.rentcrm.app.model.NotificationDelivery;
import ru.rentcrm.app.model.NotificationDeliveryStatus;
import ru.rentcrm.app.model.NotificationSettings;
import ru.rentcrm.app.model.Payment;
import ru.rentcrm.app.model.PaymentStatus;
import ru.rentcrm.app.model.RentalObject;
import ru.rentcrm.app.model.Tenant;
import ru.rentcrm.app.repository.NotificationDeliveryRepository;
import ru.rentcrm.app.repository.NotificationSettingsRepository;
import ru.rentcrm.app.repository.ObjectRepository;
import ru.rentcrm.app.repository.PaymentRepository;
import ru.rentcrm.app.repository.TenantRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationSettingsRepository settingsRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final TenantRepository tenantRepository;
    private final ObjectRepository objectRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public NotificationService(
        NotificationSettingsRepository settingsRepository,
        NotificationDeliveryRepository deliveryRepository,
        PaymentRepository paymentRepository,
        TenantRepository tenantRepository,
        ObjectRepository objectRepository
    ) {
        this.settingsRepository = settingsRepository;
        this.deliveryRepository = deliveryRepository;
        this.paymentRepository = paymentRepository;
        this.tenantRepository = tenantRepository;
        this.objectRepository = objectRepository;
    }

    public NotificationSettings getOrCreateSettings(UUID accountId) {
        return settingsRepository.findByAccountId(accountId).orElseGet(() -> {
            NotificationSettings settings = new NotificationSettings();
            settings.accountId = accountId;
            return settingsRepository.save(settings);
        });
    }

    public NotificationSettings saveSettings(UUID accountId, NotificationSettings input) {
        log.debug("Saving notification settings started accountId={}", accountId);
        NotificationSettings settings = getOrCreateSettings(accountId);
        settings.enabled = value(input.enabled, true);
        settings.remindDaysBefore = Math.max(0, value(input.remindDaysBefore, 1));
        settings.reminderTime = normalizeTime(input.reminderTime);
        settings.telegramEnabled = value(input.telegramEnabled, false);
        settings.telegramBotToken = blankToNull(input.telegramBotToken);
        settings.telegramDefaultChatId = blankToNull(input.telegramDefaultChatId);
        settings.smsRuEnabled = value(input.smsRuEnabled, false);
        settings.smsRuApiId = blankToNull(input.smsRuApiId);
        settings.smsRuSender = blankToNull(input.smsRuSender);
        settings.smsRuTestMode = value(input.smsRuTestMode, true);
        settings.whatsappEnabled = value(input.whatsappEnabled, false);
        settings.whatsappApiUrl = blankToNull(input.whatsappApiUrl);
        settings.whatsappToken = blankToNull(input.whatsappToken);
        settings.whatsappDefaultRecipient = blankToNull(input.whatsappDefaultRecipient);
        settings.messageTemplate = blankToDefault(input.messageTemplate, settings.messageTemplate);
        NotificationSettings saved = settingsRepository.save(settings);
        log.debug("Saving notification settings finished accountId={} settingsId={}", accountId, saved.id);
        return saved;
    }

    public List<NotificationDelivery> deliveries(UUID accountId) {
        return deliveryRepository.findAll().stream()
            .filter(item -> item.accountId.equals(accountId))
            .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
            .limit(100)
            .toList();
    }

    public List<NotificationDelivery> sendDueRemindersForAccount(UUID accountId) {
        log.info("Manual due notification sending started accountId={}", accountId);
        NotificationSettings settings = getOrCreateSettings(accountId);
        List<NotificationDelivery> deliveries = sendDueReminders(settings, true);
        log.info("Manual due notification sending finished accountId={} deliveries={}", accountId, deliveries.size());
        return deliveries;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void sendScheduledReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        for (NotificationSettings settings : settingsRepository.findByEnabledTrue()) {
            if (isWithinCurrentHour(settings.reminderTime, now)) {
                sendDueReminders(settings, false);
            }
        }
    }

    private List<NotificationDelivery> sendDueReminders(NotificationSettings settings, boolean force) {
        if (!Boolean.TRUE.equals(settings.enabled)) {
            log.debug("Due notification sending skipped because disabled accountId={}", settings.accountId);
            return List.of();
        }
        LocalDate dueDate = LocalDate.now().plusDays(settings.remindDaysBefore);
        log.debug("Due notification sending started accountId={} dueDate={} force={}", settings.accountId, dueDate, force);
        List<NotificationDelivery> deliveries = paymentRepository.findByAccountIdAndDueDateAndStatusNot(settings.accountId, dueDate, PaymentStatus.PAID)
            .stream()
            .flatMap(payment -> sendPaymentReminder(settings, payment, force).stream())
            .toList();
        log.debug("Due notification sending finished accountId={} deliveries={}", settings.accountId, deliveries.size());
        return deliveries;
    }

    private List<NotificationDelivery> sendPaymentReminder(NotificationSettings settings, Payment payment, boolean force) {
        Optional<Tenant> tenant = payment.tenantId == null ? Optional.empty() : tenantRepository.findById(payment.tenantId);
        if (tenant.isPresent() && Boolean.FALSE.equals(tenant.get().notificationsEnabled)) {
            return List.of(log(payment, NotificationChannel.TELEGRAM, null, NotificationDeliveryStatus.SKIPPED, "У арендатора отключены уведомления", settings.remindDaysBefore));
        }
        String text = renderMessage(settings, payment);
        return List.of(
                channel(settings.telegramEnabled, NotificationChannel.TELEGRAM),
                channel(settings.smsRuEnabled, NotificationChannel.SMS_RU),
                channel(settings.whatsappEnabled, NotificationChannel.WHATSAPP)
            ).stream()
            .filter(channel -> channel != null)
            .filter(channel -> force || !deliveryRepository.existsByPaymentIdAndChannelAndRemindDaysBefore(payment.id, channel, settings.remindDaysBefore))
            .map(channel -> sendChannel(settings, payment, tenant.orElse(null), channel, text))
            .toList();
    }

    private NotificationDelivery sendChannel(NotificationSettings settings, Payment payment, Tenant tenant, NotificationChannel channel, String text) {
        try {
            log.debug("Sending payment reminder started accountId={} paymentId={} channel={}", payment.accountId, payment.id, channel);
            deliveryRepository.findExisting(payment.id, channel, settings.remindDaysBefore)
                .ifPresent(deliveryRepository::delete);
            NotificationDelivery delivery = switch (channel) {
                case TELEGRAM -> sendTelegram(settings, payment, tenant, text);
                case SMS_RU -> sendSmsRu(settings, payment, tenant, text);
                case WHATSAPP -> sendWhatsapp(settings, payment, tenant, text);
            };
            log.debug("Sending payment reminder finished accountId={} paymentId={} channel={} status={}", payment.accountId, payment.id, channel, delivery.status);
            return delivery;
        } catch (RuntimeException ex) {
            log.info("Sending payment reminder failed accountId={} paymentId={} channel={} reason={}", payment.accountId, payment.id, channel, ex.getMessage());
            return log(payment, channel, null, NotificationDeliveryStatus.FAILED, ex.getMessage(), settings.remindDaysBefore);
        }
    }

    private NotificationDelivery sendTelegram(NotificationSettings settings, Payment payment, Tenant tenant, String text) {
        require(settings.telegramBotToken, "Не указан Telegram bot token");
        String recipient = firstNonBlank(tenant == null ? null : tenant.telegramChatId, settings.telegramDefaultChatId);
        require(recipient, "Не указан Telegram chat_id арендатора или default chat_id");

        String body = "chat_id=" + encode(recipient) + "&text=" + encode(text);
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.telegram.org/bot" + settings.telegramBotToken + "/sendMessage"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return execute(payment, NotificationChannel.TELEGRAM, recipient, request, settings.remindDaysBefore);
    }

    private NotificationDelivery sendSmsRu(NotificationSettings settings, Payment payment, Tenant tenant, String text) {
        require(settings.smsRuApiId, "Не указан SMS.RU api_id");
        String recipient = tenant == null ? null : tenant.phone;
        require(recipient, "У платежа нет арендатора с телефоном");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://sms.ru/sms/send")
            .queryParam("api_id", settings.smsRuApiId)
            .queryParam("to", recipient)
            .queryParam("msg", text)
            .queryParam("json", "1");
        if (settings.smsRuSender != null) {
            builder.queryParam("from", settings.smsRuSender);
        }
        if (Boolean.TRUE.equals(settings.smsRuTestMode)) {
            builder.queryParam("test", "1");
        }
        HttpRequest request = HttpRequest.newBuilder(builder.build(true).toUri()).GET().build();
        return execute(payment, NotificationChannel.SMS_RU, recipient, request, settings.remindDaysBefore);
    }

    private NotificationDelivery sendWhatsapp(NotificationSettings settings, Payment payment, Tenant tenant, String text) {
        require(settings.whatsappApiUrl, "Не указан WhatsApp webhook URL");
        String recipient = firstNonBlank(tenant == null ? null : tenant.whatsappPhone, tenant == null ? null : tenant.phone, settings.whatsappDefaultRecipient);
        require(recipient, "Не указан WhatsApp получатель");

        String json = "{\"phone\":\"" + json(recipient) + "\",\"message\":\"" + json(text) + "\"}";
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(settings.whatsappApiUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));
        if (settings.whatsappToken != null) {
            builder.header("Authorization", "Bearer " + settings.whatsappToken);
        }
        return execute(payment, NotificationChannel.WHATSAPP, recipient, builder.build(), settings.remindDaysBefore);
    }

    private NotificationDelivery execute(Payment payment, NotificationChannel channel, String recipient, HttpRequest request, Integer remindDaysBefore) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            NotificationDeliveryStatus status = response.statusCode() >= 200 && response.statusCode() < 300
                ? NotificationDeliveryStatus.SENT
                : NotificationDeliveryStatus.FAILED;
            return log(payment, channel, recipient, status, response.statusCode() + ": " + response.body(), remindDaysBefore);
        } catch (IOException ex) {
            return log(payment, channel, recipient, NotificationDeliveryStatus.FAILED, ex.getMessage(), remindDaysBefore);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return log(payment, channel, recipient, NotificationDeliveryStatus.FAILED, ex.getMessage(), remindDaysBefore);
        }
    }

    private NotificationDelivery log(Payment payment, NotificationChannel channel, String recipient, NotificationDeliveryStatus status, String response, Integer remindDaysBefore) {
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.accountId = payment.accountId;
        delivery.paymentId = payment.id;
        delivery.channel = channel;
        delivery.remindDaysBefore = remindDaysBefore;
        delivery.recipient = recipient;
        delivery.status = status;
        delivery.response = response;
        return deliveryRepository.save(delivery);
    }

    private String renderMessage(NotificationSettings settings, Payment payment) {
        String objectTitle = objectRepository.findById(payment.objectId).map(item -> item.title).orElse("объекту");
        return settings.messageTemplate
            .replace("{object}", objectTitle)
            .replace("{amount}", amount(payment.amount))
            .replace("{dueDate}", String.valueOf(payment.dueDate))
            .replace("{comment}", payment.comment == null ? "" : payment.comment);
    }

    private boolean isWithinCurrentHour(String reminderTime, LocalTime now) {
        LocalTime time = LocalTime.parse(normalizeTime(reminderTime));
        return time.getHour() == now.getHour();
    }

    private String normalizeTime(String value) {
        try {
            return LocalTime.parse(blankToDefault(value, "10:00")).withSecond(0).withNano(0).toString();
        } catch (RuntimeException ex) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Время уведомления должно быть в формате HH:mm");
        }
    }

    private String amount(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private NotificationChannel channel(Boolean enabled, NotificationChannel channel) {
        return Boolean.TRUE.equals(enabled) ? channel : null;
    }

    private <T> T value(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
