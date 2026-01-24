-- Convert PostgreSQL ENUMs to VARCHAR for JPA @Enumerated(EnumType.STRING) compatibility

-- =============================================
-- ACCOUNTS TABLE
-- =============================================

-- Convert account_type column
ALTER TABLE accounts
    ALTER COLUMN account_type TYPE VARCHAR(20) USING account_type::text;

-- Remove default before converting status column (default depends on enum type)
ALTER TABLE accounts
    ALTER COLUMN status DROP DEFAULT;

-- Convert status column
ALTER TABLE accounts
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- Re-add the default value as VARCHAR
ALTER TABLE accounts
    ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- Drop the ENUM types (no longer needed)
DROP TYPE account_type;
DROP TYPE account_status;

-- =============================================
-- TRANSACTIONS TABLE
-- =============================================

-- Convert transaction_type column
ALTER TABLE transactions
    ALTER COLUMN transaction_type TYPE VARCHAR(20) USING transaction_type::text;

-- Remove default before converting status column (default depends on enum type)
ALTER TABLE transactions
    ALTER COLUMN status DROP DEFAULT;

-- Convert status column
ALTER TABLE transactions
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- Re-add the default value as VARCHAR
ALTER TABLE transactions
    ALTER COLUMN status SET DEFAULT 'PENDING';

-- Drop the ENUM types (no longer needed)
DROP TYPE transaction_type;
DROP TYPE transaction_status;

-- Add new column with TIMESTAMP WITH TIME ZONE type
ALTER TABLE customers
    ALTER COLUMN kyc_verified_at TYPE TIMESTAMP WITH TIME ZONE;
