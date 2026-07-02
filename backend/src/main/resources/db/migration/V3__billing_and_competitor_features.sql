ALTER TABLE accounts
  ALTER COLUMN tariff SET DEFAULT 'FREE',
  ALTER COLUMN objects_limit SET DEFAULT 3;

CREATE TABLE billing_settings (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  provider VARCHAR(40) NOT NULL,
  yookassa_shop_id VARCHAR(120),
  yookassa_secret_key VARCHAR(255),
  cloud_payments_public_id VARCHAR(120),
  cloud_payments_api_secret VARCHAR(255),
  robokassa_merchant_login VARCHAR(180),
  robokassa_password1 VARCHAR(255),
  generic_payment_url VARCHAR(1000),
  test_mode BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX ux_billing_settings_account ON billing_settings(account_id);

CREATE TABLE billing_invoices (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  tariff VARCHAR(40) NOT NULL,
  amount NUMERIC(14,2) NOT NULL,
  objects_limit INTEGER NOT NULL,
  provider VARCHAR(40) NOT NULL,
  status VARCHAR(40) NOT NULL,
  provider_payment_id VARCHAR(180),
  confirmation_url VARCHAR(1000),
  provider_response TEXT,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE expenses (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  object_id UUID NOT NULL REFERENCES rental_objects(id),
  expense_date DATE NOT NULL,
  category VARCHAR(120) NOT NULL,
  amount NUMERIC(14,2) NOT NULL,
  vendor VARCHAR(180),
  document_url VARCHAR(1000),
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE listings (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  object_id UUID NOT NULL REFERENCES rental_objects(id),
  title VARCHAR(180) NOT NULL,
  description TEXT NOT NULL,
  price NUMERIC(14,2) NOT NULL,
  public_url VARCHAR(1000),
  published BOOLEAN NOT NULL,
  published_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE leads (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  listing_id UUID REFERENCES listings(id),
  object_id UUID REFERENCES rental_objects(id),
  full_name VARCHAR(180) NOT NULL,
  phone VARCHAR(60) NOT NULL,
  email VARCHAR(180),
  source VARCHAR(120),
  status VARCHAR(40) NOT NULL,
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
