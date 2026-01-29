package com.banking.system.transaction.application.mapper;

import com.banking.system.transaction.application.dto.result.TransferResult;
import com.banking.system.transaction.domain.model.Transfer;

public class TransferDomainMapper {
    public static TransferResult toResult(Transfer transfer) {
        return new TransferResult(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getCategory(),
                transfer.getAmount().getValue(),
                transfer.getCreditTransactionId(),
                transfer.getDebitTransactionId(),
                transfer.getFeeTransactionId()
        );
    }
}
