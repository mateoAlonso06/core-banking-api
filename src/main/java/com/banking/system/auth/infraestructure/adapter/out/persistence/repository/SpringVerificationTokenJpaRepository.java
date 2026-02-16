package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.VerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringVerificationTokenJpaRepository extends JpaRepository<VerificationTokenJpaEntity, UUID> {
    Optional<VerificationTokenJpaEntity> findByToken(String token);

    Optional<VerificationTokenJpaEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}