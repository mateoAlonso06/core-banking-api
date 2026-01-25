package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    Page<TransactionJpaEntity> findAllByAccountId(UUID accountId, Pageable pageable);
}
