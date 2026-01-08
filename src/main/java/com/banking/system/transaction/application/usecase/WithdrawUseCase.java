package com.banking.system.transaction.application.usecase;

import com.banking.system.common.domain.Money;

import java.util.UUID;

public interface WithdrawUseCase {
    void withdraw(UUID accountId, Money amount);
}
