package com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.common.infraestructure.utils.SanitizeHtml;
import com.banking.system.transaction.application.dto.command.WithdrawMoneyCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawMoneyRequest(
        @NotNull @Positive
        BigDecimal amount,
        @NotBlank
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g., USD, EUR)")
        @SanitizeHtml
        String currency,
        @NotNull
        UUID idempotencyKey
) {
    public WithdrawMoneyCommand toCommand() {
        return new WithdrawMoneyCommand(this.amount, this.currency, this.idempotencyKey);
    }
}
