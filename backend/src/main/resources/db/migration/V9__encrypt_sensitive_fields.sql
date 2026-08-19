ALTER TABLE accounts
  ALTER COLUMN name TYPE TEXT;

ALTER TABLE users
  ALTER COLUMN full_name TYPE TEXT;

ALTER TABLE rental_objects
  ALTER COLUMN title TYPE TEXT,
  ALTER COLUMN address TYPE TEXT,
  ALTER COLUMN cadastral_number TYPE TEXT,
  ALTER COLUMN notes TYPE TEXT;

ALTER TABLE tenants
  ALTER COLUMN full_name TYPE TEXT,
  ALTER COLUMN phone TYPE TEXT,
  ALTER COLUMN email TYPE TEXT,
  ALTER COLUMN passport_masked TYPE TEXT,
  ALTER COLUMN inn TYPE TEXT,
  ALTER COLUMN telegram_chat_id TYPE TEXT,
  ALTER COLUMN whatsapp_phone TYPE TEXT,
  ALTER COLUMN notes TYPE TEXT;

ALTER TABLE lease_contracts
  ALTER COLUMN number TYPE TEXT,
  ALTER COLUMN document_url TYPE TEXT,
  ALTER COLUMN notes TYPE TEXT;

ALTER TABLE payments
  ALTER COLUMN method TYPE TEXT,
  ALTER COLUMN comment TYPE TEXT;

ALTER TABLE maintenance_requests
  ALTER COLUMN title TYPE TEXT,
  ALTER COLUMN description TYPE TEXT;

ALTER TABLE documents
  ALTER COLUMN name TYPE TEXT,
  ALTER COLUMN file_url TYPE TEXT,
  ALTER COLUMN storage_key TYPE TEXT,
  ALTER COLUMN original_file_name TYPE TEXT;

ALTER TABLE expenses
  ALTER COLUMN vendor TYPE TEXT,
  ALTER COLUMN document_url TYPE TEXT,
  ALTER COLUMN comment TYPE TEXT;

ALTER TABLE leads
  ALTER COLUMN full_name TYPE TEXT,
  ALTER COLUMN phone TYPE TEXT,
  ALTER COLUMN email TYPE TEXT,
  ALTER COLUMN comment TYPE TEXT;

ALTER TABLE billing_settings
  ALTER COLUMN yookassa_shop_id TYPE TEXT,
  ALTER COLUMN yookassa_secret_key TYPE TEXT,
  ALTER COLUMN cloud_payments_public_id TYPE TEXT,
  ALTER COLUMN cloud_payments_api_secret TYPE TEXT,
  ALTER COLUMN robokassa_merchant_login TYPE TEXT,
  ALTER COLUMN robokassa_password1 TYPE TEXT,
  ALTER COLUMN generic_payment_url TYPE TEXT;

ALTER TABLE notification_settings
  ALTER COLUMN telegram_bot_token TYPE TEXT,
  ALTER COLUMN telegram_default_chat_id TYPE TEXT,
  ALTER COLUMN sms_ru_api_id TYPE TEXT,
  ALTER COLUMN sms_ru_sender TYPE TEXT,
  ALTER COLUMN whatsapp_api_url TYPE TEXT,
  ALTER COLUMN whatsapp_token TYPE TEXT,
  ALTER COLUMN whatsapp_default_recipient TYPE TEXT,
  ALTER COLUMN message_template TYPE TEXT;

ALTER TABLE notification_deliveries
  ALTER COLUMN recipient TYPE TEXT,
  ALTER COLUMN response TYPE TEXT;
