package ru.rentalcrm.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.rentalcrm.dto.PaymentUpdateRequest;
import ru.rentalcrm.model.Payment;
import ru.rentalcrm.repository.PaymentRepository;
import ru.rentalcrm.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository repository;
    private final PaymentService paymentService;

    public PaymentController(PaymentRepository repository, PaymentService paymentService) {
        this.repository = repository;
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Payment> list() {
        return repository.findAll();
    }

    @PatchMapping("/{id}/paid")
    public Payment markPaid(@PathVariable Long id, @Valid @RequestBody PaymentUpdateRequest request) {
        return paymentService.markPaid(id, request.method(), request.comment());
    }
}
