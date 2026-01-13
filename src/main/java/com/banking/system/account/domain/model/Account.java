package com.banking.system.account.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Account {
    private final UUID id;
    private final UUID customerId;
    private final AccountNumber accountNumber;
    private final AccountAlias alias;
    private final AccountType accountType;
    private final MoneyCurrency currency;
    private final LocalDateTime openedAt;

    // Life cycle and financial fields
    private Money balance;
    private Money availableBalance;
    private Money dailyTransferLimit;
    private Money monthlyWithdrawalLimit;
    private AccountStatus status;
    private LocalDateTime closedAt;

    private Account(UUID id, UUID customerId,
                    AccountNumber accountNumber,
                    AccountAlias alias,
                    AccountType accountType,
                    MoneyCurrency currency,
                    AccountStatus status,
                    Money balance,
                    Money availableBalance,
                    Money dailyTransferLimit,
                    Money monthlyWithdrawalLimit,
                    LocalDateTime openedAt,
                    LocalDateTime closedAt) {
        this.id = id;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.alias = alias;
        this.accountType = accountType;
        this.currency = currency;
        this.status = status;
        this.balance = balance;
        this.availableBalance = availableBalance;
        this.dailyTransferLimit = dailyTransferLimit;
        this.monthlyWithdrawalLimit = monthlyWithdrawalLimit;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

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
     * @param customerId  UUID of the customer owning this account (required)
     * @param accountType Type of account (required)
     * @param currency    Currency for the account (required)
     * @return new Account instance with validated and defaulted fields
     * @throws NullPointerException if any required parameter is null
     */
    public static Account createNewAccount(
            UUID customerId,
            AccountType accountType,
            MoneyCurrency currency,
            AccountNumber accountNumber,
            AccountAlias alias) {

        validateFields(customerId, accountType, currency, accountNumber, alias);

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
                null
        );
    }

    private static void validateFields(
            UUID customerId,
            AccountType accountType,
            MoneyCurrency currency,
            AccountNumber accountNumber,
            AccountAlias alias) {
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(accountType, "Account type cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(accountNumber);
        Objects.requireNonNull(alias);
    }
}
