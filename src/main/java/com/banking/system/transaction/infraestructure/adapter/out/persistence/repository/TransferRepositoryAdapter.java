package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.transaction.domain.model.Transfer;
import com.banking.system.transaction.domain.port.out.TransferRepositoryPort;
import com.banking.system.transaction.infraestructure.adapter.out.mapper.TransferJpaEntityMapper;
import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransferJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepositoryPort {
    private final SpringDataTransferRepository transferJpaRepository;
    private final SpringDataTransactionRepository transactionJpaRepository;

    @Override
    public Transfer save(Transfer transfer) {
        var transactionOut = transactionJpaRepository.findById(transfer.getDebitTransactionId())
                .orElseThrow(() -> new IllegalStateException("Debit transaction not found"));
        var transactionIn = transactionJpaRepository.findById(transfer.getCreditTransactionId())
                .orElseThrow(() -> new IllegalStateException("Credit transaction not found"));

        TransactionJpaEntity transactionFee = null;

        if (transfer.getFeeAmount() != null) {
            transactionFee = transactionJpaRepository.findById(transfer.getFeeTransactionId())
                    .orElseThrow(() -> new IllegalStateException("Fee transaction not found"));
        }

        var transferJpaEntity = TransferJpaEntity.builder()
                .sourceAccountId(transfer.getSourceAccountId())
                .destinationAccountId(transfer.getDestinationAccountId())
                .debitTransaction(transactionOut)
                .creditTransaction(transactionIn)
                .feeTransaction(transactionFee) // can be null
                .amount(transfer.getAmount().getValue())
                .currency(transfer.getAmount().getCurrency().code())
                .category(transfer.getCategory())
                .feeAmount(transfer.getFeeAmount() != null ? transfer.getFeeAmount().getValue() : null)
                .description(transfer.getDescription() != null ? transfer.getDescription().value() : null)
                .idempotencyKey(transfer.getIdempotencyKey().value())
                .executedAt(transfer.getExecutedAt())
                .build();

        var transferSaved = transferJpaRepository.save(transferJpaEntity);

        return TransferJpaEntityMapper.toDomainEntity(transferSaved);
    }

    @Override
    public Optional<Transfer> findById(UUID id) {
        var transferJpaEntity = transferJpaRepository.findById(id);
        return transferJpaEntity.map(TransferJpaEntityMapper::toDomainEntity);
    }

    @Override
    public PagedResult<Transfer> getALlTransactionsByAccountId(PageRequest request, UUID accountId, UUID userId) {
        return null;
    }

    @Override
    public Optional<Transfer> findByIdempotencyKey(String idempotencyKey) {
        var transferJpaEntity = transferJpaRepository.findByIdempotencyKey(idempotencyKey);
        return transferJpaEntity.map(TransferJpaEntityMapper::toDomainEntity);
    }
}
