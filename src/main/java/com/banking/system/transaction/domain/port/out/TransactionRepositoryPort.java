package com.banking.system.transaction.domain.port.out;

import com.banking.system.transaction.domain.model.Transaction;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
}
