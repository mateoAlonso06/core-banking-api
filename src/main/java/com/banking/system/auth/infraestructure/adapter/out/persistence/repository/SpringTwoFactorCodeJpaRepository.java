package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.TwoFactorCodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringTwoFactorCodeJpaRepository extends JpaRepository<TwoFactorCodeJpaEntity, UUID> {

    Optional<TwoFactorCodeJpaEntity> findBySessionToken(String sessionToken);

    Optional<TwoFactorCodeJpaEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("DELETE FROM TwoFactorCodeJpaEntity t WHERE t.expiresAt < :now")
    void deleteExpiredCodes(LocalDateTime now);
}
