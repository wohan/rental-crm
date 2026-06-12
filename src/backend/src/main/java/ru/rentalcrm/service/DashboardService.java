package ru.rentalcrm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentalcrm.dto.DashboardDto;
import ru.rentalcrm.model.*;
import ru.rentalcrm.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final MaintenanceRequestRepository maintenanceRepository;

    public DashboardService(PropertyRepository propertyRepository,
                            LeaseRepository leaseRepository,
                            PaymentRepository paymentRepository,
                            MaintenanceRequestRepository maintenanceRepository) {
        this.propertyRepository = propertyRepository;
        this.leaseRepository = leaseRepository;
        this.paymentRepository = paymentRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        var today = LocalDate.now();
        return new DashboardDto(
                propertyRepository.count(),
                propertyRepository.countByStatus(PropertyStatus.OCCUPIED),
                leaseRepository.countByStatus(LeaseStatus.ACTIVE),
                paymentRepository.countByStatus(PaymentStatus.OVERDUE),
                maintenanceRepository.countByStatusIn(List.of(MaintenanceStatus.OPEN, MaintenanceStatus.IN_PROGRESS, MaintenanceStatus.WAITING_TENANT)),
                paymentRepository.sumPaid(),
                paymentRepository.sumReceivable(),
                paymentRepository.findTop10ByStatusInOrderByDueDateAsc(List.of(PaymentStatus.PLANNED, PaymentStatus.PENDING, PaymentStatus.OVERDUE)),
                leaseRepository.findTop10ByStatusAndEndDateBetweenOrderByEndDateAsc(LeaseStatus.ACTIVE, today, today.plusDays(45)),
                maintenanceRepository.findTop10ByStatusInOrderByDueDateAscCreatedAtAsc(List.of(MaintenanceStatus.OPEN, MaintenanceStatus.IN_PROGRESS, MaintenanceStatus.WAITING_TENANT))
        );
    }
}
