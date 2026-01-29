package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.command.DepositMoneyCommand;
import com.banking.system.transaction.application.dto.receipt.TransactionReceipt;

import java.util.UUID;

public interface DepositUseCase {
    TransactionReceipt deposit(DepositMoneyCommand command, UUID accountId, UUID userId);
}
