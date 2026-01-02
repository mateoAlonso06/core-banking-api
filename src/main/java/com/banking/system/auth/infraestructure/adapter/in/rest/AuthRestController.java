package com.banking.system.auth.infraestructure.adapter.in.rest;

import com.banking.system.auth.application.dto.LoginCommand;
import com.banking.system.auth.application.dto.LoginResult;
import com.banking.system.auth.application.usecase.LoginUseCase;
import com.banking.system.auth.application.usecase.RegisterUseCase;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.LoginRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.LoginResponse;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserResponse;
import com.banking.system.auth.infraestructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Validated
public class AuthRestController {
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password()
        );

        LoginResult result = loginUseCase.login(command);

        String token = jwtTokenProvider.generateToken(
                result.id().toString(),
                result.email(),
                result.role().name()
        );

        LoginResponse response = new LoginResponse(
                result.id(),
                result.email(),
                token
        );

        return ResponseEntity.ok(response);
    }
}
