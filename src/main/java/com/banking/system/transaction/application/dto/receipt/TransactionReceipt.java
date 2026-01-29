package com.banking.system.transaction.application.dto.receipt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Receipt returned after executing a deposit or withdrawal operation.
 * Optimized for displaying transaction confirmation/voucher to the user.
 */
public record TransactionReceipt(
        UUID transactionId,
        String referenceNumber,
        String operationType,
        BigDecimal amount,
        String currency,
        BigDecimal balanceAfter,
        String accountNumber,
        String accountAlias,
        String description,
        String status,
        Instant executedAt
) {
}