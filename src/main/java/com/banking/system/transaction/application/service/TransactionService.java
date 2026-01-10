package com.banking.system.transaction.application.service;

import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.common.domain.Money;
import com.banking.system.transaction.application.dto.DepositCommand;
import com.banking.system.transaction.application.usecase.DepositUseCase;
import com.banking.system.transaction.application.usecase.TransferUseCase;
import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.domain.model.TransactionType;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements DepositUseCase, TransferUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;


    @Override
    public void deposit(DepositCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        Money amount = Money.of(command.amount(), command.currency());

        // Domain method:
        // Valid: currency matching, account is active and modifies the balance
        account.credit(amount);

        Transaction transaction = Transaction.createNew(
                account.getId(),
                TransactionType.DEPOSIT,
                amount.getValue(),
                amount.getCurrency(),
                command.description(),
                command.referenceNumber(),
                null
        );

        transaction = transaction.markCompleted(
                account.getCurrentBalance(),
                Instant.now()
        );

        var transactionSaved = transactionRepository.save(transaction);
        accountRepository.save(account); // Update account balance

        return TransactionMapper.toResult(saved);
    }

    @Override
    public void transfer(TransferCommand command) {
        // Implementation of transfer logic

    }
}
