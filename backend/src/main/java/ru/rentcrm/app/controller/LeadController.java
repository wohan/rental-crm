package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.Lead;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.LeadService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final CurrentUser current;
    private final LeadService service;

    public LeadController(CurrentUser current, LeadService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<Lead> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public Lead create(@RequestBody Lead item) {
        return service.create(current.get(), item);
    }

    @PutMapping("/{id}")
    public Lead update(@PathVariable UUID id, @RequestBody Lead item) {
        return service.update(current.get(), id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
