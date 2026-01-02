package com.banking.system.auth.application.service;

import com.banking.system.auth.application.dto.LoginCommand;
import com.banking.system.auth.application.dto.LoginResult;
import com.banking.system.auth.application.dto.RegisterCommand;
import com.banking.system.auth.application.dto.RegisterResult;
import com.banking.system.auth.application.usecase.LoginUseCase;
import com.banking.system.auth.domain.exception.InvalidCredentalsException;
import com.banking.system.auth.domain.exception.UserAlreadyExistsException;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import com.banking.system.auth.domain.port.out.PasswordHasher;
import com.banking.system.auth.application.usecase.RegisterUseCase;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import com.banking.system.auth.domain.model.User;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements RegisterUseCase, LoginUseCase {
    private final UserRepositoryPort userRepository;
    private final CustomerRepositoryPort customerRepository;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional
    public LoginResult login(LoginCommand loginCommand) {
        User user = userRepository.findByEmail(loginCommand.email())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!passwordHasher.verify(loginCommand.password(), user.getPasswordHash()))
            throw new InvalidCredentalsException("Invalid credentials");

        return new LoginResult(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    @Transactional
    public RegisterResult register(RegisterCommand registerUserRequest) {
        if (userRepository.existsByEmail(registerUserRequest.email()))
            throw new UserAlreadyExistsException("Email already in use");

        if (customerRepository.existsByDocumentNumber(registerUserRequest.documentNumber()))
            throw new UserAlreadyExistsException("Document number already in use");

        String hashedPassword = passwordHasher.hash(registerUserRequest.password());

        User user = User.createNew(registerUserRequest.email(), hashedPassword);

        User savedUser = userRepository.create(user);

        // Crear Customer con validación de dominio
        Customer customer = Customer.createNew(
                savedUser.getId(),
                registerUserRequest.firstName(),
                registerUserRequest.lastName(),
                registerUserRequest.documentType(),
                registerUserRequest.documentNumber(),
                registerUserRequest.birthDate(),
                registerUserRequest.phone(),
                java.time.LocalDate.now(),
                Customer.KycStatus.PENDING,
                Customer.RiskLevel.LOW
        );

        Customer savedCustomer = customerRepository.save(customer);

        return new RegisterResult(
                savedCustomer.getId(),
                savedUser.getEmail()
        );
    }
}
