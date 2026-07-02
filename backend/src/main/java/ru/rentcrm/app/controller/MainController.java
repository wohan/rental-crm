package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.Account;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.AuditLog;
import ru.rentcrm.app.repository.AccountRepository;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.AuditService;
import ru.rentcrm.app.service.DashboardService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {
    private final CurrentUser current;
    private final AccountRepository accounts;
    private final DashboardService dashboardService;
    private final AuditService auditService;

    public MainController(CurrentUser current, AccountRepository accounts, DashboardService dashboardService, AuditService auditService) {
        this.current = current;
        this.accounts = accounts;
        this.dashboardService = dashboardService;
        this.auditService = auditService;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        AppUser user = current.get();
        Account account = accounts.findById(user.accountId).orElseThrow();
        return Map.of(
            "email", user.email,
            "fullName", user.fullName,
            "role", user.role,
            "emailVerified", user.emailVerified,
            "account", account
        );
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return dashboardService.dashboard(current.accountId());
    }

    @GetMapping("/audit")
    public List<AuditLog> audit() {
        return auditService.latest(current.accountId());
    }
}
