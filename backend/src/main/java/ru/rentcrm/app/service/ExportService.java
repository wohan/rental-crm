package ru.rentcrm.app.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.Expense;
import ru.rentcrm.app.model.LeaseContract;
import ru.rentcrm.app.model.MaintenanceRequest;
import ru.rentcrm.app.model.Payment;
import ru.rentcrm.app.model.RentalObject;
import ru.rentcrm.app.model.Tenant;
import ru.rentcrm.app.repository.ContractRepository;
import ru.rentcrm.app.repository.ExpenseRepository;
import ru.rentcrm.app.repository.MaintenanceRepository;
import ru.rentcrm.app.repository.ObjectRepository;
import ru.rentcrm.app.repository.PaymentRepository;
import ru.rentcrm.app.repository.TenantRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;

@Service
public class ExportService {
    private final ObjectRepository objects;
    private final TenantRepository tenants;
    private final ContractRepository contracts;
    private final PaymentRepository payments;
    private final ExpenseRepository expenses;
    private final MaintenanceRepository maintenance;

    public ExportService(
        ObjectRepository objects,
        TenantRepository tenants,
        ContractRepository contracts,
        PaymentRepository payments,
        ExpenseRepository expenses,
        MaintenanceRepository maintenance
    ) {
        this.objects = objects;
        this.tenants = tenants;
        this.contracts = contracts;
        this.payments = payments;
        this.expenses = expenses;
        this.maintenance = maintenance;
    }

    public byte[] accountWorkbook(UUID accountId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            sheet(workbook, "Объекты",
                List.of("Название", "Тип", "Адрес", "Статус", "Аренда", "Депозит", "Заметки"),
                objects.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                    .map(item -> row(item.title, item.type, item.address, item.status, item.monthlyRent, item.depositAmount, item.notes))
                    .toList());
            sheet(workbook, "Арендаторы",
                List.of("ФИО", "Телефон", "Email", "Telegram", "WhatsApp", "Уведомления", "Публичная ссылка"),
                tenants.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                    .map(item -> row(item.fullName, item.phone, item.email, item.telegramChatId, item.whatsappPhone, item.notificationsEnabled, item.publicRequestToken))
                    .toList());
            sheet(workbook, "Договоры",
                List.of("Номер", "Объект", "Арендатор", "Начало", "Окончание", "Аренда", "День оплаты", "Статус"),
                contracts.findByAccountIdOrderByEndDateAsc(accountId).stream()
                    .map(item -> row(item.number, item.objectId, item.tenantId, item.startDate, item.endDate, item.rentAmount, item.paymentDay, item.status))
                    .toList());
            sheet(workbook, "Платежи",
                List.of("Дата", "Сумма", "Оплачено", "Статус", "Тип", "Объект", "Арендатор", "Комментарий"),
                payments.findByAccountIdOrderByDueDateAsc(accountId).stream()
                    .map(item -> row(item.dueDate, item.amount, item.paidAmount, item.status, item.type, item.objectId, item.tenantId, item.comment))
                    .toList());
            sheet(workbook, "Расходы",
                List.of("Дата", "Категория", "Сумма", "Подрядчик", "Объект", "Комментарий"),
                expenses.findByAccountIdOrderByExpenseDateDesc(accountId).stream()
                    .map(item -> row(item.expenseDate, item.category, item.amount, item.vendor, item.objectId, item.comment))
                    .toList());
            sheet(workbook, "Заявки",
                List.of("Название", "Статус", "Приоритет", "Срок", "Стоимость", "Объект", "Арендатор", "Описание"),
                maintenance.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                    .map(item -> row(item.title, item.status, item.priority, item.dueDate, item.cost, item.objectId, item.tenantId, item.description))
                    .toList());
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create export workbook", ex);
        }
    }

    private void sheet(Workbook workbook, String name, List<String> headers, List<List<Object>> rows) {
        Sheet sheet = workbook.createSheet(name);
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            header.createCell(i).setCellValue(headers.get(i));
        }
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            List<Object> values = rows.get(rowIndex);
            for (int cellIndex = 0; cellIndex < values.size(); cellIndex++) {
                write(row.createCell(cellIndex), values.get(cellIndex));
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private List<Object> row(Object... values) {
        return java.util.Arrays.asList(values);
    }

    private void write(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof BigDecimal decimal) {
            cell.setCellValue(decimal.doubleValue());
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool ? "Да" : "Нет");
        } else if (value instanceof TemporalAccessor || value instanceof UUID || value instanceof Enum<?>) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }
}
