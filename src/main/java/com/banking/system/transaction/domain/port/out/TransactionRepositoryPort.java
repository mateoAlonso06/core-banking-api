package com.banking.system.transaction.domain.port.out;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.transaction.domain.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    PagedResult<Transaction> findAllTransactionsByAccountId(PageRequest pageRequest, UUID accountId);

    PagedResult<Transaction> findALlByAccountIds(List<UUID> accountIds, PageRequest pageRequest);
}
