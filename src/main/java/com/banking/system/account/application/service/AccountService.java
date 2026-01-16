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
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
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
        log.info("Creating account: userId={}, accountType={}, currency={}",
                command.userId(), command.accountType(), command.currency());

        var customer = customerRepositoryPort.findByUserId(command.userId())
                .orElseThrow(() -> new InvalidAccountOwnerException("Customer not found for user ID " + command.userId()));

        if (!customer.isKycApproved()) {
            log.debug("KYC not approved for customer: {}", customer.getId());
            throw new IllegalStateException("Customer with ID " + customer.getId() + " has not completed KYC.");
        }

        if (command.currency().equals("USD")) {
            if (accountRepositoryPort.existsUsdAccount(customer.getId())) {
                log.debug("USD acount already exists for customer: {}", customer.getId());
                throw new IllegalStateException("Users are only allowed to have one USD account.");
            }
        }

        // The alias has more potential for collisions, so we generate and check it in a loop
        var account = this.createAccountWithUniqueAlias(
                customer.getId(),
                command.accountType(),
                MoneyCurrency.ofCode(command.currency())
        );

        Account savedAccount = accountRepositoryPort.save(account);

        log.info("Account created: id={}, number={}, customer={}",
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                customer.getId());

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
        log.debug("Finding account by id: {}", accountId);
        var account = accountRepositoryPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found."));

        return AccountResult.fromDomain(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResult> findAll(int page, int size) {
        log.info("Fetching all accounts - page: {}, size: {}", page, size);
        if (page < 0 || size <= 0) {
            log.error("Invalid pagination parameters: page={}, size={}", page, size);
            throw new IllegalArgumentException("Invalid pagination parameters: page must be >= 0 and size must be > 0.");
        }

        var accounts = accountRepositoryPort.findAll(page, size);

        log.debug("Found {} accounts", accounts.size());

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

            if (!accountRepositoryPort.existsByAlias(candidateAlias.value())) {
                log.debug("Alias generated on attempt {}: {}", attempt + 1, candidateAlias.value());
                return Account.createNewAccount(
                        customerId, accountType, currency, accountNumber, candidateAlias
                );
            }
            log.debug("Alias collision on attempt {}: {}", attempt + 1, candidateAlias.value());
        }
        throw new AliasGenerationFailedException("Failed to generate unique alias after " + MAX_ALIAS_GENERATION_ATTEMPTS + " attempts");
    }
}
