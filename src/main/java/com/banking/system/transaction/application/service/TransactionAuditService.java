package com.banking.system.transaction.application.service;

import com.banking.system.transaction.domain.exception.notfound.TransactionNotFoundException;
import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionAuditService {
    private final TransactionRepositoryPort transactionRepositoryPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction registerTransactionAudit(Transaction transaction) {
        return transactionRepositoryPort.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void transactionCompleted(UUID transactionId) {
        Transaction tx = transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with ID " + transactionId + " not found"));

        tx.markCompleted();
        transactionRepositoryPort.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transactionFailed(UUID transactionId) {
        Transaction tx = transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with ID " + transactionId + " not found"));

        tx.markFailed();
        transactionRepositoryPort.save(tx);
    }
}
