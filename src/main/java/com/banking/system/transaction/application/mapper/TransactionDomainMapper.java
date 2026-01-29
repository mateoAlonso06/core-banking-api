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
                null, // amountFee - not applicable for individual transactions
                transaction.getAmount().getCurrency().code(),
                transaction.getBalanceAfter() != null ? transaction.getBalanceAfter().getValue() : null,
                transaction.getDescription() != null ? transaction.getDescription().value() : null,
                transaction.getReferenceNumber().value(),
                transaction.getExecutedAt().toString(),
                transaction.getStatus().name()
        );
    }
}
