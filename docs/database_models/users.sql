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