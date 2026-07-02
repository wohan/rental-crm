ALTER TABLE users
  ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN email_verified_at TIMESTAMPTZ;

UPDATE users SET email_verified_at = created_at WHERE email_verified = TRUE AND email_verified_at IS NULL;

CREATE TABLE auth_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  type VARCHAR(40) NOT NULL,
  token_hash VARCHAR(128) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_auth_tokens_lookup ON auth_tokens(token_hash, type, used_at);
CREATE INDEX idx_auth_tokens_user_type ON auth_tokens(user_id, type);
