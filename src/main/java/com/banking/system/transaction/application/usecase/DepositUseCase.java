package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.command.DepositMoneyCommand;

import java.util.UUID;

public interface DepositUseCase {
    void deposit(DepositMoneyCommand command, UUID accountId, UUID userId);
}
