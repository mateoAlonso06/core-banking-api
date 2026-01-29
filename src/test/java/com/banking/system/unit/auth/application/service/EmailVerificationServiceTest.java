package com.banking.system.unit.auth.application.service;

import com.banking.system.auth.application.dto.command.ResendVerificationCommand;
import com.banking.system.auth.application.dto.command.VerifyEmailCommand;
import com.banking.system.auth.application.event.publisher.UserEventPublisher;
import com.banking.system.auth.application.service.EmailVerificationService;
import com.banking.system.auth.domain.exception.InvalidVerificationTokenException;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import com.banking.system.auth.domain.exception.UserNotVerifiedException;
import com.banking.system.auth.domain.model.*;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import com.banking.system.auth.domain.port.out.VerificationTokenRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService Tests")
class EmailVerificationServiceTest {

    @Mock
    private VerificationTokenRepositoryPort tokenRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserEventPublisher eventPublisher;
    @InjectMocks
    private EmailVerificationService emailVerificationService;

    // Helper methods
    private User createTestUser(UUID userId, UserStatus status, String email) {
        Role role = createTestRole();
        return User.reconsitute(
                userId,
                new Email(email),
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
    @DisplayName("verifyEmail() Method Tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify email successfully when token is valid and not expired")
        void shouldVerifyEmailSuccessfully_whenTokenIsValidAndNotExpired() {
            // Given
            UUID userId = UUID.randomUUID();
            String tokenString = "valid-token-123";
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    userId,
                    tokenString,
                    LocalDateTime.now().plusMinutes(10),
                    false
            );
            User user = createTestUser(userId, UserStatus.PENDING_VERIFICATION, "user@test.com");

            VerifyEmailCommand command = new VerifyEmailCommand(tokenString);

            when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(VerificationToken.class))).thenReturn(token);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            emailVerificationService.verifyEmail(command);

            // Then
            verify(tokenRepository).findByToken(tokenString);
            verify(userRepository).findById(userId);
            verify(tokenRepository).save(token);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw InvalidVerificationTokenException when token does not exist")
        void shouldThrowInvalidVerificationTokenException_whenTokenDoesNotExist() {
            // Given
            String nonExistentToken = "non-existent-token";
            VerifyEmailCommand command = new VerifyEmailCommand(nonExistentToken);

            when(tokenRepository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

            // When & Then
            InvalidVerificationTokenException exception = assertThrows(
                    InvalidVerificationTokenException.class, () ->
                            emailVerificationService.verifyEmail(command)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(tokenRepository).findByToken(nonExistentToken);
            verify(tokenRepository, never()).save(any());
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw InvalidVerificationTokenException when token is already used")
        void shouldThrowInvalidVerificationTokenException_whenTokenIsAlreadyUsed() {
            // Given
            UUID userId = UUID.randomUUID();
            String tokenString = "used-token";
            VerificationToken usedToken = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    userId,
                    tokenString,
                    LocalDateTime.now().plusMinutes(10),
                    true // already used
            );

            VerifyEmailCommand command = new VerifyEmailCommand(tokenString);

            when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(usedToken));

            // When & Then
            InvalidVerificationTokenException exception = assertThrows(
                    InvalidVerificationTokenException.class, () ->
                            emailVerificationService.verifyEmail(command)
            );

            assertTrue(exception.getMessage().contains("already been used"));
            verify(tokenRepository).findByToken(tokenString);
            verify(tokenRepository, never()).save(any());
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw InvalidVerificationTokenException when token is expired")
        void shouldThrowInvalidVerificationTokenException_whenTokenIsExpired() {
            // Given
            UUID userId = UUID.randomUUID();
            String tokenString = "expired-token";
            VerificationToken expiredToken = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    userId,
                    tokenString,
                    LocalDateTime.now().minusMinutes(1), // expired
                    false
            );

            VerifyEmailCommand command = new VerifyEmailCommand(tokenString);

            when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(expiredToken));

            // When & Then
            InvalidVerificationTokenException exception = assertThrows(
                    InvalidVerificationTokenException.class, () ->
                            emailVerificationService.verifyEmail(command)
            );

            assertTrue(exception.getMessage().contains("expired"));
            verify(tokenRepository).findByToken(tokenString);
            verify(tokenRepository, never()).save(any());
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            // Given
            UUID userId = UUID.randomUUID();
            String tokenString = "valid-token";
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    userId,
                    tokenString,
                    LocalDateTime.now().plusMinutes(10),
                    false
            );

