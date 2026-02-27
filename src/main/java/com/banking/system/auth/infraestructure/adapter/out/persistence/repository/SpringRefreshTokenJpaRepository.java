package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringRefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revoked = true WHERE r.userId = :userId AND r.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiresAt < :now OR r.revoked = true")
    int deleteExpiredOrRevoked(@Param("now") Instant now);
}