package com.banking.system.transaction.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Transaction {
    private UUID id;
    private UUID accountId;
    private TransactionType transactionType;
    private Money amount;
    private Money balanceAfter;
    private String description;
    private String referenceNumber;
    private UUID relatedTransactionId; // only for transfers
    private TransactionStatus status;
    private Instant executedAt;

    public Transaction(UUID id,
                       UUID accountId,
                       TransactionType transactionType,
                       Money amount, Money balanceAfter,
                       String description,
                       String referenceNumber,
                       UUID relatedTransactionId,
                       TransactionStatus status,
                       Instant executedAt) {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(transactionType, "Transaction type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(referenceNumber, "Reference number cannot be null");
        Objects.requireNonNull(status, "Transaction status cannot be null");
        Objects.requireNonNull(executedAt, "Executed at timestamp cannot be null");

        this.id = id;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.relatedTransactionId = relatedTransactionId;
        this.status = status;
        this.executedAt = executedAt;
    }

    public static Transaction createNew(
            UUID accountId,
            TransactionType transactionType,
            Money amount,
            String description,
            String referenceNumber,
            UUID relatedTransactionId
    ) {

        return null;
    }
}
