package com.banking.system.transaction.application.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyCommand(
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        String currency,
        BigDecimal feeAmount,
        String feeCurrency,
        String description,
        UUID idempotencyKey
) {
}
