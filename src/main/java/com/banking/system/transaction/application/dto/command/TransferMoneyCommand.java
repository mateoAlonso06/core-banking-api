package com.banking.system.transaction.application.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyCommand(
        UUID fromAccountId,
        String toAlias,
        String toAccountNumber,
        BigDecimal amount,
        String currency,
        BigDecimal feeAmount,
        String feeCurrency,
        String description,
        UUID idempotencyKey
) {
}
