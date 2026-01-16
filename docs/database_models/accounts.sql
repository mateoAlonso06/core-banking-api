-- Tipos de cuenta
CREATE TYPE account_type AS ENUM ('SAVINGS', 'CHECKING', 'INVESTMENT');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'BLOCKED', 'CLOSED');

-- Cuenta bancaria
CREATE TABLE accounts
(
    id                     UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    customer_id            UUID               NOT NULL REFERENCES customers (id) ON DELETE RESTRICT,

    -- Identificación
    account_number         VARCHAR(22) UNIQUE NOT NULL,                -- CBU/CVU argentino (22 dígitos)
    alias                  VARCHAR(50) UNIQUE,                         -- Alias opcional (ej: "juan.pizza.ahorro")

    -- Tipo y estado
    account_type           account_type       NOT NULL,
    currency               VARCHAR(3)      NOT NULL DEFAULT 'ARS', -- ISO 4217
    status                 account_status     NOT NULL DEFAULT 'ACTIVE',

    -- Saldos (usar NUMERIC para dinero, NUNCA FLOAT)
    balance                NUMERIC(19, 4)     NOT NULL DEFAULT 0.0000,
    available_balance      NUMERIC(19, 4)     NOT NULL DEFAULT 0.0000, -- balance - holds

    -- Límites
    daily_transfer_limit   NUMERIC(19, 4),
    monthly_transfer_limit NUMERIC(19, 4),

    -- Metadata
    opened_at              DATE               NOT NULL DEFAULT CURRENT_DATE,
    closed_at              DATE,

    created_at             TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP          NOT NULL DEFAULT NOW(),

    -- Constraint: saldo no puede ser negativo
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_available_balance_valid CHECK (available_balance <= balance)
);

CREATE INDEX idx_accounts_customer ON accounts (customer_id);
CREATE INDEX idx_accounts_number ON accounts (account_number);
CREATE INDEX idx_accounts_alias ON accounts (alias);
CREATE INDEX idx_accounts_status ON accounts (status);

-- Retenciones temporales (ej: autorización de pago pendiente)
CREATE TABLE account_holds
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    account_id UUID           NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    amount     NUMERIC(19, 4) NOT NULL,
    reason     VARCHAR(255),
    expires_at TIMESTAMP      NOT NULL,
    released   BOOLEAN                 DEFAULT FALSE,
    created_at TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_holds_account ON account_holds (account_id);