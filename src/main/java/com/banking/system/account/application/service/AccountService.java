package com.banking.system.account.application.service;

import com.banking.system.account.application.dto.command.CreateAccountCommand;
import com.banking.system.account.application.dto.result.AccountResult;
import com.banking.system.account.application.event.AccountCreatedEvent;
import com.banking.system.account.application.event.publisher.AccountEventPublisher;
import com.banking.system.account.application.usecase.CreateAccountUseCase;
import com.banking.system.account.application.usecase.FindAccountByIdUseCase;
import com.banking.system.account.application.usecase.FindAllAccountUseCase;
import com.banking.system.account.domain.exception.AccountNotFoundException;
import com.banking.system.account.domain.exception.AliasGenerationFailedException;
import com.banking.system.account.domain.exception.InvalidAccountOwnerException;
import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.model.AccountType;
import com.banking.system.account.domain.port.out.AccountAliasGenerator;
import com.banking.system.account.domain.port.out.AccountNumberGenerator;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService implements
        CreateAccountUseCase,
        FindAccountByIdUseCase,
        FindAllAccountUseCase {

    private static final int MAX_ALIAS_GENERATION_ATTEMPTS = 5;

    private final AccountRepositoryPort accountRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountAliasGenerator accountAliasGenerator;
    private final AccountEventPublisher accountEventPublisher;

    @Override
    @Transactional
    public AccountResult createAccount(CreateAccountCommand command) {
        Customer customer = customerRepositoryPort.findById(command.customerId())
                .orElseThrow(() -> new InvalidAccountOwnerException("Customer with ID " + command.customerId() + " not found."));

        if (!customer.isKycApproved())
            throw new IllegalStateException("Customer with ID " + command.customerId() + " has not completed KYC.");

        // The alias has more potential for collisions, so we generate and check it in a loop
        Account account = this.createAccountWithUniqueAlias(
                command.customerId(),
                command.accountType(),
                MoneyCurrency.ofCode(command.currency())
        );

        Account savedAccount = accountRepositoryPort.save(account);
        accountEventPublisher.publishAccountCreated(
                new AccountCreatedEvent(
                        savedAccount.getId(),
                        savedAccount.getCustomerId(),
                        customer.getUserId(),
                        savedAccount.getCurrency().code(),
                        savedAccount.getBalance().getValue(),
                        savedAccount.getAccountNumber(),
                        savedAccount.getAlias(),
                        savedAccount.getAccountType(),
                        savedAccount.getOpenedAt()
                )
        );

        return AccountResult.fromDomain(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResult findById(UUID accountId) {
        var account = accountRepositoryPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found."));

        return AccountResult.fromDomain(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResult> findAll(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: page must be >= 0 and size must be > 0.");
        }

        var accounts = accountRepositoryPort.findAll(page, size);

        return accounts.stream()
                .map(AccountResult::fromDomain)
                .toList();
    }

    /**
     * Creates account with unique alias, retrying if collision occurs.
     * This handles the rare case where the randomly generated alias already exists.
     */
    private Account createAccountWithUniqueAlias(UUID customerId, AccountType accountType, MoneyCurrency currency) {
        var accountNumber = accountNumberGenerator.generate(accountType);

        for (int attempt = 0; attempt < MAX_ALIAS_GENERATION_ATTEMPTS; attempt++) {
            var candidateAlias = accountAliasGenerator.generate();

            if (!accountRepositoryPort.existsByAlias(candidateAlias)) {
                return Account.createNewAccount(
                        customerId,
                        accountType,
                        currency,
                        accountNumber,
                        candidateAlias
                );
            }
        }
        throw new AliasGenerationFailedException("Failed to generate unique alias after " + MAX_ALIAS_GENERATION_ATTEMPTS + " attempts");
    }
}
