-- ============================================================================
-- V6__seed_roles_permissions_data.sql
-- Core Banking System - Seed Roles and Permissions
-- ============================================================================

-- ============================================================================
-- 1. INSERT PERMISSIONS
-- ============================================================================

-- Customer module permissions
INSERT INTO permissions (code, description, module) VALUES
('CUSTOMER_VIEW_OWN', 'View own customer profile', 'CUSTOMER'),
('CUSTOMER_VIEW', 'View specific customer profile', 'CUSTOMER'),
('CUSTOMER_VIEW_ALL', 'List all customers', 'CUSTOMER'),
('KYC_APPROVE', 'Approve KYC verification', 'CUSTOMER'),
('KYC_REJECT', 'Reject KYC verification', 'CUSTOMER');

-- Account module permissions
INSERT INTO permissions (code, description, module) VALUES
('ACCOUNT_CREATE', 'Create bank account', 'ACCOUNT'),
('ACCOUNT_VIEW_OWN', 'View own accounts', 'ACCOUNT'),
('ACCOUNT_VIEW', 'View specific account', 'ACCOUNT'),
('ACCOUNT_VIEW_ALL', 'List all accounts', 'ACCOUNT'),
('ACCOUNT_BLOCK', 'Block account', 'ACCOUNT'),
('ACCOUNT_CLOSE', 'Close account', 'ACCOUNT');

-- Transaction module permissions
INSERT INTO permissions (code, description, module) VALUES
('TRANSACTION_DEPOSIT', 'Make deposits', 'TRANSACTION'),
('TRANSACTION_WITHDRAW', 'Make withdrawals', 'TRANSACTION'),
('TRANSACTION_TRANSFER', 'Make transfers', 'TRANSACTION'),
('TRANSACTION_VIEW_OWN', 'View own transactions', 'TRANSACTION'),
('TRANSACTION_VIEW', 'View specific transaction', 'TRANSACTION'),
('TRANSACTION_VIEW_ALL', 'View all transactions', 'TRANSACTION'),
('TRANSACTION_REVERSE', 'Reverse transaction', 'TRANSACTION');

-- Audit module permissions
INSERT INTO permissions (code, description, module) VALUES
('AUDIT_VIEW', 'View audit logs', 'AUDIT'),
('AUDIT_EXPORT', 'Export audit logs', 'AUDIT');

-- User management permissions
INSERT INTO permissions (code, description, module) VALUES
('USER_VIEW', 'View users', 'USER'),
('USER_BLOCK', 'Block user', 'USER'),
('USER_CHANGE_ROLE', 'Change user role', 'USER');

-- ============================================================================
-- 2. INSERT ROLES
-- ============================================================================

INSERT INTO roles (name, description) VALUES
('CUSTOMER', 'Regular bank customer with basic account access'),
('SUPPORT', 'Customer support representative with read access'),
('BRANCH_MANAGER', 'Branch manager with account control capabilities'),
('COMPLIANCE', 'Compliance officer for KYC and risk management'),
('AUDITOR', 'Internal auditor with read-only access to all data'),
('ADMIN', 'System administrator with full access');

-- ============================================================================
-- 3. ASSIGN PERMISSIONS TO ROLES
-- ============================================================================

-- CUSTOMER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CUSTOMER'
  AND p.code IN (
    'CUSTOMER_VIEW_OWN',
    'ACCOUNT_CREATE',
    'ACCOUNT_VIEW_OWN',
    'TRANSACTION_DEPOSIT',
    'TRANSACTION_WITHDRAW',
    'TRANSACTION_TRANSFER',
    'TRANSACTION_VIEW_OWN'
);

-- SUPPORT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'SUPPORT'
  AND p.code IN (
    'CUSTOMER_VIEW',
    'CUSTOMER_VIEW_ALL',
    'ACCOUNT_VIEW',
    'ACCOUNT_VIEW_ALL',
    'TRANSACTION_VIEW'
);

-- BRANCH_MANAGER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'BRANCH_MANAGER'
  AND p.code IN (
    'CUSTOMER_VIEW',
    'CUSTOMER_VIEW_ALL',
    'ACCOUNT_VIEW',
    'ACCOUNT_VIEW_ALL',
    'ACCOUNT_BLOCK',
    'ACCOUNT_CLOSE',
    'TRANSACTION_VIEW',
    'TRANSACTION_VIEW_ALL',
    'TRANSACTION_REVERSE',
    'USER_VIEW'
);

-- COMPLIANCE permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'COMPLIANCE'
  AND p.code IN (
    'CUSTOMER_VIEW',
    'CUSTOMER_VIEW_ALL',
    'KYC_APPROVE',
    'KYC_REJECT',
    'ACCOUNT_VIEW',
    'TRANSACTION_VIEW',
    'TRANSACTION_VIEW_ALL',
    'AUDIT_VIEW'
);

-- AUDITOR permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'AUDITOR'
  AND p.code IN (
    'CUSTOMER_VIEW',
    'CUSTOMER_VIEW_ALL',
    'ACCOUNT_VIEW',
    'ACCOUNT_VIEW_ALL',
    'TRANSACTION_VIEW',
    'TRANSACTION_VIEW_ALL',
    'AUDIT_VIEW',
    'AUDIT_EXPORT'
);

-- ADMIN permissions (ALL permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';
