package com.banking.system.account.application.dto.result;

import com.banking.system.account.domain.model.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResult(
        UUID id,
        UUID customerId,
        String accountNumber,
        String alias,
        String accountType,
        String currency,
        BigDecimal balance,
        BigDecimal availableBalance
) {
    public static AccountResult fromDomain(Account account) {
        return new AccountResult(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber().value(),
                account.getAlias().value(),
                account.getAccountType().name(),
                account.getCurrency().code(),
                account.getBalance().getValue(),
                account.getAvailableBalance().getValue()
        );
    }
}