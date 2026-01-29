package com.banking.system.unit.auth.domain.model;

import com.banking.system.auth.domain.model.Permission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Permission Domain Entity Tests")
class PermissionTest {

    @Nested
    @DisplayName("Factory Method: reconstitute")
    class ReconstituteTests {

        @Test
        @DisplayName("Should reconstitute permission with all valid parameters")
        void shouldReconstitutePermission_whenAllParametersAreValid() {
            UUID id = UUID.randomUUID();
            String code = "account:read";
            String description = "Read account data";
            String module = "account";

            Permission permission = Permission.reconstitute(id, code, description, module);

            assertNotNull(permission);
            assertEquals(id, permission.getId());
            assertEquals(code, permission.getCode());
            assertEquals(description, permission.getDescription());
            assertEquals(module, permission.getModule());
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowException_whenIdIsNull() {
            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                    Permission.reconstitute(null, "account:read", "Read account", "account")
            );

            assertTrue(exception.getMessage().contains("id cannot be null"));
        }

        @Test
        @DisplayName("Should throw exception when code is null")
        void shouldThrowException_whenCodeIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Permission.reconstitute(UUID.randomUUID(), null, "Read account", "account")
            );
        }

        @Test
        @DisplayName("Should throw exception when code is blank")
        void shouldThrowException_whenCodeIsBlank() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Permission.reconstitute(UUID.randomUUID(), "   ", "Read account", "account")
            );

            assertTrue(exception.getMessage().contains("code cannot be blank"));
        }

        @Test
        @DisplayName("Should throw exception when code is empty string")
        void shouldThrowException_whenCodeIsEmptyString() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Permission.reconstitute(UUID.randomUUID(), "", "Read account", "account")
            );

            assertTrue(exception.getMessage().contains("code cannot be blank"));
        }

        @Test
        @DisplayName("Should throw exception when description is null")
        void shouldThrowException_whenDescriptionIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Permission.reconstitute(UUID.randomUUID(), "account:read", null, "account")
            );
        }

        @Test
        @DisplayName("Should throw exception when module is null")
        void shouldThrowException_whenModuleIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Permission.reconstitute(UUID.randomUUID(), "account:read", "Read account", null)
            );
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when codes are the same")
        void shouldBeEqual_whenCodesAreTheSame() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();

            Permission permission1 = Permission.reconstitute(id1, "account:read", "Description 1", "account");
            Permission permission2 = Permission.reconstitute(id2, "account:read", "Description 2", "account");

            assertEquals(permission1, permission2);
        }

        @Test
        @DisplayName("Should not be equal when codes differ")
        void shouldNotBeEqual_whenCodesDiffer() {
            UUID id = UUID.randomUUID();

            Permission permission1 = Permission.reconstitute(id, "account:read", "Read account", "account");
            Permission permission2 = Permission.reconstitute(id, "account:write", "Write account", "account");

            assertNotEquals(permission1, permission2);
        }

        @Test
        @DisplayName("Should have same hashCode when codes are equal")
        void shouldHaveSameHashCode_whenCodesAreEqual() {
            Permission permission1 = Permission.reconstitute(UUID.randomUUID(), "account:read", "Desc 1", "account");
            Permission permission2 = Permission.reconstitute(UUID.randomUUID(), "account:read", "Desc 2", "account");

            assertEquals(permission1.hashCode(), permission2.hashCode());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            Permission permission = Permission.reconstitute(UUID.randomUUID(), "account:read", "Read", "account");

            assertEquals(permission, permission);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Permission permission = Permission.reconstitute(UUID.randomUUID(), "account:read", "Read", "account");

            assertNotEquals(null, permission);
        }

        @Test
        @DisplayName("Should not be equal to object of different type")
        void shouldNotBeEqualToObjectOfDifferentType() {
            Permission permission = Permission.reconstitute(UUID.randomUUID(), "account:read", "Read", "account");

            assertNotEquals("account:read", permission);
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return code as string representation")
        void shouldReturnCode_asStringRepresentation() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "transaction:create",
                    "Create transaction",
                    "transaction"
            );

            assertEquals("transaction:create", permission.toString());
        }
    }

    @Nested
    @DisplayName("Valid Permission Codes")
    class ValidPermissionCodesTests {

        @Test
        @DisplayName("Should accept permission code with colon separator")
        void shouldAcceptPermissionCode_withColonSeparator() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "customer:create",
                    "Create customer",
                    "customer"
            );

            assertEquals("customer:create", permission.getCode());
        }

        @Test
        @DisplayName("Should accept permission code with underscore")
        void shouldAcceptPermissionCode_withUnderscore() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "customer_read",
                    "Read customer",
                    "customer"
            );

            assertEquals("customer_read", permission.getCode());
        }

        @Test
        @DisplayName("Should accept permission code with hyphen")
        void shouldAcceptPermissionCode_withHyphen() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "customer-read",
                    "Read customer",
                    "customer"
            );

            assertEquals("customer-read", permission.getCode());
        }

        @Test
        @DisplayName("Should accept permission code with all caps")
        void shouldAcceptPermissionCode_withAllCaps() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "ADMIN_ALL",
                    "Full admin access",
                    "admin"
            );

            assertEquals("ADMIN_ALL", permission.getCode());
        }

        @Test
        @DisplayName("Should accept simple permission code")
        void shouldAcceptSimplePermissionCode() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "read",
                    "Read permission",
                    "generic"
            );

            assertEquals("read", permission.getCode());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should accept empty description")
        void shouldAcceptEmptyDescription() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "test:permission",
                    "",
                    "test"
            );

            assertEquals("", permission.getDescription());
        }

        @Test
        @DisplayName("Should accept empty module")
        void shouldAcceptEmptyModule() {
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "test:permission",
                    "Test permission",
                    ""
            );

            assertEquals("", permission.getModule());
        }

        @Test
        @DisplayName("Should preserve code exactly as provided")
        void shouldPreserveCode_exactlyAsProvided() {
            String code = "CamelCase:Permission_With-Different.Separators";

            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    code,
                    "Complex permission",
                    "test"
            );

            assertEquals(code, permission.getCode());
        }
    }
}
