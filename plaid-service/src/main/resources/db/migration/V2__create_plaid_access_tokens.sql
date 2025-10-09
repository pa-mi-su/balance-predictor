-- Plaid Access Tokens (for sandbox/production tokens)
CREATE TABLE IF NOT EXISTS plaid_access_tokens (
  id            BIGSERIAL PRIMARY KEY,
  user_id       BIGINT       NOT NULL,
  access_token  TEXT         NOT NULL,
  item_id       TEXT         NOT NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Enforce one access token per user (latest replaces old)
CREATE UNIQUE INDEX IF NOT EXISTS uq_plaid_access_tokens_user
  ON plaid_access_tokens(user_id);

-- Optional: quick lookup by item_id (useful for audits)
CREATE INDEX IF NOT EXISTS idx_plaid_access_tokens_item
  ON plaid_access_tokens(item_id);
