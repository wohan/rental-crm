package ru.rentcrm.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.dto.PublicMaintenanceRequest;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.LeaseContract;
import ru.rentcrm.app.model.MaintenanceRequest;
import ru.rentcrm.app.model.MaintenanceStatus;
import ru.rentcrm.app.model.Priority;
import ru.rentcrm.app.model.Tenant;
import ru.rentcrm.app.repository.ContractRepository;
import ru.rentcrm.app.repository.MaintenanceRepository;
import ru.rentcrm.app.repository.TenantRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/maintenance")
public class PublicMaintenanceController {
    private final TenantRepository tenants;
    private final ContractRepository contracts;
    private final MaintenanceRepository maintenance;

    public PublicMaintenanceController(TenantRepository tenants, ContractRepository contracts, MaintenanceRepository maintenance) {
        this.tenants = tenants;
        this.contracts = contracts;
        this.maintenance = maintenance;
    }

    @GetMapping("/{token}")
    public Map<String, Object> context(@PathVariable String token) {
        Tenant tenant = tenant(token);
        return Map.of(
            "ok", true,
            "tenant", tenant.fullName
        );
    }

    @PostMapping("/{token}")
    public Map<String, Object> create(@PathVariable String token, @Valid @RequestBody PublicMaintenanceRequest request) {
        Tenant tenant = tenant(token);
        LeaseContract contract = contracts.findByTenantIdOrderByEndDateDesc(tenant.id).stream()
            .max(Comparator.comparing(item -> item.endDate))
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Для арендатора не найден договор с объектом"));

        MaintenanceRequest item = new MaintenanceRequest();
        item.id = UUID.randomUUID();
        item.accountId = tenant.accountId;
        item.tenantId = tenant.id;
        item.objectId = contract.objectId;
        item.title = request.title();
        item.priority = Priority.MEDIUM;
        item.status = MaintenanceStatus.NEW;
        item.cost = BigDecimal.ZERO;
        item.description = """
            Заявка из публичной формы.
            Контакт: %s
            Телефон: %s

            %s
            """.formatted(nullToBlank(request.contactName()), nullToBlank(request.contactPhone()), nullToBlank(request.description()));
        item.createdAt = Instant.now();
        item.updatedAt = Instant.now();
        MaintenanceRequest saved = maintenance.save(item);
        return Map.of("ok", true, "id", saved.id, "message", "Заявка принята");
    }

    private Tenant tenant(String token) {
        return tenants.findByPublicRequestToken(token)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Публичная форма не найдена"));
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
