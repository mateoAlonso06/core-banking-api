package com.banking.system.unit.auth.domain.model;

import com.banking.system.auth.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Domain Entity Tests")
class UserTest {

    // Helper methods para crear objetos de prueba
    private Permission createTestPermission(String code, String description, String module) {
        return Permission.reconstitute(
                UUID.randomUUID(),
                code,
                description,
                module
        );
    }

    private Role createTestRole(RoleName roleName, Set<Permission> permissions) {
        return Role.reconstitute(
                UUID.randomUUID(),
                roleName,
                "Test role for " + roleName.name(),
                permissions
        );
    }

    private Role createDefaultCustomerRole() {
        Set<Permission> permissions = Set.of(
                createTestPermission("account:read", "Read account", "account"),
                createTestPermission("transaction:create", "Create transaction", "transaction")
        );
        return createTestRole(RoleName.CUSTOMER, permissions);
    }

    private Email createTestEmail() {
        return new Email("test.user@banking.com");
    }

    private Password createTestPasswordHash() {
        // BCrypt hash válido para "TestPassword123!"
        return Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
    }

    @Nested
    @DisplayName("Factory Method: createNew")
    class CreateNewTests {

        @Test
        @DisplayName("Should create new user with PENDING_VERIFICATION status and null id")
        void shouldCreateNewUser_withPendingVerificationStatus() {
            Email email = createTestEmail();
            Password password = createTestPasswordHash();
            Role role = createDefaultCustomerRole();

            User user = User.createNew(email, password, role);

            assertNotNull(user);
            assertNull(user.getId(), "New user should have null id");
            assertEquals(email, user.getEmail());
            assertEquals(password, user.getPassword());
            assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
            assertEquals(role, user.getRole());
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowException_whenEmailIsNull() {
            Password password = createTestPasswordHash();
            Role role = createDefaultCustomerRole();

            assertThrows(NullPointerException.class, () ->
                    User.createNew(null, password, role)
            );
        }

        @Test
        @DisplayName("Should throw exception when password is null")
        void shouldThrowException_whenPasswordIsNull() {
            Email email = createTestEmail();
            Role role = createDefaultCustomerRole();

            assertThrows(NullPointerException.class, () ->
                    User.createNew(email, null, role)
            );
        }

        @Test
        @DisplayName("Should throw exception when role is null")
        void shouldThrowException_whenRoleIsNull() {
            Email email = createTestEmail();
            Password password = createTestPasswordHash();

            assertThrows(NullPointerException.class, () ->
                    User.createNew(email, password, null)
            );
        }
    }

    @Nested
    @DisplayName("Factory Method: reconstitute")
    class ReconstituteTests {

        @Test
        @DisplayName("Should reconstitute user with all provided parameters")
        void shouldReconstituteUser_withAllParameters() {
            UUID id = UUID.randomUUID();
            Email email = createTestEmail();
            Password password = createTestPasswordHash();
            UserStatus status = UserStatus.ACTIVE;
            Role role = createDefaultCustomerRole();

            User user = User.reconstitute(id, email, password, status, role, false);

            assertNotNull(user);
            assertEquals(id, user.getId());
            assertEquals(email, user.getEmail());
            assertEquals(password, user.getPassword());
            assertEquals(status, user.getStatus());
            assertEquals(role, user.getRole());
        }

        @Test
        @DisplayName("Should reconstitute user with PENDING_VERIFICATION status")
        void shouldReconstituteUser_withPendingVerificationStatus() {
            UUID id = UUID.randomUUID();
            Email email = createTestEmail();
            Password password = createTestPasswordHash();
            Role role = createDefaultCustomerRole();

            User user = User.reconstitute(id, email, password, UserStatus.PENDING_VERIFICATION, role, false);

            assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
        }

        @Test
        @DisplayName("Should reconstitute user with BLOCKED status")
        void shouldReconstituteUser_withBlockedStatus() {
            UUID id = UUID.randomUUID();
            Email email = createTestEmail();
            Password password = createTestPasswordHash();
            Role role = createDefaultCustomerRole();

            User user = User.reconstitute(id, email, password, UserStatus.BLOCKED, role, false);

            assertEquals(UserStatus.BLOCKED, user.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowException_whenEmailIsNull() {
            UUID id = UUID.randomUUID();
            Password password = createTestPasswordHash();
            Role role = createDefaultCustomerRole();

            assertThrows(NullPointerException.class, () ->
                    User.reconstitute(id, null, password, UserStatus.ACTIVE, role, false)
            );
        }
    }

    @Nested
    @DisplayName("Business Method: changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePassword_successfully() {
            User user = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    createDefaultCustomerRole()
            );

            Password newPassword = Password.fromHash("$2a$10$NewHashedPassword123456789012345678901234567890");
            user.changePassword(newPassword);

            assertEquals(newPassword, user.getPassword());
        }

        @Test
        @DisplayName("Should throw exception when new password is null")
        void shouldThrowException_whenNewPasswordIsNull() {
            User user = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    createDefaultCustomerRole()
            );

            assertThrows(NullPointerException.class, () ->
                    user.changePassword(null)
            );
        }

        @Test
        @DisplayName("Should allow changing password multiple times")
        void shouldAllowChangingPassword_multipleTimes() {
            User user = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    createDefaultCustomerRole()
            );

            Password password1 = Password.fromHash("$2a$10$FirstPasswordHash12345678901234567890123456789");
            Password password2 = Password.fromHash("$2a$10$SecondPasswordHash1234567890123456789012345678");

            user.changePassword(password1);
            assertEquals(password1, user.getPassword());

