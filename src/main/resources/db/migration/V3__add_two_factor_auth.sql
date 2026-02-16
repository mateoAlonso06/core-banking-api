-- Add two-factor authentication support

-- Add two_factor_enabled column to users table
ALTER TABLE users
ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- Create two_factor_codes table
CREATE TABLE two_factor_codes (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code          VARCHAR(6)   NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMP    NOT NULL,
    used          BOOLEAN      NOT NULL DEFAULT FALSE,
    attempts      INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Create indexes for efficient queries
CREATE INDEX idx_2fa_session_token ON two_factor_codes(session_token);
CREATE INDEX idx_2fa_user_id ON two_factor_codes(user_id);
CREATE INDEX idx_2fa_expires_at ON two_factor_codes(expires_at);
