package ru.rentalcrm.service;

import org.junit.jupiter.api.Test;
import ru.rentalcrm.model.*;
import ru.rentalcrm.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    private final PropertyRepository propertyRepository = mock(PropertyRepository.class);
    private final LeaseRepository leaseRepository = mock(LeaseRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final MaintenanceRequestRepository maintenanceRepository = mock(MaintenanceRequestRepository.class);
    private final DashboardService service = new DashboardService(propertyRepository, leaseRepository, paymentRepository, maintenanceRepository);

    @Test
    void getDashboardAggregatesCriticalPortfolioMetrics() {
        when(propertyRepository.count()).thenReturn(8L);
        when(propertyRepository.countByStatus(PropertyStatus.OCCUPIED)).thenReturn(6L);
        when(leaseRepository.countByStatus(LeaseStatus.ACTIVE)).thenReturn(6L);
        when(paymentRepository.countByStatus(PaymentStatus.OVERDUE)).thenReturn(2L);
        when(maintenanceRepository.countByStatusIn(any())).thenReturn(3L);
        when(paymentRepository.sumPaid()).thenReturn(new BigDecimal("330000"));
        when(paymentRepository.sumReceivable()).thenReturn(new BigDecimal("120000"));
        when(paymentRepository.findTop10ByStatusInOrderByDueDateAsc(any())).thenReturn(List.of(new Payment()));
        when(leaseRepository.findTop10ByStatusAndEndDateBetweenOrderByEndDateAsc(eq(LeaseStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new Lease()));
        when(maintenanceRepository.findTop10ByStatusInOrderByDueDateAscCreatedAtAsc(any())).thenReturn(List.of(new MaintenanceRequest()));

        var dashboard = service.getDashboard();

        assertThat(dashboard.propertiesTotal()).isEqualTo(8);
        assertThat(dashboard.occupiedProperties()).isEqualTo(6);
        assertThat(dashboard.activeLeases()).isEqualTo(6);
        assertThat(dashboard.overduePayments()).isEqualTo(2);
        assertThat(dashboard.openMaintenanceRequests()).isEqualTo(3);
        assertThat(dashboard.paidRevenue()).isEqualByComparingTo("330000");
        assertThat(dashboard.receivable()).isEqualByComparingTo("120000");
        assertThat(dashboard.upcomingPayments()).hasSize(1);
        assertThat(dashboard.expiringLeases()).hasSize(1);
        assertThat(dashboard.maintenanceQueue()).hasSize(1);
    }
}
