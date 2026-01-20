package com.banking.system.auth.application.service;

import com.banking.system.auth.application.dto.command.ChangeUserPasswordCommand;
import com.banking.system.auth.application.dto.command.LoginCommand;
import com.banking.system.auth.application.dto.command.RegisterCommand;
import com.banking.system.auth.application.dto.result.LoginResult;
import com.banking.system.auth.application.dto.result.RegisterResult;
import com.banking.system.auth.application.dto.result.UserResult;
import com.banking.system.auth.application.event.publisher.UserEventPublisher;
import com.banking.system.auth.application.usecase.ChangePasswordUseCase;
import com.banking.system.auth.application.usecase.FindUserByIdUseCase;
import com.banking.system.auth.application.usecase.LoginUseCase;
import com.banking.system.auth.application.usecase.RegisterUseCase;
import com.banking.system.auth.domain.exception.InvalidCredentalsException;
import com.banking.system.auth.domain.exception.UserAlreadyExistsException;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import com.banking.system.auth.domain.model.Email;
import com.banking.system.auth.domain.model.Password;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.domain.port.out.PasswordHasher;
import com.banking.system.auth.domain.port.out.TokenGenerator;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements
        RegisterUseCase,
        LoginUseCase,
        FindUserByIdUseCase,
        ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final UserEventPublisher userEventPublisher;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!passwordHasher.verify(command.password(), user.getPassword().value()))
            throw new InvalidCredentalsException("Invalid credentials");

        String token = tokenGenerator.generateToken(
                user.getId(),
                user.getEmail().value(),
                user.getRole().name()
        );

        return new LoginResult(
                user.getId(),
                user.getEmail().value(),
                user.getRole(),
                token
        );
    }

    @Override
    @Transactional
    public RegisterResult register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Validates password strength (length, regex...)
        Password plainPassword = Password.fromPlainPassword(command.password());
        Password hashedPassword = Password.fromHash(passwordHasher.hash(plainPassword.value()));

        User user = User.createNew(
                new Email(command.email()),
                hashedPassword
        );
        User savedUser = userRepository.save(user);

        userEventPublisher.publishUserRegisteredEvent(savedUser, command);

        return new RegisterResult(savedUser.getId(), savedUser.getEmail().value());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResult findById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserResult(user.getId(), user.getEmail().value());
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangeUserPasswordCommand command) {
        log.info("Changing password for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordHasher.verify(command.oldPassword(), user.getPassword().value())) {
            throw new InvalidCredentalsException("Old password is incorrect");
        }

        Password hashedNewPassword = Password.fromHash(passwordHasher.hash(command.newPassword()));

        user.changePassword(hashedNewPassword);
        userRepository.save(user);

        log.info("Password changed successfully for user with ID: {}", userId);
    }
}
