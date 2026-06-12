package ru.rentalcrm.service;

import org.junit.jupiter.api.Test;
import ru.rentalcrm.model.Payment;
import ru.rentalcrm.model.PaymentMethod;
import ru.rentalcrm.model.PaymentStatus;
import ru.rentalcrm.repository.PaymentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    private final PaymentRepository repository = mock(PaymentRepository.class);
    private final PaymentService service = new PaymentService(repository);

    @Test
    void markPaidStoresMethodTimestampAndComment() {
        Payment payment = new Payment();
        payment.setId(10L);
        payment.setStatus(PaymentStatus.PLANNED);
        when(repository.findById(10L)).thenReturn(Optional.of(payment));
        when(repository.save(payment)).thenReturn(payment);

        Payment result = service.markPaid(10L, PaymentMethod.SBP, "Оплачено по СБП");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.SBP);
        assertThat(result.getPaidAt()).isNotNull();
        assertThat(result.getComment()).isEqualTo("Оплачено по СБП");
        verify(repository).save(payment);
    }

    @Test
    void markOverduePaymentsOnlyMovesPastPlannedPayments() {
        Payment first = new Payment();
        Payment second = new Payment();
        LocalDate today = LocalDate.of(2026, 6, 12);
        when(repository.findByStatusAndDueDateBefore(PaymentStatus.PLANNED, today)).thenReturn(List.of(first, second));

        int updated = service.markOverduePayments(today);

        assertThat(updated).isEqualTo(2);
        assertThat(first.getStatus()).isEqualTo(PaymentStatus.OVERDUE);
        assertThat(second.getStatus()).isEqualTo(PaymentStatus.OVERDUE);
        verify(repository).saveAll(List.of(first, second));
    }
}
