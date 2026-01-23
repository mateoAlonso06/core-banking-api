package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import com.banking.system.transaction.infraestructure.adapter.out.mapper.TransactionJpaEntityMapper;
import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransactionJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
        return null;
    }

    @Override
    public List<Transaction> findAll(int size, int page) {
        return List.of();
    }

    @Override
    public void deleteTransaction(UUID transactionId) {

    }
}
