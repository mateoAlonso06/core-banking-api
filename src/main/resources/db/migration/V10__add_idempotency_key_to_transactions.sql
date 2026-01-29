ALTER TABLE transactions ADD COLUMN idempotency_key VARCHAR(255);

CREATE UNIQUE INDEX idx_transaction_idempotency_key ON transactions(idempotency_key) WHERE idempotency_key IS NOT NULL;
