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
import com.banking.system.transaction.domain.model.TransferExecution;
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
public class TransferService implements TransferMoneyUseCase, GetTransferByIdUseCase {

    private final TransferRepositoryPort transferRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final TransferDomainService transferDomainService;

    @Override
    @Transactional
    public TransferResult transfer(TransferMoneyCommand command) {
        log.info("Initiating transfer from account {} to account {}",
                command.fromAccountId(), command.toAccountId());

        IdempotencyKey idempotencyKey = IdempotencyKey.from(command.idempotencyKey());

        Optional<Transfer> existingTransfer = transferRepositoryPort.findByIdempotencyKey(idempotencyKey.value());
        if (existingTransfer.isPresent()) {
            log.info("Transfer already exists for idempotency key {}", idempotencyKey.value());
            return TransferDomainMapper.toResult(existingTransfer.get());
        }

        Account sourceAccount = findAccount(command.fromAccountId(), "Source");
        Account targetAccount = findAccount(command.toAccountId(), "Target");

        Money amount = toMoney(command.amount(), command.currency());
        Money feeAmount = toFeeAmount(command);
        Description description = new Description(command.description());

        try {
            TransferExecution execution = transferDomainService.execute(
                    sourceAccount, targetAccount, amount, description, feeAmount, idempotencyKey
            );

            persistExecution(execution, sourceAccount, targetAccount);

            log.info("Transfer completed successfully for idempotency key {}", idempotencyKey.value());
            return TransferDomainMapper.toResult(execution.transfer());

        } catch (DomainException e) {
            log.error("Transfer failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public TransferResult findById(UUID transferId) {
        Transfer transfer = transferRepositoryPort.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found: " + transferId));

        return TransferDomainMapper.toResult(transfer);
    }

    private Account findAccount(UUID accountId, String label) {
        return accountRepositoryPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(label + " account not found"));
    }

    private Money toMoney(BigDecimal amount, String currency) {
        return Money.of(amount, MoneyCurrency.ofCode(currency));
    }

    private Money toFeeAmount(TransferMoneyCommand command) {
        if (command.feeAmount() == null || command.feeCurrency() == null) {
            return null;
        }
        if (command.feeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return Money.of(command.feeAmount(), MoneyCurrency.ofCode(command.feeCurrency()));
    }

    private void persistExecution(TransferExecution execution, Account sourceAccount, Account targetAccount) {
        Transaction savedDebit = transactionRepositoryPort.save(execution.debitTransaction());
        Transaction savedCredit = transactionRepositoryPort.save(execution.creditTransaction());

        Transaction savedFee = null;
        if (execution.hasFee()) {
            savedFee = transactionRepositoryPort.save(execution.feeTransaction());
        }

        accountRepositoryPort.save(sourceAccount);
        accountRepositoryPort.save(targetAccount);

        savedDebit.markCompleted();
        savedCredit.markCompleted();
        transactionRepositoryPort.save(savedDebit);
        transactionRepositoryPort.save(savedCredit);

        if (savedFee != null) {
            savedFee.markCompleted();
            transactionRepositoryPort.save(savedFee);
        }

        transferRepositoryPort.save(execution.transfer());
    }
}