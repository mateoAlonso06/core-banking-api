package com.banking.system.account.infraestructure.adapter.out.persistence.repository;

import com.banking.system.account.infraestructure.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
    boolean existsByCustomerIdAndCurrency(UUID customerId, String currency);

    boolean existsByAlias(String alias);

    boolean existsByAccountNumber(String accountNumber);

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);
}
