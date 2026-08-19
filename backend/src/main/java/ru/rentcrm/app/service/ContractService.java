package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.ContractStatus;
import ru.rentcrm.app.model.LeaseContract;
import ru.rentcrm.app.model.Payment;
import ru.rentcrm.app.model.PaymentStatus;
import ru.rentcrm.app.model.PaymentType;
import ru.rentcrm.app.repository.ContractRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContractService {
    private static final Logger log = LoggerFactory.getLogger(ContractService.class);
    private static final DateTimeFormatter CONTRACT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final ContractRepository contracts;
    private final OwnershipService ownership;
    private final ObjectService objectService;
    private final TenantService tenantService;
    private final PaymentService paymentService;
    private final AuditService audit;

    public ContractService(
        ContractRepository contracts,
        OwnershipService ownership,
        ObjectService objectService,
        TenantService tenantService,
        PaymentService paymentService,
        AuditService audit
    ) {
        this.contracts = contracts;
        this.ownership = ownership;
        this.objectService = objectService;
        this.tenantService = tenantService;
        this.paymentService = paymentService;
        this.audit = audit;
    }

    public List<LeaseContract> list(UUID accountId) {
        log.debug("Listing contracts started accountId={}", accountId);
        List<LeaseContract> result = contracts.findByAccountIdOrderByEndDateAsc(accountId);
        log.debug("Listing contracts finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public LeaseContract create(AppUser actor, LeaseContract input) {
        log.info("Creating lease contract started accountId={} number={}", actor.accountId, input.number);
        validateForSave(input);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        if (input.number == null || input.number.isBlank()) {
            input.number = nextContractNumber(actor.accountId, input.startDate);
        }
        objectService.markOccupied(actor.accountId, input.objectId);
        tenantService.require(actor.accountId, input.tenantId);
        LeaseContract saved = contracts.save(input);
        createPaymentSchedule(saved);
        audit.record(actor, "CREATE", "CONTRACT", saved.id, saved.number);
        log.info("Creating lease contract finished accountId={} contractId={}", actor.accountId, saved.id);
        return saved;
    }

    public LeaseContract update(AppUser actor, UUID id, LeaseContract input) {
        log.info("Updating lease contract started accountId={} contractId={}", actor.accountId, id);
        LeaseContract item = require(actor.accountId, id);
        validateForSave(input);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        input.updatedAt = Instant.now();
        objectService.markOccupied(actor.accountId, input.objectId);
        tenantService.require(actor.accountId, input.tenantId);
        LeaseContract saved = contracts.save(input);
        createPaymentSchedule(saved);
        audit.record(actor, "UPDATE", "CONTRACT", saved.id, saved.number);
        log.info("Updating lease contract finished accountId={} contractId={}", actor.accountId, saved.id);
        return saved;
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting lease contract started accountId={} contractId={}", actor.accountId, id);
        LeaseContract item = require(actor.accountId, id);
        contracts.delete(item);
        audit.record(actor, "DELETE", "CONTRACT", id, item.number);
        log.debug("Deleting lease contract finished accountId={} contractId={}", actor.accountId, id);
    }

    public List<LeaseContract> expiring(UUID accountId, LocalDate from, LocalDate to) {
        return contracts.findByAccountIdAndEndDateBetween(accountId, from, to);
    }

    public List<LeaseContract> byTenant(UUID tenantId) {
        return contracts.findByTenantIdOrderByEndDateDesc(tenantId);
    }

    private LeaseContract require(UUID accountId, UUID id) {
        return ownership.requireOwned(contracts.findById(id), accountId);
    }

    private void validateForSave(LeaseContract input) {
        if (input.objectId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Выберите объект");
        }
        if (input.tenantId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Выберите арендатора");
        }
        if (input.startDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите дату начала договора");
        }
        if (input.endDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите дату окончания договора");
        }
        if (input.endDate.isBefore(input.startDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Дата окончания не может быть раньше даты начала");
        }
        if (input.rentAmount == null) {
            input.rentAmount = BigDecimal.ZERO;
        }
        if (input.rentAmount.signum() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Сумма аренды не может быть отрицательной");
        }
        if (input.depositAmount == null) {
            input.depositAmount = BigDecimal.ZERO;
        }
        if (input.depositAmount.signum() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Депозит не может быть отрицательным");
        }
        if (input.paymentDay == null || input.paymentDay < 1 || input.paymentDay > 31) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите день оплаты от 1 до 31");
        }
        if (input.status == null) {
            input.status = ContractStatus.ACTIVE;
        }
    }

    private String nextContractNumber(UUID accountId, LocalDate contractDate) {
        long sequence = contracts.countByAccountId(accountId) + 1;
        LocalDate date = Optional.ofNullable(contractDate).orElse(LocalDate.now());
        return "%03d-%s".formatted(sequence, date.format(CONTRACT_DATE_FORMAT));
    }

    private void createPaymentSchedule(LeaseContract contract) {
        LocalDate dueDate = firstDueDate(contract.startDate, contract.paymentDay);
        while (!dueDate.isAfter(contract.endDate)) {
            if (!paymentService.existsByContractAndDueDate(contract.id, dueDate)) {
                Payment payment = new Payment();
                payment.id = UUID.randomUUID();
                payment.accountId = contract.accountId;
                payment.objectId = contract.objectId;
                payment.tenantId = contract.tenantId;
                payment.contractId = contract.id;
                payment.dueDate = dueDate;
                payment.amount = contract.rentAmount;
                payment.paidAmount = BigDecimal.ZERO;
                payment.status = PaymentStatus.PLANNED;
                payment.type = PaymentType.RENT;
                payment.comment = "Аренда по договору № " + contract.number;
                payment.createdAt = Instant.now();
                payment.updatedAt = Instant.now();
                paymentService.saveScheduled(payment);
            }
            dueDate = nextDueDate(dueDate, contract.paymentDay);
        }
    }

    private LocalDate firstDueDate(LocalDate startDate, Integer paymentDay) {
        LocalDate candidate = dueDateInMonth(YearMonth.from(startDate), paymentDay);
        return candidate.isBefore(startDate) ? nextDueDate(candidate, paymentDay) : candidate;
    }

    private LocalDate nextDueDate(LocalDate date, Integer paymentDay) {
        return dueDateInMonth(YearMonth.from(date).plusMonths(1), paymentDay);
    }

    private LocalDate dueDateInMonth(YearMonth month, Integer paymentDay) {
        int day = Math.min(Math.max(Optional.ofNullable(paymentDay).orElse(1), 1), month.lengthOfMonth());
        return month.atDay(day);
    }
}
