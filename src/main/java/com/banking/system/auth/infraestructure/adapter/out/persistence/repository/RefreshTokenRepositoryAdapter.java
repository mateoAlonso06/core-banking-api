package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.domain.model.RefreshToken;
import com.banking.system.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.banking.system.auth.infraestructure.adapter.out.mapper.RefreshTokenJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final SpringRefreshTokenJpaRepository springRefreshTokenJpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = RefreshTokenJpaMapper.toJpaEntity(refreshToken);
        RefreshTokenJpaEntity saved = springRefreshTokenJpaRepository.save(entity);
        return RefreshTokenJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springRefreshTokenJpaRepository.findByToken(token)
                .map(RefreshTokenJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        springRefreshTokenJpaRepository.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public int deleteExpiredOrRevoked() {
        return springRefreshTokenJpaRepository.deleteExpiredOrRevoked(Instant.now());
    }
}