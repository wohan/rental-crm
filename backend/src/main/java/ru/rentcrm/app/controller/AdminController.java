package ru.rentcrm.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.Account;
import ru.rentcrm.app.model.BillingInvoice;
import ru.rentcrm.app.model.ErrorEvent;
import ru.rentcrm.app.model.UserRole;
import ru.rentcrm.app.repository.AccountRepository;
import ru.rentcrm.app.repository.BillingInvoiceRepository;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.AuditService;
import ru.rentcrm.app.service.ErrorTrackingService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CurrentUser current;
    private final AccountRepository accounts;
    private final BillingInvoiceRepository invoices;
    private final AuditService auditService;
    private final ErrorTrackingService errorTracking;

    public AdminController(CurrentUser current, AccountRepository accounts, BillingInvoiceRepository invoices, AuditService auditService, ErrorTrackingService errorTracking) {
        this.current = current;
        this.accounts = accounts;
        this.invoices = invoices;
        this.auditService = auditService;
        this.errorTracking = errorTracking;
    }

    @GetMapping("/accounts")
    public List<Account> accounts() {
        requireAdmin();
        return accounts.findAll();
    }

    @GetMapping("/accounts/{accountId}/invoices")
    public List<BillingInvoice> invoices(@PathVariable UUID accountId) {
        requireAdmin();
        return invoices.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @GetMapping("/errors")
    public List<ErrorEvent> errors() {
        requireAdmin();
        return errorTracking.latest();
    }

    @PostMapping("/accounts/{accountId}/block")
    public Account block(@PathVariable UUID accountId, @RequestBody Map<String, String> body) {
        requireAdmin();
        Account account = accounts.findById(accountId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Аккаунт не найден"));
        account.blockedAt = Instant.now();
        account.blockedReason = body.getOrDefault("reason", "Блокировка оператором");
        Account saved = accounts.save(account);
        auditService.record(current.get(), "UPDATE", "ADMIN_ACCOUNT", account.id, "Блокировка аккаунта: " + account.name);
        return saved;
    }

    @PostMapping("/accounts/{accountId}/unblock")
    public Account unblock(@PathVariable UUID accountId) {
        requireAdmin();
        Account account = accounts.findById(accountId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Аккаунт не найден"));
        account.blockedAt = null;
        account.blockedReason = null;
        Account saved = accounts.save(account);
        auditService.record(current.get(), "UPDATE", "ADMIN_ACCOUNT", account.id, "Разблокировка аккаунта: " + account.name);
        return saved;
    }

    private void requireAdmin() {
        if (current.get().role != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Доступно только администратору SaaS");
        }
    }
}
