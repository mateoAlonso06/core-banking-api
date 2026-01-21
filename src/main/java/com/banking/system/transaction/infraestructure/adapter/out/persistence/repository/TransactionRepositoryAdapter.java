package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {
    private final SpringDataTransactionRepository transactionJpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return null;
    }

    @Override
    public Transaction findById(UUID transactionId) {
        return null;
    }

    @Override
    public List<Transaction> findAll(int size, int page) {
        return List.of();
    }

    @Override
    public void deleteTransaction(UUID transactionId) {

    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        return null;
    }
}
