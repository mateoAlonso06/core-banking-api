package com.banking.system.transaction.application.service;

import com.banking.system.account.domain.exception.AccountNotFoundException;
import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.common.domain.exception.DomainException;
import com.banking.system.transaction.application.dto.command.TransferMoneyCommand;
import com.banking.system.transaction.application.dto.result.TransferResult;
import com.banking.system.transaction.application.usecase.TransferUseCase;
import com.banking.system.transaction.domain.model.*;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import com.banking.system.transaction.domain.port.out.TransferRepositoryPort;
import com.banking.system.transaction.domain.service.TransferDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService implements TransferUseCase {
    private final TransferRepositoryPort transferRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final TransferDomainService transferDomainService;

    @Override
    @Transactional
    public TransferResult transfer(TransferMoneyCommand command) {
        log.info("Initiating transfer from account {} to account {} for amount {} {}", command.fromAccountId(), command.toAccountId(), command.amount(), command.currency());

        IdempotencyKey idempotencyKey = IdempotencyKey.from(command.idempotencyKey());

        Transfer existingTransfer = transferRepositoryPort.findByIdempotencyKey(idempotencyKey.value())
                .orElse(null);

        Account source = accountRepositoryPort.findById(command.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));

        Account target = accountRepositoryPort.findById(command.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Target account not found"));

        Money amount = Money.of(command.amount(), MoneyCurrency.ofCode(command.currency()));
        Money feeAmount = Money.of(command.feeAmount(), MoneyCurrency.ofCode(command.feeCurrency()));
        Description description = new Description(command.description());

        transferDomainService.transfer(source, target, amount, description, feeAmount);

        ReferenceNumber refOut = ReferenceNumber.generate();
        ReferenceNumber refIn = ReferenceNumber.generate();

        Transaction txOut = Transaction.createNew(
                source.getId(),
                TransactionType.TRANSFER_OUT,
                amount,
                source.getBalance(),
                description,
                refOut
        );

        Transaction txIn = Transaction.createNew(
                target.getId(),
                TransactionType.TRANSFER_IN,
                amount,
                target.getBalance(),
                description,
                refIn
        );

        accountRepositoryPort.save(source);
        accountRepositoryPort.save(target);

        try {
            Transaction savedTxOut = transactionRepositoryPort.save(txOut);
            Transaction savedTxIn = transactionRepositoryPort.save(txIn);

            savedTxOut.markCompleted();
            savedTxIn.markCompleted();

            transactionRepositoryPort.updateTransaction(savedTxOut);
            transactionRepositoryPort.updateTransaction(savedTxIn);

            Transfer transfer = Transfer.createNew(
                    source.getId(),
                    target.getId(),
                    savedTxOut.getId(),
                    savedTxIn.getId(),
                    amount,
                    description,
                    feeAmount,
                    ,
                    idempotencyKey
            );

            transferRepositoryPort.save(transfer);

            return new TransferResult(
                    source.getId(),
                    target.getId(),
                    amount.getValue(),
                    source.getBalance().getValue(),
                    target.getBalance().getValue()
            );
        } catch (DomainException e) {
            log.error("Transfer failed: {}", e.getMessage());
            if (txOut != null) {
                txOut.markFailed();
                transactionRepositoryPort.updateTransaction(txOut);
            }
            if (txIn != null) {
                txIn.markFailed();
                transactionRepositoryPort.updateTransaction(txIn);
            }
            throw e;
        }
    }
}
