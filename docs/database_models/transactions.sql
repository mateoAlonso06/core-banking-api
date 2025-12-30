-- Tipos de transacción
CREATE TYPE transaction_type AS ENUM (
    'DEPOSIT',          -- Depósito
    'WITHDRAWAL',       -- Extracción
    'TRANSFER_OUT',     -- Transferencia saliente
    'TRANSFER_IN',      -- Transferencia entrante
    'FEE',              -- Comisión
    'INTEREST',         -- Interés
    'REVERSAL'          -- Reversión de transacción
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED',
    'REVERSED'
);

-- Transacción individual (entrada en el ledger)
CREATE TABLE transactions
(
    id                     UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    account_id             UUID               NOT NULL REFERENCES accounts (id) ON DELETE RESTRICT,

    -- Datos de la transacción
    transaction_type       transaction_type   NOT NULL,
    amount                 NUMERIC(19, 4)     NOT NULL,
    currency               currency_code      NOT NULL,

    -- Balance después de la transacción (para auditoría)
    balance_after          NUMERIC(19, 4)     NOT NULL,

    -- Descripción
    description            VARCHAR(500),
    reference_number       VARCHAR(100), -- Número de referencia externo

    -- Relacionado (si es transferencia)
    related_transaction_id UUID REFERENCES transactions (id),

    -- Metadata
    status                 transaction_status NOT NULL DEFAULT 'COMPLETED',
    executed_at            TIMESTAMP          NOT NULL DEFAULT NOW(),
    created_at             TIMESTAMP          NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_account ON transactions (account_id);
CREATE INDEX idx_transactions_executed_at ON transactions (executed_at);
CREATE INDEX idx_transactions_type ON transactions (transaction_type);
CREATE INDEX idx_transactions_status ON transactions (status);

-- Transferencia (agrupa 2 transacciones: débito + crédito)
CREATE TABLE transfers
(
    id                     UUID PRIMARY KEY            DEFAULT gen_random_uuid(),

    -- Cuentas involucradas
    source_account_id      UUID               NOT NULL REFERENCES accounts (id) ON DELETE RESTRICT,
    destination_account_id UUID               NOT NULL REFERENCES accounts (id) ON DELETE RESTRICT,

    -- Transacciones relacionadas (doble entrada contable)
    debit_transaction_id   UUID UNIQUE        NOT NULL REFERENCES transactions (id),
    credit_transaction_id  UUID UNIQUE        NOT NULL REFERENCES transactions (id),

    -- Datos de la transferencia
    amount                 NUMERIC(19, 4)     NOT NULL,
    currency               currency_code      NOT NULL,
    description            VARCHAR(500),

    -- Comisión (si aplica)
    fee_amount             NUMERIC(19, 4)              DEFAULT 0,
    fee_transaction_id     UUID REFERENCES transactions (id),

    -- Estado
    status                 transaction_status NOT NULL DEFAULT 'COMPLETED',

    -- Idempotencia (evitar transferencias duplicadas)
    idempotency_key        VARCHAR(100) UNIQUE, -- Cliente puede enviar su propio UUID

    -- Metadata
    executed_at            TIMESTAMP          NOT NULL DEFAULT NOW(),
    created_at             TIMESTAMP          NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_different_accounts CHECK (source_account_id != destination_account_id
) ,
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transfers_source ON transfers (source_account_id);
CREATE INDEX idx_transfers_destination ON transfers (destination_account_id);
CREATE INDEX idx_transfers_executed_at ON transfers (executed_at);
CREATE INDEX idx_transfers_idempotency ON transfers (idempotency_key);