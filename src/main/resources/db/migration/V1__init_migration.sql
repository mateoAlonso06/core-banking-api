-- ============================================================================
-- V1__init_migration.sql
-- Sistema Bancario - Schema Inicial
-- ============================================================================

-- ============================================================================
-- 1. USUARIOS Y AUTENTICACIÓN
-- ============================================================================

-- Usuario del sistema (puede ser cliente o empleado del banco)
CREATE TABLE users
(
    id                    UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    email                 VARCHAR(255) UNIQUE NOT NULL,
    password_hash         VARCHAR(255)        NOT NULL,
    status                VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, PENDING_VERIFICATION
    role                  VARCHAR(50)         NOT NULL,                  -- CUSTOMER, ADMIN, BRANCH_MANAGER
    created_at            TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP           NOT NULL DEFAULT NOW(),
    last_login_at         TIMESTAMP,
    failed_login_attempts INT                          DEFAULT 0,
    locked_until          TIMESTAMP
);

-- Tokens de refresh (OAuth2)
CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    user_id    UUID                NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP           NOT NULL,
    revoked    BOOLEAN                      DEFAULT FALSE,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);

-- ============================================================================
-- 2. CLIENTES
-- ============================================================================

-- Cliente del banco (persona física)
CREATE TABLE customers
(
    id              UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    user_id         UUID UNIQUE        NOT NULL REFERENCES users (id) ON DELETE RESTRICT,

    -- Datos personales
    first_name      VARCHAR(100)       NOT NULL,
    last_name       VARCHAR(100)       NOT NULL,
    document_type   VARCHAR(20)        NOT NULL,                   -- DNI, PASSPORT, CUIT
    document_number VARCHAR(50) UNIQUE NOT NULL,
    birth_date      DATE               NOT NULL,

    -- Contacto
    phone           VARCHAR(50),
    address         VARCHAR(255)       NOT NULL,
    city            VARCHAR(100)       NOT NULL,
    country         VARCHAR(2)         NOT NULL DEFAULT 'AR',      -- ISO 3166-1 alpha-2

    -- Metadata
    customer_since  DATE               NOT NULL DEFAULT CURRENT_DATE,
    kyc_status      VARCHAR(20)                 DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    risk_level      VARCHAR(20)                 DEFAULT 'LOW',     -- LOW, MEDIUM, HIGH

    created_at      TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP          NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_user ON customers (user_id);
CREATE INDEX idx_customers_document ON customers (document_number);

-- ============================================================================
-- 3. CUENTAS
-- ============================================================================

-- Cuenta bancaria
CREATE TABLE accounts
(
    id                     UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    customer_id            UUID               NOT NULL REFERENCES customers (id) ON DELETE RESTRICT,

    -- Identificación
    account_number         VARCHAR(22) UNIQUE NOT NULL,                -- CBU/CVU argentino (22 dígitos)
    alias                  VARCHAR(50) UNIQUE NOT NULL,                -- Alias (ej: "juan.pizza.ahorro")

    -- Tipo y estado
    account_type           VARCHAR(20)        NOT NULL,
    currency               VARCHAR(3)         NOT NULL DEFAULT 'ARS',
    status                 VARCHAR(20)        NOT NULL DEFAULT 'ACTIVE',

    -- Saldos (usar NUMERIC para dinero, NUNCA FLOAT)
    balance                NUMERIC(19, 4)     NOT NULL DEFAULT 0.0000,
    available_balance      NUMERIC(19, 4)     NOT NULL DEFAULT 0.0000, -- balance - holds

    -- Límites (nullable - el código asignará defaults si no se especifican)
    daily_transfer_limit   NUMERIC(19, 4),
    monthly_transfer_limit NUMERIC(19, 4),

    -- Metadata
    opened_at              DATE               NOT NULL DEFAULT CURRENT_DATE,
    closed_at              DATE,

    created_at             TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP          NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_available_balance_valid CHECK (available_balance <= balance),
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CHECKING', 'INVESTMENT')),
    CONSTRAINT chk_currency CHECK (currency IN ('ARS', 'USD')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
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

-- ============================================================================
-- 4. TRANSACCIONES
-- ============================================================================

-- Transacción individual (entrada en el ledger)
CREATE TABLE transactions
(
    id                     UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    account_id             UUID               NOT NULL REFERENCES accounts (id) ON DELETE RESTRICT,

    -- Datos de la transacción
    transaction_type       VARCHAR(20)        NOT NULL,
    amount                 NUMERIC(19, 4)     NOT NULL,
    currency               VARCHAR(3)         NOT NULL,

    -- Balance después de la transacción (para auditoría)
    balance_after          NUMERIC(19, 4)     NOT NULL,

    -- Descripción
    description            VARCHAR(500),
    reference_number       VARCHAR(100), -- Número de referencia externo

    -- Relacionado (si es transferencia)
    related_transaction_id UUID REFERENCES transactions (id),

    -- Metadata
    status                 VARCHAR(20)        NOT NULL DEFAULT 'COMPLETED',
    executed_at            TIMESTAMP          NOT NULL DEFAULT NOW(),
    created_at             TIMESTAMP          NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN', 'FEE', 'INTEREST', 'REVERSAL')),
    CONSTRAINT chk_transaction_currency CHECK (currency IN ('ARS', 'USD')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'))
);

CREATE INDEX idx_transactions_account ON transactions (account_id);
CREATE INDEX idx_transactions_executed_at ON transactions (executed_at);
CREATE INDEX idx_transactions_type ON transactions (transaction_type);
CREATE INDEX idx_transactions_status ON transactions (status);

-- ============================================================================
-- 5. TRANSFERENCIAS
-- ============================================================================

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
    currency               VARCHAR(3)         NOT NULL,
    description            VARCHAR(500),

    -- Comisión (si aplica)
    fee_amount             NUMERIC(19, 4)              DEFAULT 0,
    fee_transaction_id     UUID REFERENCES transactions (id),

    -- Estado
    status                 VARCHAR(20)        NOT NULL DEFAULT 'COMPLETED',

    -- Idempotencia (evitar transferencias duplicadas)
    idempotency_key        VARCHAR(100) UNIQUE, -- Cliente puede enviar su propio UUID

    -- Metadata
    executed_at            TIMESTAMP          NOT NULL DEFAULT NOW(),
    created_at             TIMESTAMP          NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT chk_different_accounts CHECK (source_account_id != destination_account_id),
    CONSTRAINT chk_transfer_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transfer_currency CHECK (currency IN ('ARS', 'USD')),
    CONSTRAINT chk_transfer_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'))
);

CREATE INDEX idx_transfers_source ON transfers (source_account_id);
CREATE INDEX idx_transfers_destination ON transfers (destination_account_id);
CREATE INDEX idx_transfers_executed_at ON transfers (executed_at);
CREATE INDEX idx_transfers_idempotency ON transfers (idempotency_key);

-- ============================================================================
-- 6. AUDITORÍA
-- ============================================================================

-- Log de auditoría (quién hizo qué y cuándo)
CREATE TABLE audit_logs
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    -- Quién
    user_id     UUID         REFERENCES users (id) ON DELETE SET NULL,
    user_email  VARCHAR(255),          -- Desnormalizado por si se borra el usuario
    user_role   VARCHAR(50),

    -- Qué
    entity_type VARCHAR(100) NOT NULL, -- 'Account', 'Transaction', 'Customer'
    entity_id   UUID         NOT NULL,
    action      VARCHAR(50)  NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE', 'BLOCK'

    -- Datos del cambio
    old_values  JSONB,                 -- Estado anterior
    new_values  JSONB,                 -- Estado nuevo

    -- Metadata
    ip_address  VARCHAR(45),           -- IPv4 o IPv6
    user_agent  VARCHAR(500),
    timestamp   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user ON audit_logs (user_id);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs (timestamp);