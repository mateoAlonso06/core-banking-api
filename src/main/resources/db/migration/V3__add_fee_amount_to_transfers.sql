-- Add fee_amount column to transfers table
ALTER TABLE transfers ADD COLUMN fee_amount NUMERIC(19, 2);