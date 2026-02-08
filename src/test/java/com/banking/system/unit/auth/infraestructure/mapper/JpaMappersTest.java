package com.banking.system.unit.auth.infraestructure.mapper;

import com.banking.system.auth.domain.model.*;
import com.banking.system.auth.infraestructure.adapter.out.mapper.PermissionJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.mapper.RoleJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.mapper.UserJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.mapper.VerificationTokenJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.PermissionJpaEntity;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.RoleJpaEntity;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.UserJpaEntity;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.VerificationTokenJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPA Mappers Tests")
class JpaMappersTest {

    @Nested
    @DisplayName("PermissionJpaMapper Tests")
    class PermissionJpaMapperTests {

        @Test
        @DisplayName("Should map JPA entity to domain when entity is valid")
        void shouldMapJpaEntityToDomain_whenEntityIsValid() {
            // Given
            UUID id = UUID.randomUUID();
            PermissionJpaEntity entity = new PermissionJpaEntity();
            entity.setId(id);
            entity.setCode("account:read");
            entity.setDescription("Read account permission");
            entity.setModule("account");

            // When
            Permission domain = PermissionJpaMapper.toDomain(entity);

            // Then
            assertNotNull(domain);
            assertEquals(id, domain.getId());
            assertEquals("account:read", domain.getCode());
            assertEquals("Read account permission", domain.getDescription());
            assertEquals("account", domain.getModule());
        }

        @Test
        @DisplayName("Should return null when JPA entity is null")
        void shouldReturnNull_whenJpaEntityIsNull() {
            // When
            Permission domain = PermissionJpaMapper.toDomain(null);

            // Then
            assertNull(domain);
        }

        @Test
        @DisplayName("Should map domain to JPA entity when domain is valid")
        void shouldMapDomainToJpaEntity_whenDomainIsValid() {
            // Given
            UUID id = UUID.randomUUID();
            Permission domain = Permission.reconstitute(
                    id,
                    "transaction:create",
                    "Create transaction",
                    "transaction"
            );

            // When
            PermissionJpaEntity entity = PermissionJpaMapper.toJpaEntity(domain);

            // Then
            assertNotNull(entity);
            assertEquals(id, entity.getId());
            assertEquals("transaction:create", entity.getCode());
            assertEquals("Create transaction", entity.getDescription());
            assertEquals("transaction", entity.getModule());
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNull_whenDomainIsNull() {
            // When
            PermissionJpaEntity entity = PermissionJpaMapper.toJpaEntity(null);

            // Then
            assertNull(entity);
        }

        @Test
        @DisplayName("Should preserve all fields during round-trip conversion")
        void shouldPreserveAllFields_duringRoundTripConversion() {
            // Given
            UUID id = UUID.randomUUID();
            Permission originalDomain = Permission.reconstitute(
                    id,
                    "admin:all",
                    "Full admin access",
                    "admin"
            );

            // When
            PermissionJpaEntity entity = PermissionJpaMapper.toJpaEntity(originalDomain);
            Permission convertedDomain = PermissionJpaMapper.toDomain(entity);

            // Then
            assertEquals(originalDomain.getId(), convertedDomain.getId());
            assertEquals(originalDomain.getCode(), convertedDomain.getCode());
            assertEquals(originalDomain.getDescription(), convertedDomain.getDescription());
            assertEquals(originalDomain.getModule(), convertedDomain.getModule());
        }
    }

    @Nested
    @DisplayName("RoleJpaMapper Tests")
    class RoleJpaMapperTests {

        @Test
        @DisplayName("Should map JPA entity to domain when entity is valid")
        void shouldMapJpaEntityToDomain_whenEntityIsValid() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID permId = UUID.randomUUID();

            PermissionJpaEntity permEntity = new PermissionJpaEntity();
            permEntity.setId(permId);
            permEntity.setCode("account:read");
            permEntity.setDescription("Read account");
            permEntity.setModule("account");

            RoleJpaEntity roleEntity = new RoleJpaEntity();
            roleEntity.setId(roleId);
            roleEntity.setName(RoleName.CUSTOMER);
            roleEntity.setDescription("Customer role");
            roleEntity.setPermissions(Set.of(permEntity));

            // When
            Role domain = RoleJpaMapper.toDomain(roleEntity);

            // Then
            assertNotNull(domain);
            assertEquals(roleId, domain.getId());
            assertEquals(RoleName.CUSTOMER, domain.getName());
            assertEquals("Customer role", domain.getDescription());
            assertEquals(1, domain.getPermissions().size());
            assertTrue(domain.hasPermission("account:read"));
        }

