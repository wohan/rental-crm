package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.Payment;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.PaymentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final CurrentUser current;
    private final PaymentService service;

    public PaymentController(CurrentUser current, PaymentService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<Payment> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public Payment create(@RequestBody Payment item) {
        return service.create(current.get(), item);
    }

    @PutMapping("/{id}")
    public Payment update(@PathVariable UUID id, @RequestBody Payment item) {
        return service.update(current.get(), id, item);
    }

    @PostMapping("/{id}/mark-paid")
    public Payment markPaid(@PathVariable UUID id) {
        return service.markPaid(current.get(), id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
