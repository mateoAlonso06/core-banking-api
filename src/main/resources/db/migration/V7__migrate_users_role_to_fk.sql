-- ============================================================================
-- V7__migrate_users_role_to_fk.sql
-- Core Banking System - Migrate users.role from VARCHAR to FK
-- ============================================================================

-- ============================================================================
-- 1. ADD NEW COLUMN role_id
-- ============================================================================

ALTER TABLE users ADD COLUMN role_id UUID;

-- ============================================================================
-- 2. MIGRATE EXISTING DATA
-- ============================================================================

-- Map existing VARCHAR roles to the new role_id FK
UPDATE users u
SET role_id = r.id
FROM roles r
WHERE r.name = u.role;

-- ============================================================================
-- 3. ADD CONSTRAINTS
-- ============================================================================

-- Make role_id NOT NULL after migration
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE users
    ADD CONSTRAINT fk_users_role
    FOREIGN KEY (role_id) REFERENCES roles (id);

-- Create index for better query performance
CREATE INDEX idx_users_role ON users (role_id);

-- ============================================================================
-- 4. DROP OLD COLUMN
-- ============================================================================

-- Remove the old VARCHAR role column
ALTER TABLE users DROP COLUMN role;
