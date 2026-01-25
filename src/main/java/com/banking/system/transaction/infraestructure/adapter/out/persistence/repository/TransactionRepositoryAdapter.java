package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.common.infraestructure.mapper.PageMapper;
import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import com.banking.system.transaction.infraestructure.adapter.out.mapper.TransactionJpaEntityMapper;
import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransactionJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {
    private final SpringDataTransactionRepository transactionJpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity txJpaEntity = TransactionJpaEntity.builder()
                .accountId(transaction.getAccountId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount().getValue())
                .currency(transaction.getAmount().getCurrency().code())
                .balanceAfter(transaction.getBalanceAfter().getValue())
                .description(transaction.getDescription().value())
                .referenceNumber(transaction.getReferenceNumber().value())
                .status(transaction.getStatus())
                .executedAt(transaction.getExecutedAt())
                .build();

        TransactionJpaEntity txJpaEntitySaved = transactionJpaRepository.save(txJpaEntity);

        return TransactionJpaEntityMapper.toDomainEntity(txJpaEntitySaved);
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        var transactionJpaEntity = transactionJpaRepository.findById(transactionId);
        return transactionJpaEntity
                .map(TransactionJpaEntityMapper::toDomainEntity);
    }

    @Override
    public PagedResult<Transaction> findAllTransactionsByAccountId(PageRequest pageRequest, UUID accountId) {
        var pageable = PageMapper.toPageable(pageRequest);
        var page = transactionJpaRepository.findAllByAccountId(accountId, pageable);
        return PageMapper.toPagedResult(page, TransactionJpaEntityMapper::toDomainEntity);
    }

    @Override
    public void deleteTransaction(UUID transactionId) {

    }
}
