package com.banking.system.transaction.application.dto.result;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResult(
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        UUID creditTransactionId,
        UUID debitTransactionId,
        UUID feeTransactionId
) {
}
