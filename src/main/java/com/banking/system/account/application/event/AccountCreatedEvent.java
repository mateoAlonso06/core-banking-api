package com.banking.system.account.application.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID accountId,
        UUID customerId,
        UUID userId,
        String currency,
        BigDecimal initialBalance,
        String accountNumber,
        String alias,
        String accountType,
        LocalDate openedAt
) {
}
