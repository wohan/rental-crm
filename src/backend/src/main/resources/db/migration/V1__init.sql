CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE properties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(32) NOT NULL,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(120) NOT NULL,
    area NUMERIC(10,2),
    rooms INTEGER,
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    cadastral_number VARCHAR(80),
    monthly_rent NUMERIC(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'VACANT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(180) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    email VARCHAR(180),
    legal_type VARCHAR(32) NOT NULL DEFAULT 'INDIVIDUAL',
    inn VARCHAR(20),
    passport_encrypted TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE leases (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL REFERENCES properties(id),
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    monthly_rent NUMERIC(14,2) NOT NULL,
    deposit_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    payment_day INTEGER NOT NULL CHECK (payment_day BETWEEN 1 AND 28),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    contract_number VARCHAR(80),
    contract_file_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    lease_id BIGINT NOT NULL REFERENCES leases(id),
    due_date DATE NOT NULL,
    paid_at TIMESTAMPTZ,
    amount NUMERIC(14,2) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    method VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    fiscal_receipt_id VARCHAR(120),
    provider_payment_id VARCHAR(120),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE maintenance_requests (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL REFERENCES properties(id),
    tenant_id BIGINT REFERENCES tenants(id),
    title VARCHAR(180) NOT NULL,
    description TEXT,
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    cost_estimate NUMERIC(14,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    due_date DATE
);

CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    lease_id BIGINT REFERENCES leases(id),
    tenant_id BIGINT REFERENCES tenants(id),
    type VARCHAR(32) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payload TEXT
);

CREATE INDEX idx_properties_status ON properties(status);
CREATE INDEX idx_leases_status_end_date ON leases(status, end_date);
CREATE INDEX idx_payments_status_due_date ON payments(status, due_date);
CREATE INDEX idx_maintenance_status ON maintenance_requests(status);
