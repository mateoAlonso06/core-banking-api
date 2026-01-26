package com.banking.system.auth.infraestructure.adapter.out.mapper;

import com.banking.system.auth.domain.model.Permission;
import com.banking.system.auth.domain.model.Role;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.RoleJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manual mapper for converting between Role domain entities and JPA entities.
 */
public class RoleJpaMapper {

    private RoleJpaMapper() {
    }

    /**
     * Converts a JPA entity to a domain entity with all its permissions.
     *
     * @param entity the JPA entity
     * @return the domain entity
     */
    public static Role toDomain(RoleJpaEntity entity) {
        if (entity == null) return null;

        Set<Permission> permissions = entity.getPermissions().stream()
                .map(PermissionJpaMapper::toDomain)
                .collect(Collectors.toSet());

        return Role.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                permissions
        );
    }

    /**
     * Converts a domain entity to a JPA entity.
     * Note: This does not map permissions as they are managed by the relationship.
     *
     * @param role the domain entity
     * @return the JPA entity
     */
    public static RoleJpaEntity toJpaEntity(Role role) {
        if (role == null) return null;

        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());

        return entity;
    }
}