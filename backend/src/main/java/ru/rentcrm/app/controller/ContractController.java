package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.LeaseContract;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.ContractService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    private final CurrentUser current;
    private final ContractService service;

    public ContractController(CurrentUser current, ContractService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<LeaseContract> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public LeaseContract create(@RequestBody LeaseContract item) {
        return service.create(current.get(), item);
    }

    @PutMapping("/{id}")
    public LeaseContract update(@PathVariable UUID id, @RequestBody LeaseContract item) {
        return service.update(current.get(), id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
