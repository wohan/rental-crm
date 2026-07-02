package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.dto.CheckoutRequest;
import ru.rentcrm.app.model.Account;
import ru.rentcrm.app.model.BillingInvoice;
import ru.rentcrm.app.model.BillingSettings;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.AuditService;
import ru.rentcrm.app.service.BillingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
public class BillingController {
    private final CurrentUser current;
    private final BillingService billing;
    private final AuditService audit;

    public BillingController(CurrentUser current, BillingService billing, AuditService audit) {
        this.current = current;
        this.billing = billing;
        this.audit = audit;
    }

    @GetMapping("/settings")
    public BillingSettings settings() {
        return billing.getOrCreateSettings(current.accountId());
    }

    @PutMapping("/settings")
    public BillingSettings saveSettings(@RequestBody BillingSettings input) {
        BillingSettings saved = billing.saveSettings(current.accountId(), input);
        audit.record(current.get(), "UPDATE", "BILLING_SETTINGS", saved.id, String.valueOf(saved.provider));
        return saved;
    }

    @GetMapping("/invoices")
    public List<BillingInvoice> invoices() {
        return billing.invoices(current.accountId());
    }

    @PostMapping("/checkout")
    public BillingInvoice checkout(@RequestBody CheckoutRequest request) {
        BillingInvoice invoice = billing.createCheckout(current.accountId(), request);
        audit.record(current.get(), "CREATE", "BILLING_INVOICE", invoice.id, invoice.tariff.name());
        return invoice;
    }

    @PostMapping("/invoices/{id}/mark-paid")
    public Account markPaid(@PathVariable UUID id) {
        Account account = billing.applyPaidInvoice(current.accountId(), id);
        audit.record(current.get(), "MARK_PAID", "BILLING_INVOICE", id, account.tariff.name());
        return account;
    }
}
