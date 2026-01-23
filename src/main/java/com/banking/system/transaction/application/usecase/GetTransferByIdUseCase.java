package com.banking.system.transaction.application.usecase;

import com.banking.system.transaction.application.dto.result.TransferResult;

import java.util.UUID;

public interface GetTransferByIdUseCase {
    TransferResult findById(UUID transferId);
}
