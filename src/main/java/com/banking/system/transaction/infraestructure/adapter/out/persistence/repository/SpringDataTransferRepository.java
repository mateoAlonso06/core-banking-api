package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransferJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTransferRepository extends JpaRepository<TransferJpaEntity, UUID> {
    Optional<TransferJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
