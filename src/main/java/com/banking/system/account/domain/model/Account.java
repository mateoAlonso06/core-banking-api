package com.banking.system.account.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import lombok.Getter;

import java.time.LocalDate;
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
    private final LocalDate openedAt;

    // Life cycle and financial fields
    private Money balance;
    private Money availableBalance;
    private Money dailyTransferLimit;
    private Money monthlyTransferLimit;
    private AccountStatus status;
    private LocalDate closedAt;

    private Account(
            UUID id,
            UUID customerId,
            AccountNumber accountNumber,
            AccountAlias alias,
            AccountType accountType,
            MoneyCurrency currency,
            AccountStatus status,
            Money balance,
            Money availableBalance,
            Money dailyTransferLimit,
            Money monthlyTransferLimit,
            LocalDate openedAt,
            LocalDate closedAt) {
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(accountNumber, "Account number cannot be null");
        Objects.requireNonNull(accountType, "Account type cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(status, "Account status cannot be null");
        Objects.requireNonNull(balance, "Balance cannot be null");
        Objects.requireNonNull(availableBalance, "Available balance cannot be null");
        Objects.requireNonNull(dailyTransferLimit, "Daily transfer limit cannot be null");
        Objects.requireNonNull(monthlyTransferLimit, "Monthly withdrawal limit cannot be null");
        Objects.requireNonNull(openedAt, "Opened at timestamp cannot be null");

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
        this.monthlyTransferLimit = monthlyTransferLimit;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    /**
     * Reconstitutes an existing {@link Account} aggregate from persisted state.
     * <p>
     * This factory is intended for repository implementations when loading an account
     * from the database. All invariants are still enforced by the constructor
     * preconditions.
     *
     * @param id                     unique identifier of the account
     * @param customerId             identifier of the account owner
     * @param accountNumber          business account number
     * @param alias                  optional human-friendly alias
     * @param accountType            type of the account (e.g. checking, savings)
     * @param currency               currency of the account balances
     * @param status                 current lifecycle status of the account
     * @param balance                current booked balance
     * @param availableBalance       current available balance
     * @param dailyTransferLimit     configured daily transfer limit
     * @param monthlyTransferLimit   configured monthly transfer limit
     * @param openedAt               date when the account was opened
     * @param closedAt               date when the account was closed, or {@code null} if active
     * @return fully initialized {@link Account} instance representing existing data
     */
    public static Account reconstitute(
            UUID id,
            UUID customerId,
            AccountNumber accountNumber,
            AccountAlias alias,
            AccountType accountType,
            MoneyCurrency currency,
            AccountStatus status,
            Money balance,
            Money availableBalance,
            Money dailyTransferLimit,
            Money monthlyTransferLimit,
            LocalDate openedAt,
            LocalDate closedAt) {

        return new Account(
                id,
                customerId,
                accountNumber,
                alias,
                accountType,
                currency,
                status,
                balance,
                availableBalance,
                dailyTransferLimit,
                monthlyTransferLimit,
                openedAt,
                closedAt
        );
    }

    /**
     * Creates a new {@link Account} aggregate for an account opening use case.
     * <p>
     * The identifier is left {@code null} so it can be assigned by the persistence
     * layer. The account starts in {@link AccountStatus#ACTIVE} status with zero
     * balances and default limits, and {@code openedAt} is set to {@link LocalDate#now()}.
     *
     * @param customerId    identifier of the account owner
     * @param accountType   requested type of the new account
     * @param currency      currency of the new account
     * @param accountNumber generated business account number
     * @param alias         optional alias for the new account
     * @return new {@link Account} instance ready to be persisted
     */
    public static Account createNewAccount(
            UUID customerId,
            AccountType accountType,
            MoneyCurrency currency,
            AccountNumber accountNumber,
            AccountAlias alias) {

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
                Money.of(AccountLimits.DEFAULT_DAILY_TRANSFER, currency),
                Money.of(AccountLimits.DEFAULT_MONTHLY_TRANSFER, currency),
                LocalDate.now(),
                null
        );
    }
}
