package com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.transaction.application.dto.command.TransferMoneyCommand;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyRequest(
        @NotNull(message = "Source account ID is required")
        UUID fromAccountId,

        @NotNull(message = "Destination account ID is required")
        UUID toAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 15, fraction = 2, message = "Amount must have a maximum of 19 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g., USD, EUR)")
        String currency,

        @DecimalMin(value = "0.00", message = "Fee amount cannot be negative")
        @Digits(integer = 15, fraction = 2, message = "Fee amount must have a maximum of 19 integer digits and 2 decimal places")
        BigDecimal feeAmount,

        @Size(min = 3, max = 3, message = "Fee currency must be a 3-letter ISO code (e.g., ARS, USD)")
        String feeCurrency,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Idempotency key is required")
        UUID idempotencyKey
) {

    public TransferMoneyCommand toCommand() {
        return new TransferMoneyCommand(
                fromAccountId,
                toAccountId,
                amount,
                currency,
                feeAmount,
                feeCurrency,
                description,
                idempotencyKey
        );
    }
}
