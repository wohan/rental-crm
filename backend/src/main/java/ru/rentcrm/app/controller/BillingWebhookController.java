package ru.rentcrm.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.dto.BillingWebhookRequest;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.Account;
import ru.rentcrm.app.service.BillingService;

import java.util.Map;

@RestController
@RequestMapping("/api/billing/webhooks")
public class BillingWebhookController {
    private final BillingService billingService;
    private final String webhookSecret;

    public BillingWebhookController(BillingService billingService, @Value("${app.billing.webhook-secret:}") String webhookSecret) {
        this.billingService = billingService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/payment")
    public Map<String, Object> payment(
        @RequestHeader(name = "X-RentCRM-Webhook-Secret", required = false) String secret,
        @RequestBody BillingWebhookRequest request
    ) {
        if (webhookSecret != null && !webhookSecret.isBlank() && !webhookSecret.equals(secret)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Неверная подпись webhook");
        }
        Account account = billingService.applyProviderPayment(request.invoiceId(), request.providerPaymentId());
        return Map.of(
            "ok", true,
            "accountId", account.id,
            "tariff", account.tariff,
            "paidUntil", account.subscriptionPaidUntil
        );
    }
}