        @Test
        @DisplayName("Should return null when JPA entity is null")
        void shouldReturnNull_whenJpaEntityIsNull() {
            // When
            Role domain = RoleJpaMapper.toDomain(null);

            // Then
            assertNull(domain);
        }

        @Test
        @DisplayName("Should map domain to JPA entity when domain is valid")
        void shouldMapDomainToJpaEntity_whenDomainIsValid() {
            // Given
            UUID roleId = UUID.randomUUID();
            Permission permission = Permission.reconstitute(
                    UUID.randomUUID(),
                    "account:write",
                    "Write account",
                    "account"
            );
            Role domain = Role.reconstitute(
                    roleId,
                    RoleName.ADMIN,
                    "Admin role",
                    Set.of(permission)
            );

            // When
            RoleJpaEntity entity = RoleJpaMapper.toJpaEntity(domain);

            // Then
            assertNotNull(entity);
            assertEquals(roleId, entity.getId());
            assertEquals(RoleName.ADMIN, entity.getName());
            assertEquals("Admin role", entity.getDescription());
            // Note: permissions are not mapped to JPA entity (managed by relationship)
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNull_whenDomainIsNull() {
            // When
            RoleJpaEntity entity = RoleJpaMapper.toJpaEntity(null);

            // Then
            assertNull(entity);
        }

        @Test
        @DisplayName("Should map role with empty permissions set")
        void shouldMapRole_withEmptyPermissionsSet() {
            // Given
            RoleJpaEntity roleEntity = new RoleJpaEntity();
            roleEntity.setId(UUID.randomUUID());
            roleEntity.setName(RoleName.CUSTOMER);
            roleEntity.setDescription("Customer role");
            roleEntity.setPermissions(new HashSet<>());

            // When
            Role domain = RoleJpaMapper.toDomain(roleEntity);

            // Then
            assertNotNull(domain);
            assertTrue(domain.getPermissions().isEmpty());
        }

        @Test
        @DisplayName("Should map role with multiple permissions")
        void shouldMapRole_withMultiplePermissions() {
            // Given
            PermissionJpaEntity perm1 = new PermissionJpaEntity();
            perm1.setId(UUID.randomUUID());
            perm1.setCode("account:read");
            perm1.setDescription("Read account");
            perm1.setModule("account");

            PermissionJpaEntity perm2 = new PermissionJpaEntity();
            perm2.setId(UUID.randomUUID());
            perm2.setCode("transaction:create");
            perm2.setDescription("Create transaction");
            perm2.setModule("transaction");

            RoleJpaEntity roleEntity = new RoleJpaEntity();
            roleEntity.setId(UUID.randomUUID());
            roleEntity.setName(RoleName.CUSTOMER);
            roleEntity.setDescription("Customer role");
            roleEntity.setPermissions(Set.of(perm1, perm2));

            // When
            Role domain = RoleJpaMapper.toDomain(roleEntity);

            // Then
            assertNotNull(domain);
            assertEquals(2, domain.getPermissions().size());
            assertTrue(domain.hasPermission("account:read"));
            assertTrue(domain.hasPermission("transaction:create"));
        }
    }

    @Nested
    @DisplayName("UserJpaMapper Tests")
    class UserJpaMapperTests {

        private RoleJpaEntity createTestRoleEntity() {
            RoleJpaEntity roleEntity = new RoleJpaEntity();
            roleEntity.setId(UUID.randomUUID());
            roleEntity.setName(RoleName.CUSTOMER);
            roleEntity.setDescription("Customer role");
            roleEntity.setPermissions(new HashSet<>());
            return roleEntity;
        }

        @Test
        @DisplayName("Should map JPA entity to domain when entity is valid")
        void shouldMapJpaEntityToDomain_whenEntityIsValid() {
            // Given
            UUID userId = UUID.randomUUID();
            RoleJpaEntity roleEntity = createTestRoleEntity();

            UserJpaEntity userEntity = new UserJpaEntity();
            userEntity.setId(userId);
            userEntity.setEmail("user@example.com");
            userEntity.setPasswordHash("$2a$10$hashedPassword");
            userEntity.setStatus(UserStatus.ACTIVE);
            userEntity.setRole(roleEntity);

            // When
            User domain = UserJpaMapper.toDomain(userEntity);

            // Then
            assertNotNull(domain);
            assertEquals(userId, domain.getId());
            assertEquals("user@example.com", domain.getEmail().value());
            assertEquals("$2a$10$hashedPassword", domain.getPassword().value());
            assertEquals(UserStatus.ACTIVE, domain.getStatus());
            assertEquals(RoleName.CUSTOMER, domain.getRole().getName());
        }

