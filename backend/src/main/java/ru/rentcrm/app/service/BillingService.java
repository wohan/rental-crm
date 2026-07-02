package ru.rentcrm.app.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rentcrm.app.dto.CheckoutRequest;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.Account;
import ru.rentcrm.app.model.BillingInvoice;
import ru.rentcrm.app.model.BillingInvoiceStatus;
import ru.rentcrm.app.model.BillingProvider;
import ru.rentcrm.app.model.BillingSettings;
import ru.rentcrm.app.model.Tariff;
import ru.rentcrm.app.repository.AccountRepository;
import ru.rentcrm.app.repository.BillingInvoiceRepository;
import ru.rentcrm.app.repository.BillingSettingsRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

@Service
public class BillingService {
    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final BillingSettingsRepository settingsRepository;
    private final BillingInvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public BillingService(
        BillingSettingsRepository settingsRepository,
        BillingInvoiceRepository invoiceRepository,
        AccountRepository accountRepository
    ) {
        this.settingsRepository = settingsRepository;
        this.invoiceRepository = invoiceRepository;
        this.accountRepository = accountRepository;
    }

    public BillingSettings getOrCreateSettings(UUID accountId) {
        return settingsRepository.findByAccountId(accountId).orElseGet(() -> {
            BillingSettings settings = new BillingSettings();
            settings.accountId = accountId;
            return settingsRepository.save(settings);
        });
    }

    public BillingSettings saveSettings(UUID accountId, BillingSettings input) {
        log.debug("Saving billing settings started accountId={}", accountId);
        BillingSettings settings = getOrCreateSettings(accountId);
        settings.provider = input.provider == null ? BillingProvider.YOOKASSA : input.provider;
        settings.yookassaShopId = blankToNull(input.yookassaShopId);
        settings.yookassaSecretKey = blankToNull(input.yookassaSecretKey);
        settings.cloudPaymentsPublicId = blankToNull(input.cloudPaymentsPublicId);
        settings.cloudPaymentsApiSecret = blankToNull(input.cloudPaymentsApiSecret);
        settings.robokassaMerchantLogin = blankToNull(input.robokassaMerchantLogin);
        settings.robokassaPassword1 = blankToNull(input.robokassaPassword1);
        settings.genericPaymentUrl = blankToNull(input.genericPaymentUrl);
        settings.testMode = input.testMode == null || input.testMode;
        BillingSettings saved = settingsRepository.save(settings);
        log.debug("Saving billing settings finished accountId={} provider={}", accountId, saved.provider);
        return saved;
    }

    public List<BillingInvoice> invoices(UUID accountId) {
        return invoiceRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public BillingInvoice createCheckout(UUID accountId, CheckoutRequest request) {
        log.info("Creating billing checkout started accountId={} tariff={}", accountId, request.tariff());
        Tariff tariff = request.tariff() == null || request.tariff() == Tariff.FREE ? Tariff.START : request.tariff();
        BillingSettings settings = getOrCreateSettings(accountId);
        BillingInvoice invoice = new BillingInvoice();
        invoice.accountId = accountId;
        invoice.tariff = tariff;
        invoice.amount = amount(tariff);
        invoice.objectsLimit = objectsLimit(tariff);
        invoice.provider = settings.provider;
        invoice.status = BillingInvoiceStatus.PENDING;

        if (settings.provider == BillingProvider.YOOKASSA) {
            BillingInvoice saved = createYookassaPayment(settings, invoice);
            log.info("Creating billing checkout finished accountId={} invoiceId={} status={}", accountId, saved.id, saved.status);
            return saved;
        }
        if (settings.provider == BillingProvider.GENERIC_PAYMENT_LINK && settings.genericPaymentUrl != null) {
            invoice.confirmationUrl = settings.genericPaymentUrl;
            invoice.providerResponse = "Generic payment URL";
            BillingInvoice saved = invoiceRepository.save(invoice);
            log.info("Creating billing checkout finished accountId={} invoiceId={} status={}", accountId, saved.id, saved.status);
            return saved;
        }
        throw new ApiException(NOT_IMPLEMENTED, "Для выбранного провайдера пока настроен только справочник. Рабочий checkout реализован для ЮKassa и generic payment link.");
    }

    public Account applyPaidInvoice(UUID accountId, UUID invoiceId) {
        log.info("Applying paid invoice started accountId={} invoiceId={}", accountId, invoiceId);
        BillingInvoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ApiException(BAD_REQUEST, "Счет не найден"));
        if (!invoice.accountId.equals(accountId)) {
            throw new ApiException(BAD_REQUEST, "Счет не найден");
        }
        invoice.status = BillingInvoiceStatus.PAID;
        invoiceRepository.save(invoice);

        Account account = applySubscription(accountRepository.findById(accountId).orElseThrow(), invoice);
        log.info("Applying paid invoice finished accountId={} invoiceId={} paidUntil={}", accountId, invoiceId, account.subscriptionPaidUntil);
        return account;
    }

