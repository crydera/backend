
CREATE TABLE IF NOT EXISTS processed_sidecar_events (
    event_id     UUID PRIMARY KEY,
    event_type   STRING NOT NULL,
    payment_id   UUID,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_processed_events_payment ON processed_sidecar_events(payment_id);

ALTER TABLE transactions ADD COLUMN IF NOT EXISTS payment_id UUID REFERENCES payments(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_tx_payment ON transactions(payment_id);

ALTER TABLE payments ADD COLUMN IF NOT EXISTS detected_tx_hash STRING;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS confirmations    INT;
