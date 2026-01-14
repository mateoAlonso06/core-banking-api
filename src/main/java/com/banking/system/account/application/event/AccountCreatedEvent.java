package com.banking.system.account.application.event;

import com.banking.system.account.domain.model.AccountAlias;
import com.banking.system.account.domain.model.AccountNumber;
import com.banking.system.account.domain.model.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID acconuntId,
        UUID customerId,
        UUID userId,
        String currency,
        BigDecimal initialBalance,
        AccountNumber accountNumber,
        AccountAlias alias,
        AccountType accountType,
        LocalDateTime openedAt
) {
}
