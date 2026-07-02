CREATE TABLE error_events (
  id UUID PRIMARY KEY,
  account_id UUID,
  actor_email VARCHAR(180),
  method VARCHAR(20),
  path VARCHAR(500),
  exception_class VARCHAR(255),
  message TEXT,
  stack_trace TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  notified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_error_events_created_at ON error_events(created_at DESC);
CREATE INDEX idx_error_events_account ON error_events(account_id, created_at DESC);
