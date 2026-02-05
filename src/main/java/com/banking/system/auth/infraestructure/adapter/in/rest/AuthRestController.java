package com.banking.system.auth.infraestructure.adapter.in.rest;

import com.banking.system.auth.application.dto.command.LoginCommand;
import com.banking.system.auth.application.dto.command.ResendVerificationCommand;
import com.banking.system.auth.application.dto.command.VerifyEmailCommand;
import com.banking.system.auth.application.dto.result.LoginResult;
import com.banking.system.auth.application.dto.result.RegisterResult;
import com.banking.system.auth.application.usecase.*;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.request.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with email and password. Also creates associated customer profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (validation failed)"),
            @ApiResponse(responseCode = "409", description = "Email already in use"),
            @ApiResponse(responseCode = "422", description = "Invalid data provided")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResult> register(@RequestBody @Valid RegisterUserRequest request) {
        var command = request.toCommand();

        var result = registerUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
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
            summary = "Verify email",
            description = "Verifies a user's email address using the token sent during registration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "422", description = "Invalid or expired verification token")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        verifyEmailUseCase.verifyEmail(new VerifyEmailCommand(request.token()));
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Resend verification email",
            description = "Resends the email verification link to the user's email address."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification email resent"),
            @ApiResponse(responseCode = "403", description = "User is already verified"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestBody @Valid ResendVerificationRequest request) {
        resendVerificationEmailUseCase.resendVerificationEmail(
                new ResendVerificationCommand(request.email())
        );
        return ResponseEntity.ok().build();
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
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid ChangeUserPasswordRequest request) {
        changePasswordUseCase.changePassword(userId, request.toCommand());
        return ResponseEntity.noContent().build();
    }
}
