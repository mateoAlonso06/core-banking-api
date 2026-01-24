package com.banking.system.transaction.application.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositMoneyCommand(
        UUID accountId,
        BigDecimal amount,
        String currency
) {
}
