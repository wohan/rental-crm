ALTER TABLE rental_objects
  ADD COLUMN monthly_utility_amount NUMERIC(14,2) NOT NULL DEFAULT 0;

ALTER TABLE expenses
  ADD COLUMN source VARCHAR(40) NOT NULL DEFAULT 'MANUAL',
  ADD COLUMN period_month DATE;

CREATE INDEX idx_expenses_account_date ON expenses(account_id, expense_date);

CREATE UNIQUE INDEX ux_expenses_monthly_utility
  ON expenses(account_id, object_id, period_month, source)
  WHERE source = 'UTILITY_RECURRING';
