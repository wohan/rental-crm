CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  name VARCHAR(180) NOT NULL,
  tariff VARCHAR(40) NOT NULL,
  objects_limit INTEGER NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE users (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(180) NOT NULL,
  role VARCHAR(40) NOT NULL,
  enabled BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE rental_objects (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  title VARCHAR(180) NOT NULL,
  type VARCHAR(40) NOT NULL,
  address VARCHAR(500) NOT NULL,
  area_sqm NUMERIC(12,2),
  cadastral_number VARCHAR(80),
  status VARCHAR(40) NOT NULL,
  monthly_rent NUMERIC(14,2) NOT NULL,
  deposit_amount NUMERIC(14,2) NOT NULL,
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE tenants (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  full_name VARCHAR(180) NOT NULL,
  phone VARCHAR(60) NOT NULL,
  email VARCHAR(180),
  passport_masked VARCHAR(120),
  inn VARCHAR(20),
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE lease_contracts (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  object_id UUID NOT NULL REFERENCES rental_objects(id),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  number VARCHAR(80) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  rent_amount NUMERIC(14,2) NOT NULL,
  payment_day INTEGER NOT NULL,
  deposit_amount NUMERIC(14,2) NOT NULL,
  status VARCHAR(40) NOT NULL,
  document_url VARCHAR(1000),
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE payments (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  object_id UUID NOT NULL REFERENCES rental_objects(id),
  tenant_id UUID REFERENCES tenants(id),
  contract_id UUID REFERENCES lease_contracts(id),
  due_date DATE NOT NULL,
  paid_date DATE,
  amount NUMERIC(14,2) NOT NULL,
  paid_amount NUMERIC(14,2) NOT NULL,
  status VARCHAR(40) NOT NULL,
  type VARCHAR(40) NOT NULL,
  method VARCHAR(80),
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE maintenance_requests (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  object_id UUID NOT NULL REFERENCES rental_objects(id),
  tenant_id UUID REFERENCES tenants(id),
  title VARCHAR(180) NOT NULL,
  description TEXT,
  priority VARCHAR(40) NOT NULL,
  status VARCHAR(40) NOT NULL,
  cost NUMERIC(14,2) NOT NULL,
  due_date DATE,
  closed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE documents (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  object_id UUID REFERENCES rental_objects(id),
  tenant_id UUID REFERENCES tenants(id),
  contract_id UUID REFERENCES lease_contracts(id),
  name VARCHAR(180) NOT NULL,
  type VARCHAR(40) NOT NULL,
  file_url VARCHAR(1000) NOT NULL,
  expires_at DATE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_objects_account ON rental_objects(account_id);
CREATE INDEX idx_tenants_account ON tenants(account_id);
CREATE INDEX idx_contracts_account ON lease_contracts(account_id);
CREATE INDEX idx_payments_account_due ON payments(account_id, due_date);
CREATE INDEX idx_maintenance_account_status ON maintenance_requests(account_id, status);
