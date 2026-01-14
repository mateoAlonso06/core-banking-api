package com.banking.system.transaction.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class Transfer {
    private UUID id;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private UUID debitTransactionId;
    private UUID creditTransactionId;
    private Money money;
    private Money feeAmount;
    private String description;
    private UUID feeTransactionId;
    private String idempotencyKey;
    private Instant executedAt;
    private Instant createdAt;

    public static Transfer createNew(
            UUID sourceAccountId,
            UUID destinationAccountId,
            UUID debitTransactionId,
            UUID creditTransactionId,
            BigDecimal amount,
            String currency,
            String description,
            BigDecimal feeAmount,
            UUID feeTransactionId,
            String idempotencyKey
    ) {
        if (sourceAccountId == null)
            throw new IllegalArgumentException("Source account ID cannot be null");

        if (destinationAccountId == null)
            throw new IllegalArgumentException("Destination account ID cannot be null");

        if (debitTransactionId == null)
            throw new IllegalArgumentException("Debit transaction ID cannot be null");

        if (creditTransactionId == null)
            throw new IllegalArgumentException("Credit transaction ID cannot be null");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        if (description == null || description.isBlank())
            throw new IllegalArgumentException("Description cannot be null or empty");

        if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Fee amount cannot be negative");

        if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0 && feeTransactionId == null)
            throw new IllegalArgumentException("Fee transaction ID cannot be null when fee amount is greater than zero");

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new IllegalArgumentException("Idempotency key cannot be null or empty");

        // set default values is are not present
        if (feeAmount == null) {
            feeAmount = BigDecimal.ZERO;
        }

        return new Transfer(
                null,
                sourceAccountId,
                destinationAccountId,
                debitTransactionId,
                creditTransactionId,
                Money.of(amount, MoneyCurrency.ofCode(currency)),
                Money.of(feeAmount, MoneyCurrency.ofCode(currency)),
                description,
                feeTransactionId,
                idempotencyKey,
                null,
                Instant.now()
        );
    }
}
