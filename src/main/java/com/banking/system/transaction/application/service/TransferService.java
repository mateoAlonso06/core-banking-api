package com.banking.system.transaction.application.service;

import com.banking.system.account.domain.exception.AccountNotFoundException;
import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.common.domain.exception.DomainException;
import com.banking.system.transaction.application.dto.command.TransferMoneyCommand;
import com.banking.system.transaction.application.dto.result.TransferResult;
import com.banking.system.transaction.application.mapper.TransferDomainMapper;
import com.banking.system.transaction.application.usecase.GetTransferByIdUseCase;
import com.banking.system.transaction.application.usecase.TransferMoneyUseCase;
import com.banking.system.transaction.domain.exception.TransferNotFoundException;
import com.banking.system.transaction.domain.model.*;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import com.banking.system.transaction.domain.port.out.TransferRepositoryPort;
import com.banking.system.transaction.domain.service.TransferDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService implements
        TransferMoneyUseCase,
        GetTransferByIdUseCase {
    private final TransferRepositoryPort transferRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final TransferDomainService transferDomainService;

    @Override
    @Transactional
    public TransferResult transfer(TransferMoneyCommand command) {
        log.info("Initiating transfer from account {} to account {} for amount {} {}", command.fromAccountId(), command.toAccountId(), command.amount(), command.currency());

        IdempotencyKey idempotencyKey = IdempotencyKey.from(command.idempotencyKey());

        Optional<Transfer> existingTransfer = transferRepositoryPort.findByIdempotencyKey(idempotencyKey.value());

        if (existingTransfer.isPresent()) {
            log.info("Transfer already exists for idempotency key {}", idempotencyKey.value());
            return TransferDomainMapper.toResult(existingTransfer.get());
        }

        Account sourceAccount = accountRepositoryPort.findById(command.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));

        Account targetAccount = accountRepositoryPort.findById(command.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Target account not found"));

        // Prepare data
        Money amount = Money.of(command.amount(), MoneyCurrency.ofCode(command.currency()));
        Description description = new Description(command.description());

        Money feeAmount = null;
        if (command.feeAmount() != null && command.feeAmount().compareTo(BigDecimal.ZERO) > 0 && command.feeCurrency() != null) {
            feeAmount = Money.of(command.feeAmount(), MoneyCurrency.ofCode(command.feeCurrency()));
        }

        // Prepare transactions
        ReferenceNumber refOut = ReferenceNumber.generate();
        ReferenceNumber refIn = ReferenceNumber.generate();

        Transaction txOut = Transaction.createNew(sourceAccount.getId(), TransactionType.TRANSFER_OUT, amount, sourceAccount.getBalance(), description, refOut);
        Transaction txIn = Transaction.createNew(targetAccount.getId(), TransactionType.TRANSFER_IN, amount, targetAccount.getBalance(), description, refIn);

        Transaction savedTxOut = transactionRepositoryPort.save(txOut);
        Transaction savedTxIn = transactionRepositoryPort.save(txIn);

        Transaction savedTxFee = null;
        if (feeAmount != null) {
            Transaction txFee = Transaction.createNew(sourceAccount.getId(), TransactionType.FEE, feeAmount, sourceAccount.getBalance(), new Description("Transfer Fee"), ReferenceNumber.generate());
            savedTxFee = transactionRepositoryPort.save(txFee);
        }

        try {
            // Pending transactions
            transferDomainService.transfer(sourceAccount, targetAccount, amount, description, feeAmount);

            accountRepositoryPort.save(sourceAccount);
            accountRepositoryPort.save(targetAccount);

            // Complete transactions
            savedTxOut.markCompleted();
            savedTxIn.markCompleted();

            transactionRepositoryPort.save(savedTxOut);
            transactionRepositoryPort.save(savedTxIn);

            if (savedTxFee != null)
                transactionRepositoryPort.save(savedTxFee);

            Transfer transfer = Transfer.createNew(
                    sourceAccount.getId(),
                    targetAccount.getId(),
                    savedTxOut.getId(),
                    savedTxIn.getId(),
                    amount,
                    description,
                    feeAmount,
                    savedTxFee != null ? savedTxFee.getId() : null,
                    idempotencyKey
            );

            Transfer transferSaved = transferRepositoryPort.save(transfer);

            log.info("Transfer completed successfully from account {} to account {} for amount {} {}", command.fromAccountId(), command.toAccountId(), command.amount(), command.currency());

            return TransferDomainMapper.toResult(transferSaved);
        } catch (DomainException e) {
            log.error("Transfer failed for idempotency key {}: {}", command.idempotencyKey(), e.getMessage());

            savedTxOut.markFailed();
            savedTxIn.markFailed();

            transactionRepositoryPort.save(savedTxOut);
            transactionRepositoryPort.save(savedTxIn);

            if (savedTxFee != null) {
                savedTxFee.markFailed();
                transactionRepositoryPort.save(savedTxFee);
            }

            throw e;
        }
    }

    @Override
    public TransferResult findById(UUID transferId) {
        Transfer transfer = transferRepositoryPort.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException("Transfer with id: " + transferId + " not found"));

        return TransferDomainMapper.toResult(transfer);
    }
}