            user.changePassword(password2);
            assertEquals(password2, user.getPassword());
        }
    }

    @Nested
    @DisplayName("Business Method: activate")
    class ActivateTests {

        @Test
        @DisplayName("Should activate user when status is PENDING_VERIFICATION")
        void shouldActivateUser_whenStatusIsPendingVerification() {
            User user = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    createDefaultCustomerRole()
            );

            assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());

            user.activate();

            assertEquals(UserStatus.ACTIVE, user.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when user is already ACTIVE")
        void shouldThrowException_whenUserIsAlreadyActive() {
            User user = User.reconstitute(
                    UUID.randomUUID(),
                    createTestEmail(),
                    createTestPasswordHash(),
                    UserStatus.ACTIVE,
                    createDefaultCustomerRole(),
                    false
            );

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    user.activate()
            );

            assertTrue(exception.getMessage().contains("PENDING_VERIFICATION"));
        }

        @Test
        @DisplayName("Should throw exception when user is BLOCKED")
        void shouldThrowException_whenUserIsBlocked() {
            User user = User.reconstitute(
                    UUID.randomUUID(),
                    createTestEmail(),
                    createTestPasswordHash(),
                    UserStatus.BLOCKED,
                    createDefaultCustomerRole(),
                    false
            );

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    user.activate()
            );

            assertTrue(exception.getMessage().contains("PENDING_VERIFICATION"));
        }
    }

    @Nested
    @DisplayName("Business Method: block")
    class BlockTests {

        @Test
        @DisplayName("Should block user when status is ACTIVE")
        void shouldBlockUser_whenStatusIsActive() {
            User user = User.reconstitute(
                    UUID.randomUUID(),
                    createTestEmail(),
                    createTestPasswordHash(),
                    UserStatus.ACTIVE,
                    createDefaultCustomerRole(),
                    false
            );

            user.block();

            assertEquals(UserStatus.BLOCKED, user.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when user is PENDING_VERIFICATION")
        void shouldThrowException_whenUserIsPendingVerification() {
            User user = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    createDefaultCustomerRole()
            );

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    user.block()
            );

            assertTrue(exception.getMessage().contains("ACTIVE"));
        }

        @Test
        @DisplayName("Should throw exception when user is already BLOCKED")
        void shouldThrowException_whenUserIsAlreadyBlocked() {
            User user = User.reconstitute(
                    UUID.randomUUID(),
                    createTestEmail(),
                    createTestPasswordHash(),
                    UserStatus.BLOCKED,
                    createDefaultCustomerRole(),
                    false
            );

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    user.block()
            );

            assertTrue(exception.getMessage().contains("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("User Lifecycle Scenarios")
    class UserLifecycleTests {

        @Test
        @DisplayName("Should complete full user lifecycle: create -> activate -> block")
        void shouldCompleteFullUserLifecycle() {
            // Create new user
            User user = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    createDefaultCustomerRole()
            );
            assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());

            // Activate user
            user.activate();
            assertEquals(UserStatus.ACTIVE, user.getStatus());

            // Block user
            user.block();
            assertEquals(UserStatus.BLOCKED, user.getStatus());
        }

        @Test
        @DisplayName("Should allow password change in any user status")
        void shouldAllowPasswordChange_inAnyStatus() {
            // PENDING_VERIFICATION
            User pendingUser = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    createDefaultCustomerRole()
            );
            Password newPassword1 = Password.fromHash("$2a$10$NewHash1234567890123456789012345678901234567890");
            assertDoesNotThrow(() -> pendingUser.changePassword(newPassword1));

            // ACTIVE
            User activeUser = User.reconstitute(
                    UUID.randomUUID(),
                    new Email("active@test.com"),
                    createTestPasswordHash(),
                    UserStatus.ACTIVE,
                    createDefaultCustomerRole(),
                    false
            );
            Password newPassword2 = Password.fromHash("$2a$10$NewHash2234567890123456789012345678901234567890");
            assertDoesNotThrow(() -> activeUser.changePassword(newPassword2));

            // BLOCKED
            User blockedUser = User.reconstitute(
                    UUID.randomUUID(),
                    new Email("blocked@test.com"),
                    createTestPasswordHash(),
                    UserStatus.BLOCKED,
                    createDefaultCustomerRole(),
                    false
            );
            Password newPassword3 = Password.fromHash("$2a$10$NewHash3234567890123456789012345678901234567890");
            assertDoesNotThrow(() -> blockedUser.changePassword(newPassword3));
        }
    }

    @Nested
    @DisplayName("Role Integration Tests")
    class RoleIntegrationTests {

        @Test
        @DisplayName("Should create user with ADMIN role")
        void shouldCreateUser_withAdminRole() {
            Set<Permission> adminPermissions = Set.of(
                    createTestPermission("admin:all", "Full admin access", "admin"),
                    createTestPermission("user:manage", "Manage users", "user")
            );
            Role adminRole = createTestRole(RoleName.ADMIN, adminPermissions);

            User user = User.createNew(
                    createTestEmail(),
                    createTestPasswordHash(),
                    adminRole
            );

            assertEquals(RoleName.ADMIN, user.getRole().getName());
            assertTrue(user.getRole().hasPermission("admin:all"));
        }

        @Test
        @DisplayName("Should create user with different role types")
        void shouldCreateUser_withDifferentRoleTypes() {
            for (RoleName roleName : RoleName.values()) {
                Set<Permission> permissions = Set.of(
                        createTestPermission("test:permission", "Test permission", "test")
                );
                Role role = createTestRole(roleName, permissions);

                User user = User.createNew(
                        new Email("user." + roleName.name().toLowerCase() + "@test.com"),
                        createTestPasswordHash(),
                        role
                );

                assertEquals(roleName, user.getRole().getName());
            }
        }
    }
}
