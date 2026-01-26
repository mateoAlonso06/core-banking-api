package com.banking.system.account.infraestructure.adapter.out.persistence.repository;

import com.banking.system.account.domain.model.Account;
import com.banking.system.account.domain.port.out.AccountRepositoryPort;
import com.banking.system.account.infraestructure.adapter.out.mapper.AccountJpaMapper;
import com.banking.system.account.infraestructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.common.infraestructure.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public PagedResult<Account> findAll(PageRequest pageRequest) {
        var pageable = PageMapper.toPageable(pageRequest);
        var page = springDataAccountRepository.findAll(pageable);

        return PageMapper.toPagedResult(page, AccountJpaMapper::toDomainEntity);
    }

    @Override
    public boolean existsUsdAccount(UUID customerId) {
        return springDataAccountRepository.existsByCustomerIdAndCurrency(customerId, "USD");
    }
}
