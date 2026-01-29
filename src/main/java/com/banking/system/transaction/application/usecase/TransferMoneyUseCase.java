package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.command.TransferMoneyCommand;
import com.banking.system.transaction.application.dto.receipt.TransferReceipt;

import java.util.UUID;

public interface TransferMoneyUseCase {
    TransferReceipt transfer(TransferMoneyCommand command, UUID userId);
}
