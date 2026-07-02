package ru.rentcrm.app.dto;

public record BillingWebhookRequest(
    String provider,
    String event,
    String providerPaymentId,
    String invoiceId,
    String status
) {
}
