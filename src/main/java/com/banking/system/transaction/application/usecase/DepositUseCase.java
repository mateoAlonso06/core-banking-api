package com.banking.system.transaction.application.usecase;

import com.banking.system.common.domain.Money;

import java.util.UUID;

public interface DepositUseCase {
    void deposit(UUID accountId, Money amount);
}