            VerifyEmailCommand command = new VerifyEmailCommand(tokenString);

            when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                    emailVerificationService.verifyEmail(command)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(tokenRepository).findByToken(tokenString);
            verify(tokenRepository).save(token);
            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should mark token as used before activating user")
        void shouldMarkTokenAsUsed_beforeActivatingUser() {
            // Given
            UUID userId = UUID.randomUUID();
            String tokenString = "valid-token";
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    userId,
                    tokenString,
                    LocalDateTime.now().plusMinutes(10),
                    false
            );
            User user = createTestUser(userId, UserStatus.PENDING_VERIFICATION, "user@test.com");

            VerifyEmailCommand command = new VerifyEmailCommand(tokenString);

            when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));
            when(tokenRepository.save(any(VerificationToken.class))).thenReturn(token);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            emailVerificationService.verifyEmail(command);

            // Then
            ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());
            assertTrue(tokenCaptor.getValue().isUsed());
        }

        @Test
        @DisplayName("Should activate user after marking token as used")
        void shouldActivateUser_afterMarkingTokenAsUsed() {
            // Given
            UUID userId = UUID.randomUUID();
            String tokenString = "valid-token";
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    userId,
                    tokenString,
                    LocalDateTime.now().plusMinutes(10),
                    false
            );
            User user = createTestUser(userId, UserStatus.PENDING_VERIFICATION, "user@test.com");

            VerifyEmailCommand command = new VerifyEmailCommand(tokenString);

            when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));
            when(tokenRepository.save(any(VerificationToken.class))).thenReturn(token);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            emailVerificationService.verifyEmail(command);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(UserStatus.ACTIVE, userCaptor.getValue().getStatus());
        }
    }

    @Nested
    @DisplayName("resendVerificationEmail() Method Tests")
    class ResendVerificationEmailTests {

        @Test
        @DisplayName("Should resend verification email successfully when user is pending verification")
        void shouldResendVerificationEmailSuccessfully_whenUserIsPendingVerification() {
            // Given
            UUID userId = UUID.randomUUID();
            String emailStr = "pending@test.com";
            User pendingUser = createTestUser(userId, UserStatus.PENDING_VERIFICATION, emailStr);

            ResendVerificationCommand command = new ResendVerificationCommand(emailStr);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(pendingUser));
            when(tokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            emailVerificationService.resendVerificationEmail(command);

            // Then
            verify(userRepository).findByEmail(emailStr);
            verify(tokenRepository).save(any(VerificationToken.class));
            verify(eventPublisher).publishEmailVerificationRequestedEvent(
                    eq(userId),
                    eq(emailStr),
                    anyString(),
                    isNull()
            );
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            // Given
            String emailStr = "nonexistent@test.com";
            ResendVerificationCommand command = new ResendVerificationCommand(emailStr);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.empty());

            // When & Then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                    emailVerificationService.resendVerificationEmail(command)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(userRepository).findByEmail(emailStr);
            verify(tokenRepository, never()).save(any());
            verify(eventPublisher, never()).publishEmailVerificationRequestedEvent(
                    any(), anyString(), anyString(), any()
            );
        }

        @Test
        @DisplayName("Should throw UserNotVerifiedException when user is already verified")
        void shouldThrowUserNotVerifiedException_whenUserIsAlreadyVerified() {
            // Given
            UUID userId = UUID.randomUUID();
            String emailStr = "active@test.com";
            User activeUser = createTestUser(userId, UserStatus.ACTIVE, emailStr);

            ResendVerificationCommand command = new ResendVerificationCommand(emailStr);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(activeUser));

            // When & Then
            UserNotVerifiedException exception = assertThrows(UserNotVerifiedException.class, () ->
                    emailVerificationService.resendVerificationEmail(command)
            );

            assertTrue(exception.getMessage().contains("already verified"));
            verify(userRepository).findByEmail(emailStr);
            verify(tokenRepository, never()).save(any());
            verify(eventPublisher, never()).publishEmailVerificationRequestedEvent(
                    any(), anyString(), anyString(), any()
            );
        }

        @Test
        @DisplayName("Should throw UserNotVerifiedException when user is blocked")
        void shouldThrowUserNotVerifiedException_whenUserIsBlocked() {
            // Given
            UUID userId = UUID.randomUUID();
            String emailStr = "blocked@test.com";
            User blockedUser = createTestUser(userId, UserStatus.BLOCKED, emailStr);

            ResendVerificationCommand command = new ResendVerificationCommand(emailStr);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(blockedUser));

            // When & Then
            UserNotVerifiedException exception = assertThrows(UserNotVerifiedException.class, () ->
                    emailVerificationService.resendVerificationEmail(command)
            );

            assertTrue(exception.getMessage().contains("already verified"));
            verify(userRepository).findByEmail(emailStr);
            verify(tokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create new verification token when resending")
        void shouldCreateNewVerificationToken_whenResending() {
            // Given
            UUID userId = UUID.randomUUID();
            String emailStr = "pending@test.com";
            User pendingUser = createTestUser(userId, UserStatus.PENDING_VERIFICATION, emailStr);

            ResendVerificationCommand command = new ResendVerificationCommand(emailStr);

            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(pendingUser));
            when(tokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            emailVerificationService.resendVerificationEmail(command);

            // Then
            ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            VerificationToken capturedToken = tokenCaptor.getValue();
            assertEquals(userId, capturedToken.getUserId());
            assertFalse(capturedToken.isUsed());
            assertFalse(capturedToken.isExpired());
        }

        @Test
        @DisplayName("Should publish email verification event after creating token")
        void shouldPublishEmailVerificationEvent_afterCreatingToken() {
            // Given
            UUID userId = UUID.randomUUID();
            String emailStr = "pending@test.com";
            User pendingUser = createTestUser(userId, UserStatus.PENDING_VERIFICATION, emailStr);

            ResendVerificationCommand command = new ResendVerificationCommand(emailStr);

            ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(pendingUser));
            when(tokenRepository.save(tokenCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            emailVerificationService.resendVerificationEmail(command);

            // Then
            VerificationToken savedToken = tokenCaptor.getValue();
            verify(eventPublisher).publishEmailVerificationRequestedEvent(
                    eq(userId),
                    eq(emailStr),
                    eq(savedToken.getToken()),
                    isNull()
            );
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenariosTests {

        @Test
        @DisplayName("Should complete full verification flow: resend -> verify")
        void shouldCompleteFullVerificationFlow() {
            // Given
            UUID userId = UUID.randomUUID();
            String emailStr = "user@test.com";
            User pendingUser = createTestUser(userId, UserStatus.PENDING_VERIFICATION, emailStr);

            // Resend
            ResendVerificationCommand resendCommand = new ResendVerificationCommand(emailStr);
            when(userRepository.findByEmail(emailStr)).thenReturn(Optional.of(pendingUser));

            ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            when(tokenRepository.save(tokenCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            emailVerificationService.resendVerificationEmail(resendCommand);
            VerificationToken newToken = tokenCaptor.getValue();

            // Verify
            VerifyEmailCommand verifyCommand = new VerifyEmailCommand(newToken.getToken());
            when(tokenRepository.findByToken(newToken.getToken())).thenReturn(Optional.of(newToken));
            when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser));
            when(userRepository.save(any(User.class))).thenReturn(pendingUser);

            // When
            emailVerificationService.verifyEmail(verifyCommand);

            // Then
            verify(eventPublisher).publishEmailVerificationRequestedEvent(
                    eq(userId), eq(emailStr), eq(newToken.getToken()), isNull()
            );
            verify(tokenRepository, times(2)).save(any(VerificationToken.class));
            verify(userRepository).save(any(User.class));
        }
    }
}
