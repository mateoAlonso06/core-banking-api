package com.banking.system.account.application.dto.result;

import com.banking.system.account.domain.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountBalanceResult(
        UUID accountId,
        BigDecimal balance,
        BigDecimal availableBalance,
        String currency,
        Instant lastUpdated
) {
    public static AccountBalanceResult fromDomain(Account account) {
        return new AccountBalanceResult(
                account.getId(),
                account.getBalance().getValue(),
                account.getAvailableBalance().getValue(),
                account.getCurrency().code(),
                account.getUpdatedAt()
        );
    }
}
