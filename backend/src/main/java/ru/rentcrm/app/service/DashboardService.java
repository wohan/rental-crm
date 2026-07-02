package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.ObjectStatus;
import ru.rentcrm.app.model.Payment;
import ru.rentcrm.app.model.PaymentStatus;
import ru.rentcrm.app.repository.ObjectRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardService {
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final ObjectRepository objects;
    private final PaymentService payments;
    private final ExpenseService expenses;
    private final ContractService contracts;
    private final MaintenanceRequestService maintenance;

    public DashboardService(
        ObjectRepository objects,
        PaymentService payments,
        ExpenseService expenses,
        ContractService contracts,
        MaintenanceRequestService maintenance
    ) {
        this.objects = objects;
        this.payments = payments;
        this.expenses = expenses;
        this.contracts = contracts;
        this.maintenance = maintenance;
    }

    public Map<String, Object> dashboard(UUID accountId) {
        log.debug("Building dashboard started accountId={}", accountId);
        LocalDate today = LocalDate.now();
        List<Payment> monthPaid = payments.all(accountId).stream()
            .filter(p -> p.status == PaymentStatus.PAID
                && p.paidDate != null
                && p.paidDate.getMonth() == today.getMonth()
                && p.paidDate.getYear() == today.getYear())
            .toList();
        BigDecimal paidThisMonth = monthPaid.stream()
            .map(p -> p.paidAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overdue = payments.overdue(accountId).stream()
            .map(p -> p.amount.subtract(p.paidAmount))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expensesThisMonth = expenses.sumForMonth(accountId, today);

        Map<String, Object> result = Map.of(
            "objectsTotal", objects.countByAccountId(accountId),
            "objectsOccupied", objects.countByAccountIdAndStatus(accountId, ObjectStatus.OCCUPIED),
            "paidThisMonth", paidThisMonth,
            "expensesThisMonth", expensesThisMonth,
            "netThisMonth", paidThisMonth.subtract(expensesThisMonth),
            "overdueAmount", overdue,
            "upcomingPayments", payments.upcoming(accountId, today, today.plusDays(14)),
            "expiringContracts", contracts.expiring(accountId, today, today.plusDays(30)),
            "openMaintenance", maintenance.open(accountId)
        );
        log.debug("Building dashboard finished accountId={}", accountId);
        return result;
    }
}
