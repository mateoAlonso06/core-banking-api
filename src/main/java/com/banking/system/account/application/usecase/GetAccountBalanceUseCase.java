package com.banking.system.account.application.usecase;

import com.banking.system.account.application.dto.result.AccountBalanceResult;

import java.util.UUID;

public interface GetAccountBalanceUseCase {
    AccountBalanceResult getBalance(UUID accountId, UUID userId);
}
