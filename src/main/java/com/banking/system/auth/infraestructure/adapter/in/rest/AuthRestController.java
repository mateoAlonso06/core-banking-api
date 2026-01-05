package com.banking.system.auth.infraestructure.adapter.in.rest;

import com.banking.system.auth.application.dto.LoginCommand;
import com.banking.system.auth.application.dto.LoginResult;
import com.banking.system.auth.application.dto.RegisterCommand;
import com.banking.system.auth.application.dto.RegisterResult;
import com.banking.system.auth.application.usecase.LoginUseCase;
import com.banking.system.auth.application.usecase.RegisterUseCase;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.LoginRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.LoginResponse;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthRestController {
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
        RegisterCommand command = new RegisterCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.documentType(),
                request.documentNumber(),
                request.birthDate(),
                request.phone(),
                request.address(),
                request.city(),
                request.country()
        );

        RegisterResult result = registerUseCase.register(command);

        RegisterUserResponse response = new RegisterUserResponse(
                result.id(),
                result.email()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password()
        );

        LoginResult result = loginUseCase.login(command);

        LoginResponse response = new LoginResponse(
                result.id(),
                result.email(),
                result.token()
        );

        return ResponseEntity.ok(response);
    }
}
