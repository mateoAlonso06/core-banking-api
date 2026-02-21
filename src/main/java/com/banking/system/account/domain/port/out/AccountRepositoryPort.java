package com.banking.system.account.domain.port.out;

import com.banking.system.account.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);

    Optional<Account> findById(UUID id);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAlias(String alias);

    boolean existsByAlias(String alias);

    boolean existsUsdAccount(UUID customerId);

    List<Account> findAllByCustomerId(UUID customerId);

    boolean existsByCustomerId(UUID customerId);
}
