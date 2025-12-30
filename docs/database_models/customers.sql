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
    phone           VARCHAR(20),
    address         VARCHAR(255),
    city            VARCHAR(100),
    country         VARCHAR(2)                  DEFAULT 'AR',      -- ISO 3166-1 alpha-2

    -- Metadata
    customer_since  DATE               NOT NULL DEFAULT CURRENT_DATE,
    kyc_status      VARCHAR(20)                 DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    risk_level      VARCHAR(20)                 DEFAULT 'LOW',     -- LOW, MEDIUM, HIGH

    created_at      TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP          NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_user ON customers (user_id);
CREATE INDEX idx_customers_document ON customers (document_number);