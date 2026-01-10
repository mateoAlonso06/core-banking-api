package com.banking.system.account.domain.model;

import com.banking.system.account.domain.port.out.AccountAliasGenerator;
import com.banking.system.account.domain.port.out.AccountNumberGenerator;
import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Account {
    private UUID id;
    private UUID customerId;
    private AccountNumber accountNumber;
    private AccountAlias alias;
    private AccountType accountType;
    private MoneyCurrency currency;
    private AccountStatus status;
    private Money balance;
    private Money availableBalance;
    private Money dailyTransferLimit;
    private Money monthlyWithdrawalLimit;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Factory method to create a new Account for initial creation.
     * <p>
     * Validates required fields and initializes domain defaults:
     * - id remains null (to be assigned by persistence)
     * - accountNumber generated using provided generator
     * - alias generated using provided generator
     * - status defaults to ACTIVE
     * - balance and availableBalance default to zero
     * - limits use default values from AccountLimits
     * - openedAt set to current time
     *
     * @param customerId             UUID of the customer owning this account (required)
     * @param accountType            Type of account (required)
     * @param currency               Currency for the account (required)
     * @param accountNumberGenerator Generator for account numbers (required)
     * @param accountAliasGenerator  Generator for account aliases (required)
     * @return new Account instance with validated and defaulted fields
     * @throws NullPointerException if any required parameter is null
     */
    public static Account createNewAccount(
            UUID customerId,
            AccountType accountType,
            MoneyCurrency currency,
            AccountNumberGenerator accountNumberGenerator,
            AccountAliasGenerator accountAliasGenerator) {

        validateFields(customerId, accountType, currency, accountNumberGenerator, accountAliasGenerator);

        AccountNumber accountNumber = accountNumberGenerator.generate(accountType);
        AccountAlias alias = accountAliasGenerator.generate();

        return new Account(
                null, // id - assigned by persistence
                customerId,
                accountNumber,
                alias,
                accountType,
                currency,
                AccountStatus.ACTIVE,
                Money.zero(currency),
                Money.zero(currency),
                AccountLimits.DEFAULT_DAILY_TRANSFER_LIMIT,
                AccountLimits.DEFAULT_MONTHLY_WITHDRAWAL_LIMIT,
                LocalDateTime.now(),
                null, // closedAt
                null, // createdAt - assigned by persistence
                null  // updatedAt - assigned by persistence
        );
    }

    private static void validateFields(
            UUID customerId,
            AccountType accountType,
            MoneyCurrency currency,
            AccountNumberGenerator accountNumberGenerator,
            AccountAliasGenerator accountAliasGenerator) {
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(accountType, "Account type cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(accountNumberGenerator, "Account number generator cannot be null");
        Objects.requireNonNull(accountAliasGenerator, "Account alias generator cannot be null");
    }
}
