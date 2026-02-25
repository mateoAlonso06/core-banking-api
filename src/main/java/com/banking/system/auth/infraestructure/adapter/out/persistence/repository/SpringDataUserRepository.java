package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByEmail(String email);

    @Query("SELECT u.lastLoginAt FROM UserJpaEntity u WHERE u.id = :id")
    Instant findLastLoginAtById(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE UserJpaEntity u SET u.lastLoginAt = :now WHERE u.id = :id")
    void updateLastLoginAt(@Param("id") UUID id, @Param("now") Instant now);
}
