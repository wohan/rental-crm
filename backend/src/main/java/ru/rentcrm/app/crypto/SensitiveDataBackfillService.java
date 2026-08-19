package ru.rentcrm.app.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class SensitiveDataBackfillService {
    private static final Logger log = LoggerFactory.getLogger(SensitiveDataBackfillService.class);

    private final JdbcTemplate jdbc;

    public SensitiveDataBackfillService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void encryptExistingPlaintext() {
        List<TableColumn> columns = List.of(
            new TableColumn("accounts", "name"),
            new TableColumn("users", "full_name"),
            new TableColumn("rental_objects", "title"),
            new TableColumn("rental_objects", "address"),
            new TableColumn("rental_objects", "cadastral_number"),
            new TableColumn("rental_objects", "notes"),
            new TableColumn("tenants", "full_name"),
            new TableColumn("tenants", "phone"),
            new TableColumn("tenants", "email"),
            new TableColumn("tenants", "passport_masked"),
            new TableColumn("tenants", "inn"),
            new TableColumn("tenants", "telegram_chat_id"),
            new TableColumn("tenants", "whatsapp_phone"),
            new TableColumn("tenants", "notes"),
            new TableColumn("lease_contracts", "number"),
            new TableColumn("lease_contracts", "document_url"),
            new TableColumn("lease_contracts", "notes"),
            new TableColumn("payments", "method"),
            new TableColumn("payments", "comment"),
            new TableColumn("maintenance_requests", "title"),
            new TableColumn("maintenance_requests", "description"),
            new TableColumn("documents", "name"),
            new TableColumn("documents", "file_url"),
            new TableColumn("documents", "storage_key"),
            new TableColumn("documents", "original_file_name"),
            new TableColumn("expenses", "vendor"),
            new TableColumn("expenses", "document_url"),
            new TableColumn("expenses", "comment"),
            new TableColumn("leads", "full_name"),
            new TableColumn("leads", "phone"),
            new TableColumn("leads", "email"),
            new TableColumn("leads", "comment"),
            new TableColumn("billing_settings", "yookassa_shop_id"),
            new TableColumn("billing_settings", "yookassa_secret_key"),
            new TableColumn("billing_settings", "cloud_payments_public_id"),
            new TableColumn("billing_settings", "cloud_payments_api_secret"),
            new TableColumn("billing_settings", "robokassa_merchant_login"),
            new TableColumn("billing_settings", "robokassa_password1"),
            new TableColumn("billing_settings", "generic_payment_url"),
            new TableColumn("notification_settings", "telegram_bot_token"),
            new TableColumn("notification_settings", "telegram_default_chat_id"),
            new TableColumn("notification_settings", "sms_ru_api_id"),
            new TableColumn("notification_settings", "sms_ru_sender"),
            new TableColumn("notification_settings", "whatsapp_api_url"),
            new TableColumn("notification_settings", "whatsapp_token"),
            new TableColumn("notification_settings", "whatsapp_default_recipient"),
            new TableColumn("notification_settings", "message_template"),
            new TableColumn("notification_deliveries", "recipient"),
            new TableColumn("notification_deliveries", "response")
        );

        int encrypted = 0;
        for (TableColumn column : columns) {
            encrypted += encryptColumn(column);
        }
        if (encrypted > 0) {
            log.info("Encrypted {} existing sensitive field values", encrypted);
        }
    }

    private int encryptColumn(TableColumn column) {
        String sql = "select id, " + column.name + " from " + column.table
            + " where " + column.name + " is not null and " + column.name + " <> '' and " + column.name + " not like 'enc:v1:%'";
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        for (Map<String, Object> row : rows) {
            String plaintext = String.valueOf(row.get(column.name));
            jdbc.update(
                "update " + column.table + " set " + column.name + " = ? where id = ?",
                FieldEncryption.encrypt(plaintext),
                row.get("id")
            );
        }
        return rows.size();
    }

    private record TableColumn(String table, String name) {
    }
}
