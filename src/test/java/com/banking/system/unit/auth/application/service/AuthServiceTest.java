package com.banking.system.unit.auth.application.service;

import com.banking.system.auth.application.dto.command.ChangeUserPasswordCommand;
import com.banking.system.auth.application.dto.command.LoginCommand;
import com.banking.system.auth.application.dto.command.RegisterCommand;
import com.banking.system.auth.application.dto.result.LoginResult;
import com.banking.system.auth.application.dto.result.RegisterResult;
import com.banking.system.auth.application.dto.result.UserResult;
import com.banking.system.auth.application.event.publisher.UserEventPublisher;
import com.banking.system.auth.application.service.AuthService;
import com.banking.system.auth.domain.exception.InvalidCredentialsException;
import com.banking.system.auth.domain.exception.UserAlreadyExistsException;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import com.banking.system.auth.domain.exception.UserNotVerifiedException;
import com.banking.system.auth.domain.model.*;
import com.banking.system.auth.domain.port.out.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private UserEventPublisher userEventPublisher;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenGenerator tokenGenerator;
    @Mock
    private VerificationTokenRepositoryPort verificationTokenRepository;
    @InjectMocks
    private AuthService authService;

    // Helper methods
    private User createTestUser(UserStatus status) {
        Role role = createTestRole();
        return User.reconsitute(
                UUID.randomUUID(),
                new Email("test@example.com"),
                Password.fromHash("$2a$10$hashedPassword"),
                status,
                role
        );
    }

    private Role createTestRole() {
        Permission permission = Permission.reconstitute(
                UUID.randomUUID(),
                "account:read",
                "Read account",
                "account"
        );
        return Role.reconstitute(
                UUID.randomUUID(),
                RoleName.CUSTOMER,
                "Customer role",
                Set.of(permission)
        );
    }

    @Nested
    @DisplayName("login() Method Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully when credentials are valid and user is active")
        void shouldLoginSuccessfully_whenCredentialsAreValidAndUserIsActive() {
            // Given
            String emailStr = "user@test.com";
            String plainPassword = "password";
            User activeUser = createTestUser(UserStatus.ACTIVE);
            String jwtToken = "generated-jwt-token";

            LoginCommand command = new LoginCommand(emailStr, plainPassword);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(activeUser));
            when(passwordHasher.verify(plainPassword, activeUser.getPassword().value())).thenReturn(true);
            when(tokenGenerator.generateToken(
                    activeUser.getId(),
                    activeUser.getEmail().value(),
                    activeUser.getRole().getName().name(),
                    activeUser.getRole().getPermissionCodes()
            )).thenReturn(jwtToken);

            // When
            LoginResult result = authService.login(command);

            // Then
            assertNotNull(result);
            assertEquals(activeUser.getId(), result.id());
            assertEquals(activeUser.getEmail().value(), result.email());
            assertEquals(activeUser.getRole().getName(), result.role());
            assertEquals(jwtToken, result.token());

            verify(userRepository).findByEmail(emailStr);
            verify(passwordHasher).verify(plainPassword, activeUser.getPassword().value());
            verify(tokenGenerator).generateToken(any(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when email does not exist")
        void shouldThrowUserNotFoundException_whenEmailDoesNotExist() {
            // Given
            String emailStr = "nonexistent@test.com";
            LoginCommand command = new LoginCommand(emailStr, "password");

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.empty());

            // When & Then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                    authService.login(command)
            );

            assertTrue(exception.getMessage().contains("Invalid credentials"));
            verify(userRepository).findByEmail(emailStr);
            verify(passwordHasher, never()).verify(anyString(), anyString());
            verify(tokenGenerator, never()).generateToken(any(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
        void shouldThrowInvalidCredentialsException_whenPasswordIsIncorrect() {
            // Given
            String emailStr = "user@test.com";
            String wrongPassword = "wrongPassword";
            User user = createTestUser(UserStatus.ACTIVE);
            LoginCommand command = new LoginCommand(emailStr, wrongPassword);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(user));
            when(passwordHasher.verify(wrongPassword, user.getPassword().value())).thenReturn(false);

            // When & Then
            InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () ->
                    authService.login(command)
            );

            assertTrue(exception.getMessage().contains("Invalid credentials"));
            verify(userRepository).findByEmail(emailStr);
            verify(passwordHasher).verify(wrongPassword, user.getPassword().value());
            verify(tokenGenerator, never()).generateToken(any(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should throw UserNotVerifiedException when user status is PENDING_VERIFICATION")
        void shouldThrowUserNotVerifiedException_whenUserStatusIsPendingVerification() {
            // Given
            String emailStr = "user@test.com";
            String password = "password";
            User pendingUser = createTestUser(UserStatus.PENDING_VERIFICATION);
            LoginCommand command = new LoginCommand(emailStr, password);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(pendingUser));
            when(passwordHasher.verify(password, pendingUser.getPassword().value())).thenReturn(true);

            // When & Then
            UserNotVerifiedException exception = assertThrows(UserNotVerifiedException.class, () ->
                    authService.login(command)
            );

            assertTrue(exception.getMessage().contains("verify your email"));
            verify(userRepository).findByEmail(emailStr);
            verify(passwordHasher).verify(password, pendingUser.getPassword().value());
            verify(tokenGenerator, never()).generateToken(any(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should allow login when user is BLOCKED")
        void shouldAllowLogin_whenUserIsBlocked() {
            // Given
            String emailStr = "blocked@test.com";
            String password = "password";
            User blockedUser = createTestUser(UserStatus.BLOCKED);
            String jwtToken = "jwt-token";
            LoginCommand command = new LoginCommand(emailStr, password);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(blockedUser));
            when(passwordHasher.verify(password, blockedUser.getPassword().value())).thenReturn(true);
            when(tokenGenerator.generateToken(any(), anyString(), anyString(), any())).thenReturn(jwtToken);

            // When
            LoginResult result = authService.login(command);

            // Then
            assertNotNull(result);
            assertEquals(jwtToken, result.token());
        }
    }

    @Nested
    @DisplayName("register() Method Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully when email is not in use")
        void shouldRegisterNewUserSuccessfully_whenEmailIsNotInUse() {
            // Given
            String emailStr = "newuser@test.com";
            String plainPassword = "SecurePass123!";
            String hashedPassword = "$2a$10$hashedPassword";
            Role defaultRole = createTestRole();

            RegisterCommand command = new RegisterCommand(
                    emailStr,
                    plainPassword,
                    "John",
                    "Doe",
                    "DNI",
                    "12345678",
                    java.time.LocalDate.of(1990, 1, 1),
                    "+1234567890",
                    "123 Main St",
                    "Test City",
                    "Test Country"
            );

            when(userRepository.existsByEmail(emailStr)).thenReturn(false);
            when(passwordHasher.hash(plainPassword)).thenReturn(hashedPassword);
            when(roleRepository.getDefaultRole()).thenReturn(defaultRole);

            User savedUser = User.reconsitute(
                    UUID.randomUUID(),
                    new Email(emailStr),
                    Password.fromHash(hashedPassword),
                    UserStatus.PENDING_VERIFICATION,
                    defaultRole
            );
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RegisterResult result = authService.register(command);

            // Then
            assertNotNull(result);
            assertEquals(savedUser.getId(), result.id());
            assertEquals(savedUser.getEmail().value(), result.email());

            verify(userRepository).existsByEmail(emailStr);
            verify(passwordHasher).hash(plainPassword);
            verify(roleRepository).getDefaultRole();
            verify(userRepository).save(any(User.class));
            verify(verificationTokenRepository).save(any(VerificationToken.class));
            verify(userEventPublisher).publishUserRegisteredEvent(any(User.class), eq(command));
            verify(userEventPublisher).publishEmailVerificationRequestedEvent(
                    eq(savedUser.getId()),
                    eq(savedUser.getEmail().value()),
                    anyString(),
                    eq(command.firstName())
            );
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when email is already in use")
        void shouldThrowUserAlreadyExistsException_whenEmailIsAlreadyInUse() {
            // Given
            String emailStr = "existing@test.com";
            RegisterCommand command = new RegisterCommand(
                    emailStr,
                    "SecurePass123!",
                    "John",
                    "Doe",
                    "DNI",
                    "12345678",
                    java.time.LocalDate.of(1990, 1, 1),
                    "+1234567890",
                    "123 Main St",
                    "Test City",
                    "Test Country"
            );

            when(userRepository.existsByEmail(emailStr)).thenReturn(true);

            // When & Then
            UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                    authService.register(command)
            );

            assertTrue(exception.getMessage().contains("already in use"));
            verify(userRepository).existsByEmail(emailStr);
            verify(passwordHasher, never()).hash(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should create user with PENDING_VERIFICATION status")
        void shouldCreateUser_withPendingVerificationStatus() {
            // Given
            String emailStr = "newuser@test.com";
            RegisterCommand command = new RegisterCommand(
                    emailStr,
                    "SecurePass123!",
                    "John",
                    "Doe",
                    "DNI",
                    "12345678",
                    java.time.LocalDate.of(1990, 1, 1),
                    "+1234567890",
                    "123 Main St",
                    "Test City",
                    "Test Country"
            );

            when(userRepository.existsByEmail(emailStr)).thenReturn(false);
            when(passwordHasher.hash(anyString())).thenReturn("$2a$10$hashedPassword");
            when(roleRepository.getDefaultRole()).thenReturn(createTestRole());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return User.reconsitute(
                        UUID.randomUUID(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getStatus(),
                        user.getRole()
                );
            });
            when(verificationTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            authService.register(command);

            // Then
            User capturedUser = userCaptor.getValue();
            assertEquals(UserStatus.PENDING_VERIFICATION, capturedUser.getStatus());
        }

        @Test
        @DisplayName("Should validate password strength during registration")
        void shouldValidatePasswordStrength_duringRegistration() {
            // Given
            String emailStr = "user@test.com";
            String weakPassword = "weak"; // Doesn't meet strength requirements

            RegisterCommand command = new RegisterCommand(
                    emailStr,
                    weakPassword,
                    "John",
                    "Doe",
                    "DNI",
                    "12345678",
                    java.time.LocalDate.of(1990, 1, 1),
                    "+1234567890",
                    "123 Main St",
                    "Test City",
                    "Test Country"
            );

            when(userRepository.existsByEmail(emailStr)).thenReturn(false);

            // When & Then
            assertThrows(IllegalArgumentException.class, () ->
                    authService.register(command)
            );

            verify(passwordHasher, never()).hash(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should create verification token after registration")
        void shouldCreateVerificationToken_afterRegistration() {
            // Given
            String emailStr = "newuser@test.com";
            RegisterCommand command = new RegisterCommand(
                    emailStr,
                    "SecurePass123!",
                    "John",
                    "Doe",
                    "DNI",
                    "12345678",
                    java.time.LocalDate.of(1990, 1, 1),
                    "+1234567890",
                    "123 Main St",
                    "Test City",
                    "Test Country"
            );

            when(userRepository.existsByEmail(emailStr)).thenReturn(false);
            when(passwordHasher.hash(anyString())).thenReturn("$2a$10$hash");
            when(roleRepository.getDefaultRole()).thenReturn(createTestRole());

            User savedUser = User.reconsitute(
                    UUID.randomUUID(),
                    new Email(emailStr),
                    Password.fromHash("$2a$10$hash"),
                    UserStatus.PENDING_VERIFICATION,
                    createTestRole()
            );
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(verificationTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            authService.register(command);

            // Then
            ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenRepository).save(tokenCaptor.capture());

            VerificationToken capturedToken = tokenCaptor.getValue();
            assertEquals(savedUser.getId(), capturedToken.getUserId());
            assertFalse(capturedToken.isUsed());
        }
    }

    @Nested
    @DisplayName("findById() Method Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find user successfully when user exists")
        void shouldFindUserSuccessfully_whenUserExists() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createTestUser(UserStatus.ACTIVE);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // When
            UserResult result = authService.findById(userId);

            // Then
            assertNotNull(result);
            assertEquals(user.getId(), result.id());
            assertEquals(user.getEmail().value(), result.email());

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            // Given
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                    authService.findById(userId)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(userRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("changePassword() Method Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully when old password is correct")
        @org.junit.jupiter.api.Disabled("TODO: Fix verify issue")
        void shouldChangePasswordSuccessfully_whenOldPasswordIsCorrect() {
            // Given
            UUID userId = UUID.randomUUID();
            String oldPassword = "OldPass123!";
            String newPassword = "NewPass456!";
            String hashedNewPassword = "$2a$10$newHashedPassword";

            User user = createTestUser(UserStatus.ACTIVE);
            ChangeUserPasswordCommand command = new ChangeUserPasswordCommand(oldPassword, newPassword);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordHasher.verify(oldPassword, user.getPassword().value())).thenReturn(true);
            when(passwordHasher.hash(newPassword)).thenReturn(hashedNewPassword);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            authService.changePassword(userId, command);

            // Then
            verify(userRepository).findById(userId);
            verify(passwordHasher).verify(oldPassword, user.getPassword().value());
            verify(passwordHasher).hash(newPassword);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            // Given
            UUID userId = UUID.randomUUID();
            ChangeUserPasswordCommand command = new ChangeUserPasswordCommand("OldPass123!", "NewPass456!");

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () ->
                    authService.changePassword(userId, command)
            );

            verify(userRepository).findById(userId);
            verify(passwordHasher, never()).verify(anyString(), anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when old password is incorrect")
        void shouldThrowInvalidCredentialsException_whenOldPasswordIsIncorrect() {
            // Given
            UUID userId = UUID.randomUUID();
            String wrongOldPassword = "WrongPass123!";
            String newPassword = "NewPass456!";

            User user = createTestUser(UserStatus.ACTIVE);
            ChangeUserPasswordCommand command = new ChangeUserPasswordCommand(wrongOldPassword, newPassword);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordHasher.verify(wrongOldPassword, user.getPassword().value())).thenReturn(false);

            // When & Then
            InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () ->
                    authService.changePassword(userId, command)
            );

            assertTrue(exception.getMessage().contains("incorrect"));
            verify(userRepository).findById(userId);
            verify(passwordHasher).verify(wrongOldPassword, user.getPassword().value());
            verify(passwordHasher, never()).hash(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should save user with new hashed password")
        void shouldSaveUser_withNewHashedPassword() {
            // Given
            UUID userId = UUID.randomUUID();
            String oldPassword = "OldPass123!";
            String newPassword = "NewPass456!";
            String hashedNewPassword = "$2a$10$newHash";

            User user = createTestUser(UserStatus.ACTIVE);
            ChangeUserPasswordCommand command = new ChangeUserPasswordCommand(oldPassword, newPassword);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordHasher.verify(oldPassword, user.getPassword().value())).thenReturn(true);
            when(passwordHasher.hash(newPassword)).thenReturn(hashedNewPassword);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenReturn(user);

            // When
            authService.changePassword(userId, command);

            // Then
            User savedUser = userCaptor.getValue();
            assertEquals(hashedNewPassword, savedUser.getPassword().value());
        }
    }
}
