ALTER TABLE tenants
  ADD COLUMN telegram_chat_id VARCHAR(120),
  ADD COLUMN whatsapp_phone VARCHAR(60),
  ADD COLUMN notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE notification_settings (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  enabled BOOLEAN NOT NULL,
  remind_days_before INTEGER NOT NULL,
  reminder_time VARCHAR(5) NOT NULL,
  telegram_enabled BOOLEAN NOT NULL,
  telegram_bot_token VARCHAR(255),
  telegram_default_chat_id VARCHAR(120),
  sms_ru_enabled BOOLEAN NOT NULL,
  sms_ru_api_id VARCHAR(255),
  sms_ru_sender VARCHAR(80),
  sms_ru_test_mode BOOLEAN NOT NULL,
  whatsapp_enabled BOOLEAN NOT NULL,
  whatsapp_api_url VARCHAR(1000),
  whatsapp_token VARCHAR(255),
  whatsapp_default_recipient VARCHAR(80),
  message_template TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX ux_notification_settings_account ON notification_settings(account_id);

CREATE TABLE notification_deliveries (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  payment_id UUID NOT NULL REFERENCES payments(id),
  channel VARCHAR(40) NOT NULL,
  remind_days_before INTEGER NOT NULL,
  recipient VARCHAR(180),
  status VARCHAR(40) NOT NULL,
  response TEXT,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX ux_notification_delivery_once
  ON notification_deliveries(payment_id, channel, remind_days_before);
