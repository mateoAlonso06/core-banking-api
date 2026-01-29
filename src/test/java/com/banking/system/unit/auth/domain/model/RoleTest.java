package com.banking.system.unit.auth.domain.model;

import com.banking.system.auth.domain.model.Permission;
import com.banking.system.auth.domain.model.Role;
import com.banking.system.auth.domain.model.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role Domain Entity Tests")
class RoleTest {

    private Permission createTestPermission(String code) {
        return Permission.reconstitute(
                UUID.randomUUID(),
                code,
                "Test permission: " + code,
                "test"
        );
    }

    @Nested
    @DisplayName("Factory Method: reconstitute")
    class ReconstituteTests {

        @Test
        @DisplayName("Should reconstitute role with all valid parameters")
        void shouldReconstituteRole_whenAllParametersAreValid() {
            UUID id = UUID.randomUUID();
            RoleName name = RoleName.CUSTOMER;
            String description = "Standard customer role";
            Set<Permission> permissions = Set.of(
                    createTestPermission("account:read"),
                    createTestPermission("transaction:create")
            );

            Role role = Role.reconstitute(id, name, description, permissions);

            assertNotNull(role);
            assertEquals(id, role.getId());
            assertEquals(name, role.getName());
            assertEquals(description, role.getDescription());
            assertEquals(2, role.getPermissions().size());
        }

        @Test
        @DisplayName("Should throw exception when id is null during reconstitute")
        void shouldThrowException_whenIdIsNull() {
            Set<Permission> permissions = Set.of(createTestPermission("test:read"));

            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                    Role.reconstitute(null, RoleName.CUSTOMER, "Description", permissions)
            );

            assertTrue(exception.getMessage().contains("id cannot be null"));
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowException_whenNameIsNull() {
            Set<Permission> permissions = Set.of(createTestPermission("test:read"));

            assertThrows(NullPointerException.class, () ->
                    Role.reconstitute(UUID.randomUUID(), null, "Description", permissions)
            );
        }

        @Test
        @DisplayName("Should throw exception when description is null")
        void shouldThrowException_whenDescriptionIsNull() {
            Set<Permission> permissions = Set.of(createTestPermission("test:read"));

            assertThrows(NullPointerException.class, () ->
                    Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, null, permissions)
            );
        }

