package ru.rentalcrm.dto;

import ru.rentalcrm.model.Lease;
import ru.rentalcrm.model.MaintenanceRequest;
import ru.rentalcrm.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        long propertiesTotal,
        long occupiedProperties,
        long activeLeases,
        long overduePayments,
        long openMaintenanceRequests,
        BigDecimal paidRevenue,
        BigDecimal receivable,
        List<Payment> upcomingPayments,
        List<Lease> expiringLeases,
        List<MaintenanceRequest> maintenanceQueue
) {
}
