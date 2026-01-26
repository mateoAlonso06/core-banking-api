-- ============================================================================
-- V5__add_roles_permissions_tables.sql
-- Core Banking System - Roles and Permissions Tables
-- ============================================================================

-- ============================================================================
-- 1. PERMISSIONS TABLE
-- ============================================================================

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

-- ============================================================================
-- 2. ROLES TABLE
-- ============================================================================

CREATE TABLE roles
(
    id          UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    name        VARCHAR(50) UNIQUE  NOT NULL,
    description VARCHAR(255)        NOT NULL,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ============================================================================
-- 3. ROLE-PERMISSIONS RELATIONSHIP (MANY-TO-MANY)
-- ============================================================================

CREATE TABLE role_permissions
(
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);
