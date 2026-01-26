package com.banking.system.auth.infraestructure.adapter.out.mapper;

import com.banking.system.auth.domain.model.Permission;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.PermissionJpaEntity;

/**
 * Manual mapper for converting between Permission domain entities and JPA entities.
 */
public class PermissionJpaMapper {

    private PermissionJpaMapper() {
    }

    /**
     * Converts a JPA entity to a domain entity.
     *
     * @param entity the JPA entity
     * @return the domain entity
     */
    public static Permission toDomain(PermissionJpaEntity entity) {
        if (entity == null) return null;

        return Permission.reconstitute(
                entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getModule()
        );
    }

    /**
     * Converts a domain entity to a JPA entity.
     *
     * @param permission the domain entity
     * @return the JPA entity
     */
    public static PermissionJpaEntity toJpaEntity(Permission permission) {
        if (permission == null) return null;

        PermissionJpaEntity entity = new PermissionJpaEntity();
        entity.setId(permission.getId());
        entity.setCode(permission.getCode());
        entity.setDescription(permission.getDescription());
        entity.setModule(permission.getModule());

        return entity;
    }
}