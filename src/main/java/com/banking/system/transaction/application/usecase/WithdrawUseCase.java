package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.command.WithdrawMoneyCommand;

import java.util.UUID;

public interface WithdrawUseCase {
    void withdraw(WithdrawMoneyCommand command, UUID accountId, UUID userId);
}
