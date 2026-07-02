CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  user_id UUID REFERENCES users(id),
  action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(80) NOT NULL,
  entity_id UUID,
  actor_email VARCHAR(180) NOT NULL,
  summary TEXT,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_logs_account_created ON audit_logs(account_id, created_at DESC);
