package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.domain.model.VerificationToken;
import com.banking.system.auth.domain.port.out.VerificationTokenRepositoryPort;
import com.banking.system.auth.infraestructure.adapter.out.mapper.VerificationTokenJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.VerificationTokenJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepositoryPort {

    private final VerificationTokenJpaRepository verificationTokenJpaRepository;

    @Override
    public VerificationToken save(VerificationToken token) {
        VerificationTokenJpaEntity entity = VerificationTokenJpaMapper.toJpaEntity(token);
        VerificationTokenJpaEntity saved = verificationTokenJpaRepository.save(entity);
        return VerificationTokenJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return verificationTokenJpaRepository.findByToken(token)
                .map(VerificationTokenJpaMapper::toDomain);
    }

    @Override
    public Optional<VerificationToken> findLatestByUserId(UUID userId) {
        return verificationTokenJpaRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(VerificationTokenJpaMapper::toDomain);
    }
}