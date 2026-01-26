package com.banking.system.account.domain.port.out;

import com.banking.system.account.domain.model.Account;
import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);

    Optional<Account> findById(UUID id);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAlias(String alias);

    PagedResult<Account> findAll(PageRequest pageRequest);

    boolean existsUsdAccount(UUID customerId);
}
