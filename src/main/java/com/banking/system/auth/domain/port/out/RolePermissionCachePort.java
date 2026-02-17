package com.banking.system.auth.domain.port.out;

import java.util.Set;

/**
 * Output port for retrieving role permissions with caching support.
 * This abstraction allows the domain to request permissions for a role
 * without knowing the caching implementation details.
 */
public interface RolePermissionCachePort {

    /**
     * Retrieves all permission codes associated with a role name.
     * Implementations should cache the results for performance.
     *
     * @param roleName the name of the role (e.g., "CUSTOMER", "ADMIN")
     * @return a set of permission codes for the role, or empty set if role not found
     */
    Set<String> getPermissionsForRole(String roleName);

    /**
     * Invalidates the cache for a specific role.
     * Call this when role permissions are modified.
     *
     * @param roleName the name of the role to invalidate
     */
    void evictRole(String roleName);

    /**
     * Invalidates the entire permissions cache.
     * Call this when bulk permission changes occur.
     */
    void evictAll();
}