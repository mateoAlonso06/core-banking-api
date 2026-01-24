package com.banking.system.transaction.application.service;

import com.banking.system.account.domain.exception.AccountNotFoundException;
import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.customer.domain.exception.CustomerNotFoundException;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import com.banking.system.transaction.application.dto.command.DepositMoneyCommand;
import com.banking.system.transaction.application.usecase.DepositUseCase;
import com.banking.system.transaction.domain.exception.AccountAccessDeniedException;
import com.banking.system.transaction.domain.exception.KycNotApprovedException;
import com.banking.system.transaction.domain.model.Description;
import com.banking.system.transaction.domain.model.ReferenceNumber;
import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.domain.model.TransactionType;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements DepositUseCase {
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;

    @Override
    @Transactional
    public void deposit(DepositMoneyCommand command, UUID userId) {
        log.info("Starting deposit process for userId: {}", userId);
        Account account = getAuthorizedAccount(command.accountId(), userId);
        Money depositAmount = Money.of(command.amount(), MoneyCurrency.ofCode(command.currency()));

        account.credit(depositAmount);
        accountRepositoryPort.save(account);

        Transaction transaction = Transaction.createNew(
                account.getId(),
                TransactionType.DEPOSIT,
                depositAmount,
                account.getBalance(),
                new Description("Deposit of " + depositAmount + " to account " + account.getAccountNumber()),
                ReferenceNumber.generate()
        );

        transactionRepositoryPort.save(transaction);

        log.info("Deposit of {} to accountId: {} completed successfully", depositAmount, account.getId());
    }

    /*Gets the account if the user is authorized and KYC is approved*/
    private Account getAuthorizedAccount(UUID accountId, UUID userId) {
        Customer customer = customerRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for userId: " + userId));

        Account account = accountRepositoryPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        if (!account.getCustomerId().equals(customer.getId())) {
            log.warn("Unauthorized account access attempt by userId: {} to accountId: {}", userId, accountId);
            throw new AccountAccessDeniedException("Account does not belong to the authenticated user");
        }

        if (!customer.isKycApproved()) {
            log.warn("KYC not approved for userId: {}", userId);
            throw new KycNotApprovedException("KYC not approved for the customer");
        }
        return account;
    }
}
