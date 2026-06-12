package ru.rentalcrm.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentalcrm.model.Payment;
import ru.rentalcrm.model.PaymentMethod;
import ru.rentalcrm.model.PaymentStatus;
import ru.rentalcrm.repository.PaymentRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment markPaid(Long id, PaymentMethod method, String comment) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(OffsetDateTime.now());
        payment.setMethod(method);
        payment.setComment(comment);
        return paymentRepository.save(payment);
    }

    @Transactional
    public int markOverduePayments(LocalDate today) {
        List<Payment> planned = paymentRepository.findByStatusAndDueDateBefore(PaymentStatus.PLANNED, today);
        planned.forEach(payment -> payment.setStatus(PaymentStatus.OVERDUE));
        paymentRepository.saveAll(planned);
        return planned.size();
    }
}
