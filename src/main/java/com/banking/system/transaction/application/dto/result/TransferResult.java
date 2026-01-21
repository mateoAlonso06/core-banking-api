package com.banking.system.transaction.application.dto.result;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResult(
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        BigDecimal sourceBalanceAfter,
        BigDecimal targetBalanceAfter
) {
}
