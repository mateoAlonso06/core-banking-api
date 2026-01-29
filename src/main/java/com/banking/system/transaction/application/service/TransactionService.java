package com.banking.system.transaction.application.service;

import com.banking.system.account.domain.exception.AccountNotFoundException;
import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.common.domain.exception.DomainException;
import com.banking.system.customer.domain.exception.CustomerNotFoundException;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import com.banking.system.transaction.application.dto.command.DepositMoneyCommand;
import com.banking.system.transaction.application.dto.command.WithdrawMoneyCommand;
import com.banking.system.transaction.application.dto.receipt.TransactionReceipt;
import com.banking.system.transaction.application.dto.result.TransactionResult;
import com.banking.system.transaction.application.mapper.ReceiptMapper;
import com.banking.system.transaction.application.mapper.TransactionDomainMapper;
import com.banking.system.transaction.application.usecase.DepositUseCase;
import com.banking.system.transaction.application.usecase.GetAllTransactionsByAccountUseCase;
import com.banking.system.transaction.application.usecase.GetTransactionByIdUseCase;
import com.banking.system.transaction.application.usecase.WithdrawUseCase;
import com.banking.system.transaction.domain.exception.AccountAccessDeniedException;
import com.banking.system.transaction.domain.exception.KycNotApprovedException;
import com.banking.system.transaction.domain.exception.TransactionAlreadyExistException;
import com.banking.system.transaction.domain.exception.TransactionNotFoundException;
import com.banking.system.transaction.domain.model.*;
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
        GetTransactionByIdUseCase,
        GetAllTransactionsByAccountUseCase {
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final TransactionAuditService transactionAuditService;

    @Override
    @Transactional
    public TransactionReceipt deposit(DepositMoneyCommand command, UUID accountId, UUID userId) {
        log.info("Starting deposit process for userId: {}", userId);

        IdempotencyKey idempotencyKey = IdempotencyKey.from(command.idempotencyKey());
        checkIdempotency(idempotencyKey);

        Account account = getAuthorizedAccount(accountId, userId);
        Money depositAmount = Money.of(command.amount(), MoneyCurrency.ofCode(command.currency()));

        // Calculate balance after the operation BEFORE modifying the account
        Money balanceAfter = account.getBalance().add(depositAmount);

        Transaction transaction = Transaction.createNew(
                account.getId(),
                TransactionType.DEPOSIT,
                depositAmount,
                balanceAfter,
                new Description("Deposit of " + depositAmount + " to account " + account.getAccountNumber()),
                ReferenceNumber.generate(),
                idempotencyKey
        );

        // Register transaction in PENDING status
        Transaction savedTransaction = transactionAuditService.registerTransactionAudit(transaction);

        try {
            // Execute the account operation
            account.credit(depositAmount);
            accountRepositoryPort.save(account);

            // Mark transaction as COMPLETED
            transactionAuditService.transactionCompleted(savedTransaction.getId());
            log.info("Deposit of {} to accountId: {} completed successfully", depositAmount, account.getId());

            // Return receipt for confirmation/voucher
            return ReceiptMapper.toTransactionReceipt(savedTransaction, account);
        } catch (Exception e) {
            transactionAuditService.transactionFailed(savedTransaction.getId());
            log.error("Transaction failed for operation deposit, transactionId: {}", savedTransaction.getId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public TransactionReceipt withdraw(WithdrawMoneyCommand command, UUID accountId, UUID userId) {
        log.info("Starting withdrawal process for userId: {}", userId);

        IdempotencyKey idempotencyKey = IdempotencyKey.from(command.idempotencyKey());
        checkIdempotency(idempotencyKey);

        Account account = getAuthorizedAccount(accountId, userId);
        Money withdrawAmount = Money.of(command.amount(), MoneyCurrency.ofCode(command.currency()));

        // Calculate balance after the operation BEFORE modifying the account
        Money balanceAfter = account.getBalance().subtract(withdrawAmount);

        Transaction transaction = Transaction.createNew(
                account.getId(),
                TransactionType.WITHDRAWAL,
                withdrawAmount,
                balanceAfter,
                new Description("Withdrawal of " + withdrawAmount + " from account " + account.getAccountNumber()),
                ReferenceNumber.generate(),
                idempotencyKey
        );

        // Register transaction in PENDING status
        Transaction savedTransaction = transactionAuditService.registerTransactionAudit(transaction);

        try {
            // Execute the account operation
            account.debit(withdrawAmount);
            accountRepositoryPort.save(account);

            // Mark transaction as COMPLETED
            transactionAuditService.transactionCompleted(savedTransaction.getId());
            log.info("Withdrawal of {} from accountId: {} completed successfully", withdrawAmount, account.getId());

            // Return receipt for confirmation/voucher
            return ReceiptMapper.toTransactionReceipt(savedTransaction, account);
        } catch (Exception e) {
            transactionAuditService.transactionFailed(savedTransaction.getId());
            log.error("Transaction failed for operation withdrawal, transactionId: {}", savedTransaction.getId(), e);
            throw e;
        }
    }

    /*For the history*/
    @Override
    @Transactional(readOnly = true)
    public PagedResult<TransactionResult> getAllTransactionsByAccountId(UUID accountId, UUID userId, PageRequest pageRequest) {
        this.getAuthorizedAccount(accountId, userId);
        PagedResult<Transaction> transactionsPage = transactionRepositoryPort.findAllTransactionsByAccountId(pageRequest, accountId);

        return PagedResult.mapContent(transactionsPage, TransactionDomainMapper::toResult);
    }

    @Override
    public TransactionResult getTransactionById(UUID transactionId, UUID userId) {
        Customer customer = customerRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for userId: " + userId));

        Transaction transaction = transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        Account account = accountRepositoryPort.findByCustomerId(customer.getId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found for customerId: " + customer.getId()));

        if (!customer.getId().equals(account.getCustomerId())) {
            throw new AccountAccessDeniedException("Transaction does not belong to the authenticated user");
        }

        return TransactionDomainMapper.toResult(transaction);
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

    private void checkIdempotency(IdempotencyKey idempotencyKey) {
        transactionRepositoryPort.findByIdempotencyKey(idempotencyKey.value())
                .ifPresent(existing -> {
                    throw new TransactionAlreadyExistException(
                            "Transaction with idempotency key already exists: " + idempotencyKey.value()
                    );
                });
    }
}
