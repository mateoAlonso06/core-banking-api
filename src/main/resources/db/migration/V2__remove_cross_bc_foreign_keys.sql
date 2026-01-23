-- ============================================================================
-- V2__remove_cross_bc_foreign_keys.sql
-- Remove foreign keys between different bounded contexts
-- ============================================================================
--
-- This migration removes FK constraints that cross bounded context boundaries.
-- See ADR-004 for the architectural decision and rationale.
--
-- Bounded Contexts:
--   - Auth: users, refresh_tokens
--   - Customer: customers
--   - Account: accounts, account_holds
--   - Transaction: transactions, transfers
--   - Audit: audit_logs
--
-- FKs being removed (cross-BC):
--   - customers.user_id -> users (Customer -> Auth)
--   - accounts.customer_id -> customers (Account -> Customer)
--   - transactions.account_id -> accounts (Transaction -> Account)
--   - transfers.source_account_id -> accounts (Transaction -> Account)
--   - transfers.destination_account_id -> accounts (Transaction -> Account)
--   - audit_logs.user_id -> users (Audit -> Auth)
--
-- FKs being kept (same BC):
--   - refresh_tokens.user_id -> users (Auth -> Auth)
--   - account_holds.account_id -> accounts (Account -> Account)
--   - transfers.debit_transaction_id -> transactions (Transaction -> Transaction)
--   - transfers.credit_transaction_id -> transactions (Transaction -> Transaction)
--   - transfers.fee_transaction_id -> transactions (Transaction -> Transaction)
-- ============================================================================

-- Customer -> Auth
ALTER TABLE customers
    DROP CONSTRAINT customers_user_id_fkey;

-- Account -> Customer
ALTER TABLE accounts
    DROP CONSTRAINT accounts_customer_id_fkey;

-- Transaction -> Account
ALTER TABLE transactions
    DROP CONSTRAINT transactions_account_id_fkey;

-- Transfer -> Account (source)
ALTER TABLE transfers
    DROP CONSTRAINT transfers_source_account_id_fkey;

-- Transfer -> Account (destination)
ALTER TABLE transfers
    DROP CONSTRAINT transfers_destination_account_id_fkey;

-- Audit -> Auth
ALTER TABLE audit_logs
    DROP CONSTRAINT audit_logs_user_id_fkey;

-- ============================================================================
-- Note: Indexes on these columns are preserved for query performance.
-- The columns remain as UUID type for referential integrity at application level.
-- ============================================================================