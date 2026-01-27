package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.result.TransactionResult;

import java.util.UUID;

public interface GetTransactionByIdUseCase {
    TransactionResult getTransactionById(UUID transactionId, UUID userId);
}
