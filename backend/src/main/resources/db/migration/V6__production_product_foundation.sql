ALTER TABLE accounts
  ADD COLUMN subscription_paid_until TIMESTAMPTZ,
  ADD COLUMN subscription_grace_until TIMESTAMPTZ,
  ADD COLUMN blocked_at TIMESTAMPTZ,
  ADD COLUMN blocked_reason VARCHAR(500);

UPDATE accounts
SET subscription_paid_until = created_at + INTERVAL '14 days',
    subscription_grace_until = created_at + INTERVAL '21 days'
WHERE subscription_paid_until IS NULL;

ALTER TABLE users
  ADD COLUMN terms_accepted_at TIMESTAMPTZ,
  ADD COLUMN privacy_accepted_at TIMESTAMPTZ,
  ADD COLUMN notification_consent_at TIMESTAMPTZ,
  ADD COLUMN last_password_change_at TIMESTAMPTZ;

UPDATE users
SET terms_accepted_at = created_at,
    privacy_accepted_at = created_at,
    last_password_change_at = created_at
WHERE terms_accepted_at IS NULL;

ALTER TABLE tenants
  ADD COLUMN public_request_token VARCHAR(80) UNIQUE;

UPDATE tenants
SET public_request_token = replace(id::text, '-', '')
WHERE public_request_token IS NULL;

ALTER TABLE documents
  ADD COLUMN storage_key VARCHAR(500),
  ADD COLUMN original_file_name VARCHAR(255),
  ADD COLUMN content_type VARCHAR(120),
  ADD COLUMN size_bytes BIGINT,
  ADD COLUMN scan_status VARCHAR(40) NOT NULL DEFAULT 'NOT_SCANNED';

CREATE INDEX idx_tenants_public_request_token ON tenants(public_request_token);
CREATE INDEX idx_billing_invoices_provider_payment_id ON billing_invoices(provider_payment_id);
