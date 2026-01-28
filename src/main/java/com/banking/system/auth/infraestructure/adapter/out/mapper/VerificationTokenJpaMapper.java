package com.banking.system.auth.infraestructure.adapter.out.mapper;

import com.banking.system.auth.domain.model.VerificationToken;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.VerificationTokenJpaEntity;

public class VerificationTokenJpaMapper {

    private VerificationTokenJpaMapper() {
    }

    public static VerificationToken toDomain(VerificationTokenJpaEntity entity) {
        if (entity == null) return null;

        return VerificationToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.isUsed()
        );
    }

    public static VerificationTokenJpaEntity toJpaEntity(VerificationToken token) {
        if (token == null) return null;

        VerificationTokenJpaEntity entity = new VerificationTokenJpaEntity();
        entity.setId(token.getId());
        entity.setUserId(token.getUserId());
        entity.setToken(token.getToken());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsed(token.isUsed());
        return entity;
    }
}