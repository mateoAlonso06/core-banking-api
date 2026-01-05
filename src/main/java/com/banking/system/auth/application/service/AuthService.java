package com.banking.system.auth.application.service;

import com.banking.system.auth.application.dto.LoginCommand;
import com.banking.system.auth.application.dto.LoginResult;
import com.banking.system.auth.application.dto.RegisterCommand;
import com.banking.system.auth.application.dto.RegisterResult;
import com.banking.system.auth.application.event.publisher.UserEventPublisher;
import com.banking.system.auth.application.usecase.LoginUseCase;
import com.banking.system.auth.application.usecase.RegisterUseCase;
import com.banking.system.auth.domain.exception.InvalidCredentalsException;
import com.banking.system.auth.domain.exception.UserAlreadyExistsException;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.domain.port.out.PasswordHasher;
import com.banking.system.auth.domain.port.out.TokenGenerator;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements RegisterUseCase, LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final UserEventPublisher userEventPublisher;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!passwordHasher.verify(command.password(), user.getPasswordHash()))
            throw new InvalidCredentalsException("Invalid credentials");

        String token = tokenGenerator.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResult(
                user.getId(),
                user.getEmail(),
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

        String hashedPassword = passwordHasher.hash(command.password());
        User user = User.createNew(command.email(), hashedPassword);
        User savedUser = userRepository.save(user);

        userEventPublisher.publishUserRegisteredEvent(savedUser, command);

        return new RegisterResult(savedUser.getId(), savedUser.getEmail());
    }
}