    public Account applyProviderPayment(String invoiceId, String providerPaymentId) {
        log.info("Applying provider payment started invoiceId={} providerPaymentId={}", invoiceId, providerPaymentId);
        BillingInvoice invoice = null;
        if (invoiceId != null && !invoiceId.isBlank()) {
            invoice = invoiceRepository.findById(UUID.fromString(invoiceId)).orElse(null);
        }
        if (invoice == null && providerPaymentId != null && !providerPaymentId.isBlank()) {
            invoice = invoiceRepository.findByProviderPaymentId(providerPaymentId).orElse(null);
        }
        if (invoice == null) {
            throw new ApiException(BAD_REQUEST, "Счет для webhook не найден");
        }
        invoice.status = BillingInvoiceStatus.PAID;
        invoiceRepository.save(invoice);
        Account account = applySubscription(accountRepository.findById(invoice.accountId).orElseThrow(), invoice);
        log.info("Applying provider payment finished accountId={} invoiceId={} paidUntil={}", account.id, invoice.id, account.subscriptionPaidUntil);
        return account;
    }

    private BillingInvoice createYookassaPayment(BillingSettings settings, BillingInvoice invoice) {
        require(settings.yookassaShopId, "Не указан shopId ЮKassa");
        require(settings.yookassaSecretKey, "Не указан secretKey ЮKassa");

        String body = """
            {
              "amount": {"value": "%s", "currency": "RUB"},
              "capture": true,
              "description": "Подписка RentCRM: %s",
              "confirmation": {"type": "redirect", "return_url": "https://example.ru/billing/success"}
            }
            """.formatted(invoice.amount.toPlainString(), invoice.tariff.name());

        String auth = Base64.getEncoder().encodeToString((settings.yookassaShopId + ":" + settings.yookassaSecretKey).getBytes(StandardCharsets.UTF_8));
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("https://api.yookassa.ru/v3/payments"))
            .header("Authorization", "Basic " + auth)
            .header("Content-Type", "application/json")
            .header("Idempotence-Key", invoice.id.toString())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            invoice.providerResponse = response.statusCode() + ": " + response.body();
            invoice.confirmationUrl = extract(response.body(), "\"confirmation_url\":\"", "\"");
            invoice.providerPaymentId = extract(response.body(), "\"id\":\"", "\"");
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                invoice.status = BillingInvoiceStatus.FAILED;
            }
            return invoiceRepository.save(invoice);
        } catch (IOException ex) {
            invoice.status = BillingInvoiceStatus.FAILED;
            invoice.providerResponse = ex.getMessage();
            return invoiceRepository.save(invoice);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            invoice.status = BillingInvoiceStatus.FAILED;
            invoice.providerResponse = ex.getMessage();
            return invoiceRepository.save(invoice);
        }
    }

    private BigDecimal amount(Tariff tariff) {
        return switch (tariff) {
            case FREE -> BigDecimal.ZERO;
            case START -> BigDecimal.valueOf(300);
            case PRO, BUSINESS -> BigDecimal.valueOf(1000);
        };
    }

    private int objectsLimit(Tariff tariff) {
        return switch (tariff) {
            case FREE -> 3;
            case START -> 20;
            case PRO, BUSINESS -> 100;
        };
    }

    private Account applySubscription(Account account, BillingInvoice invoice) {
        Instant now = Instant.now();
        Instant base = account.subscriptionPaidUntil != null && account.subscriptionPaidUntil.isAfter(now)
            ? account.subscriptionPaidUntil
            : now;
        account.tariff = invoice.tariff;
        account.objectsLimit = invoice.objectsLimit;
        account.subscriptionPaidUntil = base.plus(30, ChronoUnit.DAYS);
        account.subscriptionGraceUntil = account.subscriptionPaidUntil.plus(7, ChronoUnit.DAYS);
        account.blockedAt = null;
        account.blockedReason = null;
        return accountRepository.save(account);
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(BAD_REQUEST, message);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String extract(String body, String prefix, String suffix) {
        int start = body.indexOf(prefix);
        if (start < 0) {
            return null;
        }
        int valueStart = start + prefix.length();
        int end = body.indexOf(suffix, valueStart);
        return end < 0 ? null : body.substring(valueStart, end).replace("\\/", "/");
    }
}
