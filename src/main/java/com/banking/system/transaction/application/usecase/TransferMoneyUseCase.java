package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.command.TransferMoneyCommand;
import com.banking.system.transaction.application.dto.result.TransferResult;

import java.util.UUID;

public interface TransferMoneyUseCase {
    TransferResult transfer(TransferMoneyCommand command, UUID userId);
}
