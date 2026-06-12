package ru.rentalcrm.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.rentalcrm.model.MaintenanceRequest;
import ru.rentalcrm.repository.MaintenanceRequestRepository;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceRequestRepository repository;

    public MaintenanceController(MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MaintenanceRequest> list() {
        return repository.findAll();
    }

    @PostMapping
    public MaintenanceRequest create(@Valid @RequestBody MaintenanceRequest request) {
        request.setId(null);
        return repository.save(request);
    }
}
