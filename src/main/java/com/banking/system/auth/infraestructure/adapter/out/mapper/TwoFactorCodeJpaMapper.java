package com.banking.system.auth.infraestructure.adapter.out.mapper;

import com.banking.system.auth.domain.model.TwoFactorCode;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.TwoFactorCodeJpaEntity;

public class TwoFactorCodeJpaMapper {

    private TwoFactorCodeJpaMapper() {
    }

    public static TwoFactorCode toDomain(TwoFactorCodeJpaEntity entity) {
        if (entity == null) return null;

        return TwoFactorCode.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getCode(),
                entity.getSessionToken(),
                entity.getExpiresAt(),
                entity.isUsed(),
                entity.getAttempts()
        );
    }

    public static TwoFactorCodeJpaEntity toJpaEntity(TwoFactorCode twoFactorCode) {
        if (twoFactorCode == null) return null;

        TwoFactorCodeJpaEntity entity = new TwoFactorCodeJpaEntity();
        entity.setId(twoFactorCode.getId());
        entity.setUserId(twoFactorCode.getUserId());
        entity.setCode(twoFactorCode.getCode());
        entity.setSessionToken(twoFactorCode.getSessionToken());
        entity.setExpiresAt(twoFactorCode.getExpiresAt());
        entity.setUsed(twoFactorCode.isUsed());
        entity.setAttempts(twoFactorCode.getAttempts());
        return entity;
    }
}
