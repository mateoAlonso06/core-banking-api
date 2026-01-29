package com.banking.system.transaction.application.dto.result;

import java.math.BigDecimal;

public record TransactionResult(
        String id,
        String accountId,
        String transactionType,
        BigDecimal amount,
        BigDecimal amountFee,
        String currency,
        BigDecimal balanceAfter,
        String description,
        String referenceNumber,
        String executedAt,
        String status
) {
}
