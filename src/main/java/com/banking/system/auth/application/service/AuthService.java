package com.banking.system.auth.application.service;

import com.banking.system.auth.domain.exception.UserAlreadyExistsException;
import com.banking.system.auth.domain.port.out.PasswordHasher;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserResponse;
import com.banking.system.auth.application.usecase.RegisterUseCase;
import com.banking.system.auth.domain.port.out.UserRepository;
import com.banking.system.auth.domain.model.User;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements RegisterUseCase {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional
    public RegisterUserResponse register(RegisterUserRequest registerUserRequest) {
        if (userRepository.existsByEmail(registerUserRequest.email()))
            throw new UserAlreadyExistsException("Email already in use");

        if (customerRepository.existsByDocumentNumber(registerUserRequest.documentNumber()))
            throw new UserAlreadyExistsException("Document number already in use");

        String hashedPassword = passwordHasher.hash(registerUserRequest.password());

        User user = new User(registerUserRequest.email(), hashedPassword);
        User savedUser = userRepository.save(user);

        // El id se auto-genera con UUID.randomUUID() en el constructor de User
        Customer customer = Customer.builder()
                .userId(savedUser.getId())
                .firstName(registerUserRequest.firstName())
                .lastName(registerUserRequest.lastName())
                .documentType(registerUserRequest.documentType())
                .documentNumber(registerUserRequest.documentNumber())
                .birthDate(registerUserRequest.birthDate())
                .phone(registerUserRequest.phone())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return new RegisterUserResponse(
                savedCustomer.getId(),
                savedUser.getEmail()
        );
    }
}
