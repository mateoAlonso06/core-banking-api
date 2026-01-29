package com.banking.system.transaction.application.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawMoneyCommand(
        BigDecimal amount,
        String currency,
        UUID idempotencyKey
) {
}
