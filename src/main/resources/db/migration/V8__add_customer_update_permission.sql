-- ============================================================================
-- V8__add_customer_update_permission.sql
-- Add CUSTOMER_UPDATE permission and assign to CUSTOMER and ADMIN roles
-- ============================================================================

INSERT INTO permissions (code, description, module) VALUES
('CUSTOMER_UPDATE', 'Update own customer profile', 'CUSTOMER');

-- Assign to CUSTOMER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CUSTOMER' AND p.code = 'CUSTOMER_UPDATE';

-- ADMIN already gets all permissions via V6 wildcard insert,
-- but since that ran at V6 time, we need to explicitly add the new one.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.code = 'CUSTOMER_UPDATE';
