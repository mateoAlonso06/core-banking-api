package com.banking.system.auth.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a permission in the system.
 * Permissions are granular access rights that can be assigned to roles.
 */
@Getter
public class Permission {
    private final UUID id;
    private final String code;
    private final String description;
    private final String module;

    private Permission(UUID id, String code, String description, String module) {
        Objects.requireNonNull(code, "Permission code cannot be null");
        Objects.requireNonNull(description, "Permission description cannot be null");
        Objects.requireNonNull(module, "Permission module cannot be null");

        if (code.isBlank()) {
            throw new IllegalArgumentException("Permission code cannot be blank");
        }

        this.id = id;
        this.code = code;
        this.description = description;
        this.module = module;
    }

    /**
     * Reconstitutes a permission from persistence.
     */
    public static Permission reconstitute(UUID id, String code, String description, String module) {
        Objects.requireNonNull(id, "Permission id cannot be null when reconstituting");
        return new Permission(id, code, description, module);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}