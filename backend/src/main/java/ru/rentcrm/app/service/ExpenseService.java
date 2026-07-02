package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.Expense;
import ru.rentcrm.app.model.RentalObject;
import ru.rentcrm.app.repository.ExpenseRepository;
import ru.rentcrm.app.repository.ObjectRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {
    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);
    private static final String MANUAL_SOURCE = "MANUAL";
    private static final String UTILITY_SOURCE = "UTILITY_RECURRING";
    private static final String UTILITY_CATEGORY = "Коммунальные платежи";
    private static final String UTILITY_VENDOR = "Автоначисление";
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM.yyyy");

    private final ExpenseRepository expenses;
    private final ObjectRepository objects;
    private final OwnershipService ownership;
    private final AuditService audit;

    public ExpenseService(ExpenseRepository expenses, ObjectRepository objects, OwnershipService ownership, AuditService audit) {
        this.expenses = expenses;
        this.objects = objects;
        this.ownership = ownership;
        this.audit = audit;
    }

    public List<Expense> list(UUID accountId) {
        log.debug("Listing expenses started accountId={}", accountId);
        ensureMonthlyUtilityExpenses(accountId, LocalDate.now());
        List<Expense> result = expenses.findByAccountIdOrderByExpenseDateDesc(accountId);
        log.debug("Listing expenses finished accountId={} count={}", accountId, result.size());
        return result;
    }

    @Transactional
    public Expense create(AppUser actor, Expense input) {
        log.debug("Creating expense started accountId={}", actor.accountId);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.createdAt = Instant.now();
        input.updatedAt = Instant.now();
        normalizeManualExpense(input);
        Expense saved = expenses.save(input);
        audit.record(actor, "CREATE", "EXPENSE", saved.id, saved.category);
        log.debug("Creating expense finished accountId={} expenseId={}", actor.accountId, saved.id);
        return saved;
    }

    @Transactional
    public Expense update(AppUser actor, UUID id, Expense input) {
        log.debug("Updating expense started accountId={} expenseId={}", actor.accountId, id);
        Expense item = ownership.requireOwned(expenses.findById(id), actor.accountId);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        input.updatedAt = Instant.now();
        input.source = item.source;
        input.periodMonth = item.periodMonth;
        normalizeExpense(input);
        Expense saved = expenses.save(input);
        audit.record(actor, "UPDATE", "EXPENSE", saved.id, saved.category);
        log.debug("Updating expense finished accountId={} expenseId={}", actor.accountId, saved.id);
        return saved;
    }

    @Transactional
    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting expense started accountId={} expenseId={}", actor.accountId, id);
        Expense item = ownership.requireOwned(expenses.findById(id), actor.accountId);
        expenses.delete(item);
        audit.record(actor, "DELETE", "EXPENSE", id, item.category);
        log.debug("Deleting expense finished accountId={} expenseId={}", actor.accountId, id);
    }

    public BigDecimal sumForMonth(UUID accountId, LocalDate date) {
        ensureMonthlyUtilityExpenses(accountId, date);
        return expenses.findByAccountIdOrderByExpenseDateDesc(accountId).stream()
            .filter(e -> e.expenseDate != null && e.expenseDate.getMonth() == date.getMonth() && e.expenseDate.getYear() == date.getYear())
            .map(e -> e.amount == null ? BigDecimal.ZERO : e.amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized void ensureMonthlyUtilityExpenses(UUID accountId, LocalDate date) {
        LocalDate periodMonth = date.withDayOfMonth(1);
        log.debug("Ensuring monthly utility expenses started accountId={} periodMonth={}", accountId, periodMonth);
        List<RentalObject> rentalObjects = objects.findByAccountIdOrderByCreatedAtDesc(accountId);
        int created = 0;
        for (RentalObject object : rentalObjects) {
            BigDecimal amount = object.monthlyUtilityAmount == null ? BigDecimal.ZERO : object.monthlyUtilityAmount;
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            boolean exists = expenses.findByAccountIdAndObjectIdAndSourceAndPeriodMonth(accountId, object.id, UTILITY_SOURCE, periodMonth).isPresent();
            if (exists) {
                continue;
            }
            Expense utilityExpense = new Expense();
            utilityExpense.id = UUID.randomUUID();
            utilityExpense.accountId = accountId;
            utilityExpense.objectId = object.id;
            utilityExpense.expenseDate = periodMonth;
            utilityExpense.periodMonth = periodMonth;
            utilityExpense.category = UTILITY_CATEGORY;
            utilityExpense.amount = amount;
            utilityExpense.vendor = UTILITY_VENDOR;
            utilityExpense.source = UTILITY_SOURCE;
            utilityExpense.comment = "Коммунальные платежи по объекту \"" + object.title + "\" за " + periodMonth.format(MONTH_FORMAT);
            utilityExpense.createdAt = Instant.now();
            utilityExpense.updatedAt = utilityExpense.createdAt;
            try {
                expenses.saveAndFlush(utilityExpense);
                created += 1;
            } catch (DataIntegrityViolationException duplicate) {
                log.debug("Monthly utility expense already exists accountId={} objectId={} periodMonth={}", accountId, object.id, periodMonth);
            }
        }
        log.debug("Ensuring monthly utility expenses finished accountId={} periodMonth={} created={}", accountId, periodMonth, created);
    }

    private void normalizeManualExpense(Expense expense) {
        if (expense.source == null || expense.source.isBlank()) {
            expense.source = MANUAL_SOURCE;
        }
        normalizeExpense(expense);
    }

    private void normalizeExpense(Expense expense) {
        if (expense.amount == null) {
            expense.amount = BigDecimal.ZERO;
        }
        if (expense.source == null || expense.source.isBlank()) {
            expense.source = MANUAL_SOURCE;
        }
    }
}
