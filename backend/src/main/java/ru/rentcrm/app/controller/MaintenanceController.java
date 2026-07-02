package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.MaintenanceRequest;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.MaintenanceRequestService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final CurrentUser current;
    private final MaintenanceRequestService service;

    public MaintenanceController(CurrentUser current, MaintenanceRequestService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<MaintenanceRequest> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public MaintenanceRequest create(@RequestBody MaintenanceRequest item) {
        return service.create(current.get(), item);
    }

    @PutMapping("/{id}")
    public MaintenanceRequest update(@PathVariable UUID id, @RequestBody MaintenanceRequest item) {
        return service.update(current.get(), id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
