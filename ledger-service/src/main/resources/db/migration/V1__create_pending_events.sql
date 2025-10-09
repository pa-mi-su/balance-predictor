CREATE TABLE IF NOT EXISTS pending_events (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT            NOT NULL,
  event_date   DATE              NOT NULL,
  amount       NUMERIC(19,2)     NOT NULL,
  description  VARCHAR(255)      NOT NULL,
  created_at   TIMESTAMP         NOT NULL DEFAULT NOW()
);

-- Fast lookups by user/date
CREATE INDEX IF NOT EXISTS idx_pending_events_user_date
  ON pending_events(user_id, event_date);

-- Prevent exact duplicates from being inserted (simple idempotency)
CREATE UNIQUE INDEX IF NOT EXISTS uq_pending_events_natural
  ON pending_events(user_id, event_date, amount, description);
