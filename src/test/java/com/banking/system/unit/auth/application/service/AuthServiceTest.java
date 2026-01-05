package com.banking.system.unit.auth.application.service;

import com.banking.system.auth.application.dto.LoginCommand;
import com.banking.system.auth.application.dto.LoginResult;
import com.banking.system.auth.application.dto.RegisterCommand;
import com.banking.system.auth.application.dto.RegisterResult;
import com.banking.system.auth.application.event.publisher.UserEventPublisher;
import com.banking.system.auth.application.service.AuthService;
import com.banking.system.auth.domain.exception.InvalidCredentalsException;
import com.banking.system.auth.domain.exception.UserAlreadyExistsException;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import com.banking.system.auth.domain.model.Role;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.domain.model.UserStatus;
import com.banking.system.auth.domain.port.out.PasswordHasher;
import com.banking.system.auth.domain.port.out.TokenGenerator;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenGenerator tokenGenerator;

    @InjectMocks
    private AuthService authService;

    @Test
    public void login_whenValidCredentials_shouldReturnLoginResult() {
        String email = "test@example.com";
        String password = "password123";
        String passwordHash = "hashedPassword";
        UUID userId = UUID.randomUUID();
        String token = "generated.jwt.token";

        LoginCommand command = new LoginCommand(email, password);

        User user = new User(
                userId,
                email,
                passwordHash,
                UserStatus.ACTIVE,
                Role.CUSTOMER
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.verify(password, passwordHash)).thenReturn(true);
        when(tokenGenerator.generateToken(userId, email, Role.CUSTOMER.name())).thenReturn(token);

        LoginResult result = authService.login(command);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(email, result.email());
        assertEquals(Role.CUSTOMER, result.role());
        assertEquals(token, result.token());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordHasher, times(1)).verify(password, passwordHash);
        verify(tokenGenerator, times(1)).generateToken(userId, email, Role.CUSTOMER.name());
    }

    @Test
    public void login_whenUserNotFound_shouldThrowException() {
        String email = "nonexistent@example.com";
        String password = "password123";

        LoginCommand command = new LoginCommand(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            authService.login(command);
        });

        String expectedMessage = "Invalid credentials";
        assertEquals(expectedMessage, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordHasher, never()).verify(anyString(), anyString());
        verify(tokenGenerator, never()).generateToken(any(), anyString(), anyString());
    }

    @Test
    public void login_whenInvalidPassword_shouldThrowException() {
        String email = "test@example.com";
        String password = "wrongpassword";
        String passwordHash = "hashedPassword";
        UUID userId = UUID.randomUUID();

        LoginCommand command = new LoginCommand(email, password);

        User user = new User(
                userId,
                email,
                passwordHash,
                UserStatus.ACTIVE,
                Role.CUSTOMER
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.verify(password, passwordHash)).thenReturn(false);

        Exception exception = assertThrows(InvalidCredentalsException.class, () -> {
            authService.login(command);
        });

        String expectedMessage = "Invalid credentials";
        assertEquals(expectedMessage, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordHasher, times(1)).verify(password, passwordHash);
        verify(tokenGenerator, never()).generateToken(any(), anyString(), anyString());
    }

    @Test
    public void register_whenNewUser_shouldCreateAndReturnUser() {
        String email = "newuser@example.com";
        String password = "password123";
        String hashedPassword = "hashedPassword";
        UUID userId = UUID.randomUUID();

        RegisterCommand command = new RegisterCommand(
                email,
                password,
                "John",
                "Doe",
                "DNI",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "+1234567890",
                "123 Main St",
                "New York",
                "USA"
        );

        User savedUser = new User(
                userId,
                email,
                hashedPassword,
                UserStatus.PENDING_VERIFICATION,
                Role.CUSTOMER
        );

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordHasher.hash(password)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResult result = authService.register(command);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(email, result.email());

        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordHasher, times(1)).hash(password);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userEventPublisher, times(1)).publishUserRegisteredEvent(savedUser, command);
    }

    @Test
    public void register_whenEmailAlreadyExists_shouldThrowException() {
        String email = "existing@example.com";
        String password = "password123";

        RegisterCommand command = new RegisterCommand(
                email,
                password,
                "John",
                "Doe",
                "DNI",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "+1234567890",
                "123 Main St",
                "New York",
                "USA"
        );

        when(userRepository.existsByEmail(email)).thenReturn(true);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(command);
        });

        String expectedMessage = "Email already in use";
        assertEquals(expectedMessage, exception.getMessage());

        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordHasher, never()).hash(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userEventPublisher, never()).publishUserRegisteredEvent(any(), any());
    }
}
