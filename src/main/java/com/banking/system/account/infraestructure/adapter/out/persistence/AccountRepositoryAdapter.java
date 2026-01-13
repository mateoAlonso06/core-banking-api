package com.banking.system.account.infraestructure.adapter.out.persistence;

import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.model.AccountAlias;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {
    private final SpringDataAccountRepository springDataAccountRepository;

    @Override
    public Account save(Account account) {
        return null;
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.empty();
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return false;
    }

    @Override
    public boolean existsByAlias(AccountAlias alias) {
        return false;
    }

    @Override
    public List<Account> findAll(int page, int size) {
        return List.of();
    }
}
