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