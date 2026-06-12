package ru.rentalcrm.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.rentalcrm.model.Lease;
import ru.rentalcrm.repository.LeaseRepository;

import java.util.List;

@RestController
@RequestMapping("/api/leases")
public class LeaseController {
    private final LeaseRepository repository;

    public LeaseController(LeaseRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Lease> list() {
        return repository.findAll();
    }

    @PostMapping
    public Lease create(@Valid @RequestBody Lease lease) {
        lease.setId(null);
        return repository.save(lease);
    }
}