        @Test
        @DisplayName("Should throw exception when permissions is null")
        void shouldThrowException_whenPermissionsIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Description", null)
            );
        }

        @Test
        @DisplayName("Should accept empty permissions set")
        void shouldAcceptEmptyPermissionsSet() {
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.CUSTOMER,
                    "Role without permissions",
                    Collections.emptySet()
            );

            assertNotNull(role);
            assertTrue(role.getPermissions().isEmpty());
        }
    }

    @Nested
    @DisplayName("Business Method: hasPermission")
    class HasPermissionTests {

        @Test
        @DisplayName("Should return true when role has the permission")
        void shouldReturnTrue_whenRoleHasThePermission() {
            Set<Permission> permissions = Set.of(
                    createTestPermission("account:read"),
                    createTestPermission("account:write")
            );
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", permissions);

            boolean result = role.hasPermission("account:read");

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when role does not have the permission")
        void shouldReturnFalse_whenRoleDoesNotHaveThePermission() {
            Set<Permission> permissions = Set.of(
                    createTestPermission("account:read")
            );
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", permissions);

            boolean result = role.hasPermission("account:write");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when role has no permissions")
        void shouldReturnFalse_whenRoleHasNoPermissions() {
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.CUSTOMER,
                    "Customer",
                    Collections.emptySet()
            );

            boolean result = role.hasPermission("account:read");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should be case sensitive when checking permission")
        void shouldBeCaseSensitive_whenCheckingPermission() {
            Set<Permission> permissions = Set.of(
                    createTestPermission("account:read")
            );
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", permissions);

            boolean result = role.hasPermission("ACCOUNT:READ");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when role has multiple permissions including target")
        void shouldReturnTrue_whenRoleHasMultiplePermissionsIncludingTarget() {
            Set<Permission> permissions = Set.of(
                    createTestPermission("account:read"),
                    createTestPermission("account:write"),
                    createTestPermission("transaction:create"),
                    createTestPermission("transaction:read")
            );
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.ADMIN, "Admin", permissions);

            assertTrue(role.hasPermission("transaction:create"));
        }
    }

    @Nested
    @DisplayName("Business Method: getPermissionCodes")
    class GetPermissionCodesTests {

        @Test
        @DisplayName("Should return all permission codes")
        void shouldReturnAllPermissionCodes() {
            Set<Permission> permissions = Set.of(
                    createTestPermission("account:read"),
                    createTestPermission("account:write"),
                    createTestPermission("transaction:create")
            );
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", permissions);

            Set<String> codes = role.getPermissionCodes();

            assertEquals(3, codes.size());
            assertTrue(codes.contains("account:read"));
            assertTrue(codes.contains("account:write"));
            assertTrue(codes.contains("transaction:create"));
        }

        @Test
        @DisplayName("Should return empty set when role has no permissions")
        void shouldReturnEmptySet_whenRoleHasNoPermissions() {
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.CUSTOMER,
                    "Customer",
                    Collections.emptySet()
            );

            Set<String> codes = role.getPermissionCodes();

            assertTrue(codes.isEmpty());
        }

        @Test
        @DisplayName("Should return unmodifiable set")
        void shouldReturnUnmodifiableSet() {
            Set<Permission> permissions = Set.of(createTestPermission("account:read"));
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", permissions);

            Set<String> codes = role.getPermissionCodes();

            assertThrows(UnsupportedOperationException.class, () ->
                    codes.add("new:permission")
            );
        }
    }

    @Nested
    @DisplayName("Permissions Immutability Tests")
    class PermissionsImmutabilityTests {

        @Test
        @DisplayName("Should return unmodifiable permissions set")
        void shouldReturnUnmodifiablePermissionsSet() {
            Set<Permission> permissions = Set.of(createTestPermission("account:read"));
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", permissions);

            Set<Permission> rolePermissions = role.getPermissions();

            assertThrows(UnsupportedOperationException.class, () ->
                    rolePermissions.add(createTestPermission("new:permission"))
            );
        }

        @Test
        @DisplayName("Should not be affected by changes to original permissions set")
        void shouldNotBeAffectedByChangesToOriginalSet() {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(createTestPermission("account:read"));

            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", permissions);

            permissions.add(createTestPermission("account:write"));

            assertEquals(1, role.getPermissions().size());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when role names are the same")
        void shouldBeEqual_whenRoleNamesAreTheSame() {
            Role role1 = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.CUSTOMER,
                    "Description 1",
                    Set.of(createTestPermission("perm1"))
            );
            Role role2 = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.CUSTOMER,
                    "Description 2",
                    Set.of(createTestPermission("perm2"))
            );

            assertEquals(role1, role2);
        }

        @Test
        @DisplayName("Should not be equal when role names differ")
        void shouldNotBeEqual_whenRoleNamesDiffer() {
            Role role1 = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.CUSTOMER,
                    "Description",
                    Collections.emptySet()
            );
            Role role2 = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.ADMIN,
                    "Description",
                    Collections.emptySet()
            );

            assertNotEquals(role1, role2);
        }

        @Test
        @DisplayName("Should have same hashCode when role names are equal")
        void shouldHaveSameHashCode_whenRoleNamesAreEqual() {
            Role role1 = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Desc 1", Collections.emptySet());
            Role role2 = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Desc 2", Collections.emptySet());

            assertEquals(role1.hashCode(), role2.hashCode());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Desc", Collections.emptySet());

            assertEquals(role, role);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Role role = Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Desc", Collections.emptySet());

            assertNotEquals(null, role);
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return role name as string representation")
        void shouldReturnRoleName_asStringRepresentation() {
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.ADMIN,
                    "Administrator role",
                    Collections.emptySet()
            );

            assertEquals("ADMIN", role.toString());
        }

        @Test
        @DisplayName("Should return correct string for each role name")
        void shouldReturnCorrectString_forEachRoleName() {
            for (RoleName roleName : RoleName.values()) {
                Role role = Role.reconstitute(
                        UUID.randomUUID(),
                        roleName,
                        "Test role",
                        Collections.emptySet()
                );

                assertEquals(roleName.name(), role.toString());
            }
        }
    }

    @Nested
    @DisplayName("All Role Names Tests")
    class AllRoleNamesTests {

        @Test
        @DisplayName("Should create role with CUSTOMER name")
        void shouldCreateRole_withCustomerName() {
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.CUSTOMER,
                    "Customer role",
                    Collections.emptySet()
            );

            assertEquals(RoleName.CUSTOMER, role.getName());
        }

        @Test
        @DisplayName("Should create role with ADMIN name")
        void shouldCreateRole_withAdminName() {
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.ADMIN,
                    "Admin role",
                    Collections.emptySet()
            );

            assertEquals(RoleName.ADMIN, role.getName());
        }

        @Test
        @DisplayName("Should create role with BRANCH_MANAGER name")
        void shouldCreateRole_withBranchManagerName() {
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.BRANCH_MANAGER,
                    "Branch Manager role",
                    Collections.emptySet()
            );

            assertEquals(RoleName.BRANCH_MANAGER, role.getName());
        }

        @Test
        @DisplayName("Should create role with all role name types")
        void shouldCreateRole_withAllRoleNameTypes() {
            for (RoleName roleName : RoleName.values()) {
                Role role = Role.reconstitute(
                        UUID.randomUUID(),
                        roleName,
                        "Test role for " + roleName,
                        Collections.emptySet()
                );

                assertEquals(roleName, role.getName());
            }
        }
    }
}
