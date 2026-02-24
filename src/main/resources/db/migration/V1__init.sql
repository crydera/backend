
CREATE TABLE IF NOT EXISTS merchants (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email          STRING NOT NULL UNIQUE,
    password_hash  STRING NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS api_keys (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id  UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    key_hash     STRING NOT NULL UNIQUE,
    label        STRING,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at   TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_api_keys_merchant ON api_keys(merchant_id);

CREATE TABLE IF NOT EXISTS wallets (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id  UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    network      STRING NOT NULL,
    address      STRING NOT NULL,
    label        STRING,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (network, address)
);
CREATE INDEX IF NOT EXISTS idx_wallets_merchant ON wallets(merchant_id);

CREATE TABLE IF NOT EXISTS transactions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id     UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    type          STRING NOT NULL,
    status        STRING NOT NULL,
    amount        DECIMAL(38, 18) NOT NULL,
    currency      STRING NOT NULL,
    tx_hash       STRING,
    counterparty  STRING,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    confirmed_at  TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_tx_wallet ON transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_tx_hash ON transactions(tx_hash);

CREATE TABLE IF NOT EXISTS payments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id      UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    kind             STRING NOT NULL,
    network          STRING NOT NULL,
    address          STRING,
    amount           DECIMAL(38, 18) NOT NULL,
    currency         STRING NOT NULL,
    status           STRING NOT NULL,
    external_order_id STRING,
    callback_url     STRING,
    metadata         JSONB,
    expires_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    paid_at          TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_payments_merchant ON payments(merchant_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
