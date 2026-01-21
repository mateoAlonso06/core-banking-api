package com.banking.system.transaction.domain.port.out;

import com.banking.system.transaction.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);

    Transaction findById(UUID transactionId);

    List<Transaction> findAll(int size, int page);

    void deleteTransaction(UUID transactionId);

    Transaction updateTransaction(Transaction transaction);
}
