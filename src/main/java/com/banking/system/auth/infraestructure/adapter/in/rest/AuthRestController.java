package com.banking.system.auth.infraestructure.adapter.in.rest;

import com.banking.system.auth.application.dto.command.LoginCommand;
import com.banking.system.auth.application.dto.result.LoginResult;
import com.banking.system.auth.application.dto.result.RegisterResult;
import com.banking.system.auth.application.usecase.ChangePasswordUseCase;
import com.banking.system.auth.application.usecase.LoginUseCase;
import com.banking.system.auth.application.usecase.RegisterUseCase;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.request.ChangeUserPasswordRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.request.LoginRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.request.RegisterUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthRestController {
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResult> register(@RequestBody @Valid RegisterUserRequest request) {
        var command = request.toCommand();

        var result = registerUseCase.register(command);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody @Valid LoginRequest request) {
        var command = new LoginCommand(
                request.email(),
                request.password()
        );

        var result = loginUseCase.login(command);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UUID userId,
                                               @RequestBody ChangeUserPasswordRequest request) {
        changePasswordUseCase.changePassword(userId, request.toCommand());
        return ResponseEntity.noContent().build();
    }
}
