package com.banking.system.auth.infraestructure.adapter.out.mapper;

import com.banking.system.auth.domain.model.RefreshToken;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;

public class RefreshTokenJpaMapper {

    private RefreshTokenJpaMapper() {}

    public static RefreshTokenJpaEntity toJpaEntity(RefreshToken domain) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setToken(domain.getToken());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevoked(domain.isRevoked());
        return entity;
    }

    public static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }
}