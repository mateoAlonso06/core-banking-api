package com.banking.system.transaction.application.mapper;

import com.banking.system.transaction.application.dto.result.TransactionResult;
import com.banking.system.transaction.domain.model.Transaction;

public class TransactionDomainMapper {
    public static TransactionResult toResult(Transaction transaction) {
        return new TransactionResult(
                transaction.getId().toString(),
                transaction.getAccountId().toString(),
                transaction.getTransactionType().name(),
                transaction.getAmount().getValue(),
                transaction.getAmount().getCurrency().code(),
                transaction.getBalanceAfter().getValue(),
                transaction.getDescription().toString(),
                transaction.getReferenceNumber().toString(),
                transaction.getExecutedAt().toString(),
                transaction.getStatus().toString()
        );
    }
}
