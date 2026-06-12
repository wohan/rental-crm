package ru.rentalcrm.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.rentalcrm.model.Tenant;
import ru.rentalcrm.repository.TenantRepository;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantRepository repository;

    public TenantController(TenantRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Tenant> list() {
        return repository.findAll();
    }

    @PostMapping
    public Tenant create(@Valid @RequestBody Tenant tenant) {
        tenant.setId(null);
        return repository.save(tenant);
    }

    @GetMapping("/{id}")
    public Tenant get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + id));
    }
}
