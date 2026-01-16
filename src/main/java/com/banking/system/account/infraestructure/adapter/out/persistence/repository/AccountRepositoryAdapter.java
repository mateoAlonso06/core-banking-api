package com.banking.system.account.infraestructure.adapter.out.persistence.repository;

import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.account.infraestructure.adapter.out.mapper.AccountJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        var entity = AccountJpaMapper.toJpaEntity(account);
        var entitySaved = springDataAccountRepository.save(entity);
        return AccountJpaMapper.toDomainEntity(entitySaved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return springDataAccountRepository.findById(id).map(AccountJpaMapper::toDomainEntity);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        var account = springDataAccountRepository.findByAccountNumber(accountNumber);
        return account.map(AccountJpaMapper::toDomainEntity);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return springDataAccountRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    public boolean existsByAlias(String alias) {
        return springDataAccountRepository.existsByAlias(alias);
    }

    @Override
    public List<Account> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return springDataAccountRepository.findAll(pageable)
                .map(AccountJpaMapper::toDomainEntity)
                .toList();
    }

    @Override
    public boolean existsUsdAccount(UUID customerId) {
        return springDataAccountRepository.existsByCustomerIdAndCurrency(customerId, "USD");
    }
}