        @Test
        @DisplayName("Should return null when JPA entity is null")
        void shouldReturnNull_whenJpaEntityIsNull() {
            // When
            User domain = UserJpaMapper.toDomain(null);

            // Then
            assertNull(domain);
        }

        @Test
        @DisplayName("Should map domain to JPA entity when domain is valid")
        void shouldMapDomainToJpaEntity_whenDomainIsValid() {
            // Given
            UUID userId = UUID.randomUUID();
            Role role = Role.reconstitute(
                    UUID.randomUUID(),
                    RoleName.ADMIN,
                    "Admin role",
                    new HashSet<>()
            );
            User domain = User.reconstitute(
                    userId,
                    new Email("admin@example.com"),
                    Password.fromHash("$2a$10$adminHash"),
                    UserStatus.ACTIVE,
                    role,
                    false
            );
            RoleJpaEntity roleEntity = createTestRoleEntity();

            // When
            UserJpaEntity entity = UserJpaMapper.toJpaEntity(domain, roleEntity);

            // Then
            assertNotNull(entity);
            assertEquals(userId, entity.getId());
            assertEquals("admin@example.com", entity.getEmail());
            assertEquals("$2a$10$adminHash", entity.getPasswordHash());
            assertEquals(UserStatus.ACTIVE, entity.getStatus());
            assertEquals(roleEntity, entity.getRole());
            assertFalse(entity.isTwoFactorEnabled());
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNull_whenDomainIsNull() {
            // When
            UserJpaEntity entity = UserJpaMapper.toJpaEntity(null, createTestRoleEntity());

            // Then
            assertNull(entity);
        }

        @Test
        @DisplayName("Should map user with PENDING_VERIFICATION status")
        void shouldMapUser_withPendingVerificationStatus() {
            // Given
            UserJpaEntity userEntity = new UserJpaEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setEmail("pending@example.com");
            userEntity.setPasswordHash("$2a$10$hash");
            userEntity.setStatus(UserStatus.PENDING_VERIFICATION);
            userEntity.setRole(createTestRoleEntity());

            // When
            User domain = UserJpaMapper.toDomain(userEntity);

            // Then
            assertEquals(UserStatus.PENDING_VERIFICATION, domain.getStatus());
        }

        @Test
        @DisplayName("Should map user with BLOCKED status")
        void shouldMapUser_withBlockedStatus() {
            // Given
            UserJpaEntity userEntity = new UserJpaEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setEmail("blocked@example.com");
            userEntity.setPasswordHash("$2a$10$hash");
            userEntity.setStatus(UserStatus.BLOCKED);
            userEntity.setRole(createTestRoleEntity());

            // When
            User domain = UserJpaMapper.toDomain(userEntity);

            // Then
            assertEquals(UserStatus.BLOCKED, domain.getStatus());
        }

        @Test
        @DisplayName("Should preserve email normalization during mapping")
        void shouldPreserveEmailNormalization_duringMapping() {
            // Given
            User domain = User.reconstitute(
                    UUID.randomUUID(),
                    new Email("User@Example.COM"),
                    Password.fromHash("$2a$10$hash"),
                    UserStatus.ACTIVE,
                    Role.reconstitute(UUID.randomUUID(), RoleName.CUSTOMER, "Customer", new HashSet<>()),
                    false
            );
            RoleJpaEntity roleEntity = createTestRoleEntity();

            // When
            UserJpaEntity entity = UserJpaMapper.toJpaEntity(domain, roleEntity);

            // Then
            assertEquals("user@example.com", entity.getEmail());
        }

        @Test
        @DisplayName("Should preserve password hash during mapping")
        void shouldPreservePasswordHash_duringMapping() {
            // Given
            String expectedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
            UserJpaEntity userEntity = new UserJpaEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setEmail("test@example.com");
            userEntity.setPasswordHash(expectedHash);
            userEntity.setStatus(UserStatus.ACTIVE);
            userEntity.setRole(createTestRoleEntity());

            // When
            User domain = UserJpaMapper.toDomain(userEntity);

            // Then
            assertEquals(expectedHash, domain.getPassword().value());
            assertTrue(domain.getPassword().isHashed());
        }
    }

    @Nested
    @DisplayName("VerificationTokenJpaMapper Tests")
    class VerificationTokenJpaMapperTests {

