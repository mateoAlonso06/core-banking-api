package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.DepositCommand;

public interface DepositUseCase {
    void deposit(DepositCommand command);
}
