-- ============================================================================
-- V1__init_schema.sql
-- Core Banking System - Initial Schema (Consolidated)
-- ============================================================================
-- This consolidated migration represents the complete database schema.
-- It includes all changes from previous migrations (V1-V11).
-- ============================================================================

-- ============================================================================
-- 1. USERS & AUTHENTICATION
-- ============================================================================

-- System user (can be customer or bank employee)
CREATE TABLE users
(
    id                    UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    email                 VARCHAR(255) UNIQUE NOT NULL,
    password_hash         VARCHAR(255)        NOT NULL,
    status                VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, PENDING_VERIFICATION
    role_id               UUID                NOT NULL,                  -- FK to roles (added in V7)
    created_at            TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP           NOT NULL DEFAULT NOW(),
    last_login_at         TIMESTAMP,
    failed_login_attempts INT                          DEFAULT 0,
    locked_until          TIMESTAMP
);

-- Refresh tokens (OAuth2)
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

-- Email verification tokens (V9)
CREATE TABLE email_verification_tokens
(
    id         UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    user_id    UUID                NOT NULL REFERENCES users (id),
    token      VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP           NOT NULL,
    used       BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_verification_token ON email_verification_tokens (token);

-- ============================================================================
-- 2. ROLES & PERMISSIONS (V5)
-- ============================================================================

-- Permissions table
CREATE TABLE permissions
(
    id          UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    code        VARCHAR(50) UNIQUE  NOT NULL,
    description VARCHAR(255)        NOT NULL,
    module      VARCHAR(50)         NOT NULL,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_permissions_module ON permissions (module);
CREATE INDEX idx_permissions_code ON permissions (code);

-- Roles table
CREATE TABLE roles
(
    id          UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    name        VARCHAR(50) UNIQUE  NOT NULL,
    description VARCHAR(255)        NOT NULL,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- Role-Permissions relationship (many-to-many)
CREATE TABLE role_permissions
(
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);

-- Add foreign key from users to roles (V7)
ALTER TABLE users
    ADD CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (id);

CREATE INDEX idx_users_role ON users (role_id);

-- ============================================================================
-- 3. CUSTOMERS
-- ============================================================================

-- Bank customer (individual)
-- Note: No FK to users.id (removed in V2 for bounded context separation)
CREATE TABLE customers
(
    id              UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    user_id         UUID UNIQUE        NOT NULL,

    -- Personal data
    first_name      VARCHAR(100)       NOT NULL,
    last_name       VARCHAR(100)       NOT NULL,
    document_type   VARCHAR(20)        NOT NULL,                   -- DNI, PASSPORT, CUIT
    document_number VARCHAR(50) UNIQUE NOT NULL,
    birth_date      DATE               NOT NULL,

    -- Contact
    phone           VARCHAR(20),
    address         VARCHAR(255),
    city            VARCHAR(100),
    country         VARCHAR(2)                  DEFAULT 'AR',      -- ISO 3166-1 alpha-2

    -- Metadata
    customer_since  DATE               NOT NULL DEFAULT CURRENT_DATE,
    kyc_status      VARCHAR(20)                 DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    kyc_verified_at TIMESTAMP WITH TIME ZONE,                      -- Added in V4
    risk_level      VARCHAR(20)                 DEFAULT 'LOW',     -- LOW, MEDIUM, HIGH

    created_at      TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP          NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_user ON customers (user_id);
CREATE INDEX idx_customers_document ON customers (document_number);

-- ============================================================================
-- 4. ACCOUNTS
-- ============================================================================

-- Bank account
-- Note: No FK to customers.id (removed in V2 for bounded context separation)
-- Note: ENUMs converted to VARCHAR in V4 for JPA compatibility
CREATE TABLE accounts
(
    id                     UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    customer_id            UUID               NOT NULL,

    -- Identification
    account_number         VARCHAR(22) UNIQUE NOT NULL,                -- CBU/CVU (22 digits)
    alias                  VARCHAR(50) UNIQUE,                         -- Optional alias (e.g., "juan.pizza.savings")

    -- Type and status (VARCHAR instead of ENUM - V4)
    account_type           VARCHAR(20)        NOT NULL,                -- SAVINGS, CHECKING, INVESTMENT
    currency               VARCHAR(3)         NOT NULL DEFAULT 'ARS', -- ISO 4217
    status                 VARCHAR(20)        NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, CLOSED

    -- Balances (use NUMERIC for money, NEVER FLOAT)
    balance                NUMERIC(19, 2)     NOT NULL DEFAULT 0.00,
    available_balance      NUMERIC(19, 2)     NOT NULL DEFAULT 0.00, -- balance - holds

    -- Limits
    daily_transfer_limit   NUMERIC(19, 2),
    monthly_transfer_limit NUMERIC(19, 2),

    -- Metadata
    opened_at              DATE               NOT NULL DEFAULT CURRENT_DATE,
    closed_at              DATE,

    created_at             TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP          NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_available_balance_valid CHECK (available_balance <= balance)
);

CREATE INDEX idx_accounts_customer ON accounts (customer_id);
CREATE INDEX idx_accounts_number ON accounts (account_number);
CREATE INDEX idx_accounts_alias ON accounts (alias);
CREATE INDEX idx_accounts_status ON accounts (status);

-- Temporary holds (e.g., pending payment authorization)
CREATE TABLE account_holds
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    account_id UUID           NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    amount     NUMERIC(19, 2) NOT NULL,
    reason     VARCHAR(255),
    expires_at TIMESTAMP      NOT NULL,
    released   BOOLEAN                 DEFAULT FALSE,
    created_at TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_holds_account ON account_holds (account_id);

-- ============================================================================
-- 5. TRANSACTIONS
-- ============================================================================

-- Individual transaction (ledger entry)
-- Note: No FK to accounts.id (removed in V2 for bounded context separation)
-- Note: ENUMs converted to VARCHAR in V4 for JPA compatibility
CREATE TABLE transactions
(
    id               UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    account_id       UUID               NOT NULL,

    -- Transaction data (VARCHAR instead of ENUM - V4)
    transaction_type VARCHAR(20)        NOT NULL, -- DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN, FEE, INTEREST, REVERSAL
    amount           NUMERIC(19, 2)     NOT NULL,
    currency         VARCHAR(3)         NOT NULL,

    -- Balance after transaction (for audit)
    balance_after    NUMERIC(19, 2)     NOT NULL,

    -- Description
    description      VARCHAR(500),
    reference_number VARCHAR(100),

    -- Idempotency (V10)
    idempotency_key  VARCHAR(255),

    -- Metadata (VARCHAR instead of ENUM - V4)
    status           VARCHAR(20)        NOT NULL DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED, REVERSED
    executed_at      TIMESTAMP          NOT NULL DEFAULT NOW(),
    created_at       TIMESTAMP          NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tx_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_account ON transactions (account_id);
CREATE INDEX idx_transactions_executed_at ON transactions (executed_at);
CREATE INDEX idx_transactions_type ON transactions (transaction_type);
CREATE INDEX idx_transactions_status ON transactions (status);
CREATE UNIQUE INDEX idx_transaction_idempotency_key ON transactions (idempotency_key) WHERE idempotency_key IS NOT NULL;

-- ============================================================================
-- 6. TRANSFERS
-- ============================================================================

-- Transfer (groups 2 transactions: debit + credit)
-- Note: No FK to accounts.id (removed in V2 for bounded context separation)
CREATE TABLE transfers
(
    id                     UUID PRIMARY KEY        DEFAULT gen_random_uuid(),

    -- Accounts involved
    source_account_id      UUID           NOT NULL,
    destination_account_id UUID           NOT NULL,

    -- Related transactions (double-entry bookkeeping)
    debit_transaction_id   UUID UNIQUE    NOT NULL REFERENCES transactions (id),
    credit_transaction_id  UUID UNIQUE    NOT NULL REFERENCES transactions (id),

    -- Transfer data
    amount                 NUMERIC(19, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL,
    description            VARCHAR(255),

    -- Fee (if applicable)
    fee_transaction_id     UUID REFERENCES transactions (id),
    fee_amount             NUMERIC(19, 2),                              -- Added in V3

    -- Category (V11)
    category               VARCHAR(50)    NOT NULL DEFAULT 'BETWEEN_OWN_ACCOUNTS',

    -- Idempotency (prevent duplicate transfers)
    idempotency_key        VARCHAR(100) UNIQUE NOT NULL,

    -- Metadata
    executed_at            TIMESTAMP      NOT NULL DEFAULT NOW(),
    created_at             TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_different_accounts CHECK (source_account_id != destination_account_id),
    CONSTRAINT chk_transfer_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transfers_source ON transfers (source_account_id);
CREATE INDEX idx_transfers_destination ON transfers (destination_account_id);
CREATE INDEX idx_transfers_executed_at ON transfers (executed_at);
CREATE INDEX idx_transfers_idempotency ON transfers (idempotency_key);

-- ============================================================================
-- 7. AUDIT
-- ============================================================================

-- Audit log (who did what and when)
-- Note: No FK to users.id (removed in V2 for bounded context separation)
CREATE TABLE audit_logs
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    -- Who
    user_id     UUID,
    user_email  VARCHAR(255),          -- Denormalized in case user is deleted
    user_role   VARCHAR(50),

    -- What
    entity_type VARCHAR(100) NOT NULL, -- 'Account', 'Transaction', 'Customer'
    entity_id   UUID         NOT NULL,
    action      VARCHAR(50)  NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE', 'BLOCK'

    -- Change data
    old_values  JSONB,                 -- Previous state
    new_values  JSONB,                 -- New state

    -- Metadata
    ip_address  VARCHAR(45),           -- IPv4 or IPv6
    user_agent  VARCHAR(500),
    timestamp   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user ON audit_logs (user_id);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs (timestamp);