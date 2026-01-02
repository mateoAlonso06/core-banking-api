package com.banking.system.auth.infraestructure.adapter.out.persistence;

import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByEmail(String email);
}
