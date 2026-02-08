package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.domain.model.TwoFactorCode;
import com.banking.system.auth.domain.port.out.TwoFactorCodeRepositoryPort;
import com.banking.system.auth.infraestructure.adapter.out.mapper.TwoFactorCodeJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.TwoFactorCodeJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwoFactorCodeRepositoryAdapter implements TwoFactorCodeRepositoryPort {

    private final SpringTwoFactorCodeJpaRepository springTwoFactorCodeJpaRepository;

    @Override
    public TwoFactorCode save(TwoFactorCode twoFactorCode) {
        TwoFactorCodeJpaEntity entity = TwoFactorCodeJpaMapper.toJpaEntity(twoFactorCode);
        TwoFactorCodeJpaEntity saved = springTwoFactorCodeJpaRepository.save(entity);
        return TwoFactorCodeJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<TwoFactorCode> findBySessionToken(String sessionToken) {
        return springTwoFactorCodeJpaRepository.findBySessionToken(sessionToken)
                .map(TwoFactorCodeJpaMapper::toDomain);
    }

    @Override
    public Optional<TwoFactorCode> findLatestByUserId(UUID userId) {
        return springTwoFactorCodeJpaRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(TwoFactorCodeJpaMapper::toDomain);
    }

    @Override
    public void deleteExpiredCodes() {
        springTwoFactorCodeJpaRepository.deleteExpiredCodes(LocalDateTime.now());
    }
}
