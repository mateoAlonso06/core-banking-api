package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.transaction.domain.model.TransactionStatus;
import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    Page<TransactionJpaEntity> findAllByAccountId(UUID accountId, Pageable pageable);
    Optional<TransactionJpaEntity> findByIdempotencyKey(String idempotencyKey);

    Page<TransactionJpaEntity> findAllByAccountIdIn(List<UUID> accountIds, Pageable pageable);

    Page<TransactionJpaEntity> findAllByAccountIdInAndStatus(List<UUID> accountIds, TransactionStatus status, Pageable pageable);
}