        @Test
        @DisplayName("Should map JPA entity to domain when entity is valid")
        void shouldMapJpaEntityToDomain_whenEntityIsValid() {
            // Given
            UUID tokenId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String token = "verification-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

            VerificationTokenJpaEntity entity = new VerificationTokenJpaEntity();
            entity.setId(tokenId);
            entity.setUserId(userId);
            entity.setToken(token);
            entity.setExpiresAt(expiresAt);
            entity.setUsed(false);

            // When
            VerificationToken domain = VerificationTokenJpaMapper.toDomain(entity);

            // Then
            assertNotNull(domain);
            assertEquals(tokenId, domain.getId());
            assertEquals(userId, domain.getUserId());
            assertEquals(token, domain.getToken());
            assertEquals(expiresAt, domain.getExpiresAt());
            assertFalse(domain.isUsed());
        }

        @Test
        @DisplayName("Should return null when JPA entity is null")
        void shouldReturnNull_whenJpaEntityIsNull() {
            // When
            VerificationToken domain = VerificationTokenJpaMapper.toDomain(null);

            // Then
            assertNull(domain);
        }

        @Test
        @DisplayName("Should map domain to JPA entity when domain is valid")
        void shouldMapDomainToJpaEntity_whenDomainIsValid() {
            // Given
            UUID tokenId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String token = "token-456";
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

            VerificationToken domain = VerificationToken.reconstitute(
                    tokenId,
                    userId,
                    token,
                    expiresAt,
                    false
            );

            // When
            VerificationTokenJpaEntity entity = VerificationTokenJpaMapper.toJpaEntity(domain);

            // Then
            assertNotNull(entity);
            assertEquals(tokenId, entity.getId());
            assertEquals(userId, entity.getUserId());
            assertEquals(token, entity.getToken());
            assertEquals(expiresAt, entity.getExpiresAt());
            assertFalse(entity.isUsed());
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNull_whenDomainIsNull() {
            // When
            VerificationTokenJpaEntity entity = VerificationTokenJpaMapper.toJpaEntity(null);

            // Then
            assertNull(entity);
        }

        @Test
        @DisplayName("Should map used token correctly")
        void shouldMapUsedToken_correctly() {
            // Given
            VerificationTokenJpaEntity entity = new VerificationTokenJpaEntity();
            entity.setId(UUID.randomUUID());
            entity.setUserId(UUID.randomUUID());
            entity.setToken("used-token");
            entity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            entity.setUsed(true);

            // When
            VerificationToken domain = VerificationTokenJpaMapper.toDomain(entity);

            // Then
            assertTrue(domain.isUsed());
        }

        @Test
        @DisplayName("Should map expired token correctly")
        void shouldMapExpiredToken_correctly() {
            // Given
            VerificationTokenJpaEntity entity = new VerificationTokenJpaEntity();
            entity.setId(UUID.randomUUID());
            entity.setUserId(UUID.randomUUID());
            entity.setToken("expired-token");
            entity.setExpiresAt(LocalDateTime.now().minusMinutes(1));
            entity.setUsed(false);

            // When
            VerificationToken domain = VerificationTokenJpaMapper.toDomain(entity);

            // Then
            assertTrue(domain.isExpired());
            assertFalse(domain.isUsed());
        }

        @Test
        @DisplayName("Should preserve all fields during round-trip conversion")
        void shouldPreserveAllFields_duringRoundTripConversion() {
            // Given
            UUID tokenId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String token = "round-trip-token";
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

            VerificationToken originalDomain = VerificationToken.reconstitute(
                    tokenId,
                    userId,
                    token,
                    expiresAt,
                    false
            );

            // When
            VerificationTokenJpaEntity entity = VerificationTokenJpaMapper.toJpaEntity(originalDomain);
            VerificationToken convertedDomain = VerificationTokenJpaMapper.toDomain(entity);

            // Then
            assertEquals(originalDomain.getId(), convertedDomain.getId());
            assertEquals(originalDomain.getUserId(), convertedDomain.getUserId());
            assertEquals(originalDomain.getToken(), convertedDomain.getToken());
            assertEquals(originalDomain.getExpiresAt(), convertedDomain.getExpiresAt());
            assertEquals(originalDomain.isUsed(), convertedDomain.isUsed());
        }

        @Test
        @DisplayName("Should handle token with null id (new token)")
        void shouldHandleToken_withNullId() {
            // Given
            VerificationToken domain = VerificationToken.reconstitute(
                    null,
                    UUID.randomUUID(),
                    "new-token",
                    LocalDateTime.now().plusMinutes(15),
                    false
            );

            // When
            VerificationTokenJpaEntity entity = VerificationTokenJpaMapper.toJpaEntity(domain);

            // Then
            assertNull(entity.getId());
            assertNotNull(entity.getUserId());
            assertNotNull(entity.getToken());
        }
    }
}
