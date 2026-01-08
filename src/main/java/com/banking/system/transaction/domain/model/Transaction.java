package com.banking.system.transaction.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
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
    private Instant createdAt;

    public static Transaction createNew(
            UUID accountId,
            TransactionType transactionType,
            BigDecimal amount,
            String currency,
            String description,
            String referenceNumber,
            UUID relatedTransactionId
    ) {
        if (accountId == null)
            throw new IllegalArgumentException("Account ID cannot be null");

        if (transactionType == null)
            throw new IllegalArgumentException("Transaction type cannot be null");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        if (description == null || description.isBlank())
            throw new IllegalArgumentException("Description cannot be null or empty");

        if (referenceNumber == null || referenceNumber.isBlank())
            throw new IllegalArgumentException("Reference number cannot be null or empty");

        return new Transaction(
                UUID.randomUUID(),
                accountId,
                transactionType,
                Money.of(amount, MoneyCurrency.ofCode(currency)),
                null, // balanceAfter to be set after transaction is processed
                description,
                referenceNumber,
                relatedTransactionId,
                TransactionStatus.PENDING,
                null, // executedAt to be set when transaction is executed
                Instant.now()
        );
    }

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER_OUT,
        TRANSFER_IN,
        FEE,
        INTEREST,
        REVERSAL
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REVERSED
    }
}
