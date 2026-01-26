package com.banking.system.transaction.application.service;

import com.banking.system.account.domain.exception.AccountNotFoundException;
import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.customer.domain.exception.CustomerNotFoundException;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import com.banking.system.transaction.application.dto.command.DepositMoneyCommand;
import com.banking.system.transaction.application.dto.command.WithdrawMoneyCommand;
import com.banking.system.transaction.application.dto.result.TransactionResult;
import com.banking.system.transaction.application.mapper.TransactionDomainMapper;
import com.banking.system.transaction.application.usecase.DepositUseCase;
import com.banking.system.transaction.application.usecase.GetAllTransactionsByAccountUseCase;
import com.banking.system.transaction.application.usecase.WithdrawUseCase;
import com.banking.system.transaction.domain.exception.AccountAccessDeniedException;
import com.banking.system.transaction.domain.exception.KycNotApprovedException;
import com.banking.system.transaction.domain.model.Description;
import com.banking.system.transaction.domain.model.ReferenceNumber;
import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.domain.model.TransactionType;
import com.banking.system.transaction.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements
        DepositUseCase,
        WithdrawUseCase,
        GetAllTransactionsByAccountUseCase {
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;

    @Override
    @Transactional
    public void deposit(DepositMoneyCommand command, UUID accountId, UUID userId) {
        log.info("Starting deposit process for userId: {}", userId);
        Account account = getAuthorizedAccount(accountId, userId);
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

    @Override
    @Transactional
    public void withdraw(WithdrawMoneyCommand command, UUID accountId, UUID userId) {
        log.info("Starting withdrawal process for usedId: {}", userId);

        Account account = getAuthorizedAccount(accountId, userId);
        account.debit(Money.of(command.amount(), MoneyCurrency.ofCode(command.currency())));
        accountRepositoryPort.save(account);

        Transaction transaction = Transaction.createNew(
                account.getId(),
                TransactionType.WITHDRAWAL,
                Money.of(command.amount(), MoneyCurrency.ofCode(command.currency())),
                account.getBalance(),
                new Description("Withdrawal of " + command.amount() + " from account " + account.getAccountNumber()),
                ReferenceNumber.generate()
        );

        transactionRepositoryPort.save(transaction);
        log.info("Withdrawal of {} from accountId: {} completed successfully", command.amount(), account.getId());
    }

    /*For the history*/
    @Override
    @Transactional(readOnly = true)
    public PagedResult<TransactionResult> getAllTransactionsByAccountId(UUID accountId, UUID userId, PageRequest pageRequest) {
        Account account = this.getAuthorizedAccount(accountId, userId);
        PagedResult<Transaction> transactionsPage = transactionRepositoryPort.findAllTransactionsByAccountId(pageRequest, accountId);

        return PagedResult.mapContent(transactionsPage, TransactionDomainMapper::toResult);
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
