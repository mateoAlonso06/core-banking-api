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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "User registration, login, and password management")
public class AuthRestController {
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with email and password. Also creates associated customer profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (validation failed)"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResult> register(@RequestBody @Valid RegisterUserRequest request) {
        var command = request.toCommand();
        var result = registerUseCase.register(command);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "User login",
            description = "Authenticates user with email and password. Returns JWT token on success."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (validation failed)"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody @Valid LoginRequest request) {
        var command = new LoginCommand(
                request.email(),
                request.password()
        );
        var result = loginUseCase.login(command);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Change password",
            description = "Changes the password for the authenticated user. Requires current password verification."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (validation failed)"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token, or incorrect current password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid ChangeUserPasswordRequest request) {
        changePasswordUseCase.changePassword(userId, request.toCommand());
        return ResponseEntity.noContent().build();
    }
}
