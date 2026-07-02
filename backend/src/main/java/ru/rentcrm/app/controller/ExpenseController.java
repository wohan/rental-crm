package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.Expense;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.ExpenseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final CurrentUser current;
    private final ExpenseService service;

    public ExpenseController(CurrentUser current, ExpenseService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<Expense> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public Expense create(@RequestBody Expense item) {
        return service.create(current.get(), item);
    }

    @PutMapping("/{id}")
    public Expense update(@PathVariable UUID id, @RequestBody Expense item) {
        return service.update(current.get(), id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
