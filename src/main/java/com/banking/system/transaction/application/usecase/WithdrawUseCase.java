package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.command.WithdrawMoneyCommand;
import com.banking.system.transaction.application.dto.receipt.TransactionReceipt;

import java.util.UUID;

public interface WithdrawUseCase {
    TransactionReceipt withdraw(WithdrawMoneyCommand command, UUID accountId, UUID userId);
}
