package com.banking.system.auth.application.service;

import com.banking.system.auth.application.dto.command.ToggleTwoFactorCommand;
import com.banking.system.auth.application.dto.command.VerifyTwoFactorCommand;
import com.banking.system.auth.application.dto.result.LoginResult;
import com.banking.system.auth.application.dto.result.TwoFactorRequiredResult;
import com.banking.system.auth.application.dto.result.TwoFactorStatusResult;
import com.banking.system.auth.application.event.publisher.UserEventPublisher;
import com.banking.system.auth.application.port.out.LoginTrackingPort;
import com.banking.system.auth.application.usecase.GetTwoFactorStatusUseCase;
import com.banking.system.auth.application.usecase.ToggleTwoFactorUseCase;
import com.banking.system.auth.application.usecase.VerifyTwoFactorUseCase;
import com.banking.system.auth.domain.exception.TwoFactorCodeException;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import com.banking.system.auth.domain.model.Role;
import com.banking.system.auth.domain.model.TwoFactorCode;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.domain.model.RefreshToken;
import com.banking.system.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.banking.system.auth.domain.port.out.TokenGenerator;
import com.banking.system.auth.domain.port.out.TwoFactorCodeRepositoryPort;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorService implements
        VerifyTwoFactorUseCase,
        ToggleTwoFactorUseCase,
        GetTwoFactorStatusUseCase {

    private final TwoFactorCodeRepositoryPort twoFactorCodeRepository;
    private final UserRepositoryPort userRepository;
    private final CustomerRepositoryPort customerRepository;
    private final TokenGenerator tokenGenerator;
    private final UserEventPublisher userEventPublisher;
    private final LoginTrackingPort loginTrackingPort;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Override
    @Transactional
    public LoginResult verify(VerifyTwoFactorCommand command) {
        log.info("Verifying 2FA code for session token: {}", command.sessionToken());

        TwoFactorCode twoFactorCode = twoFactorCodeRepository.findBySessionToken(command.sessionToken())
                .orElseThrow(() -> new TwoFactorCodeException("Invalid or expired session token"));

        if (!twoFactorCode.isValid()) {
            if (twoFactorCode.isExpired()) {
                throw new TwoFactorCodeException("Two-factor code has expired");
            }
            if (twoFactorCode.hasExceededMaxAttempts()) {
                throw new TwoFactorCodeException("Maximum verification attempts exceeded");
            }
            if (twoFactorCode.isUsed()) {
                throw new TwoFactorCodeException("Two-factor code has already been used");
            }
        }

        if (!twoFactorCode.verifyCode(command.code())) {
            twoFactorCode.incrementAttempts();
            twoFactorCodeRepository.save(twoFactorCode);

            int remainingAttempts = 3 - twoFactorCode.getAttempts();
            if (remainingAttempts <= 0) {
                throw new TwoFactorCodeException("Maximum verification attempts exceeded");
            }
            throw new TwoFactorCodeException("Invalid code. " + remainingAttempts + " attempt(s) remaining");
        }

        twoFactorCode.markUsed();
        twoFactorCodeRepository.save(twoFactorCode);

        User user = userRepository.findById(twoFactorCode.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Instant previousLogin = loginTrackingPort.registerLogin(user.getId());

        Role role = user.getRole();
        String accessToken = tokenGenerator.generateToken(
                user.getId(),
                role.getName().name()
        );

        // Single-session policy: revoke any existing sessions before issuing a new one
        refreshTokenRepository.revokeAllByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.createNew(user.getId());
        refreshTokenRepository.save(refreshToken);

        log.info("2FA verification successful for user: {}", user.getId());

        return LoginResult.withToken(
                user.getId(),
                user.getEmail().value(),
                role.getName(),
                accessToken,
                refreshToken.getToken(),
                previousLogin
        );
    }

    @Override
    @Transactional
    public TwoFactorStatusResult toggle(UUID userId, ToggleTwoFactorCommand command) {
        log.info("Toggling 2FA for user: {} to: {}", userId, command.enable());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (command.enable()) {
            user.enableTwoFactor();
        } else {
            user.disableTwoFactor();
        }

        userRepository.save(user);

        log.info("2FA {} for user: {}", command.enable() ? "enabled" : "disabled", userId);

        return new TwoFactorStatusResult(user.isTwoFactorEnabled());
    }

    @Override
    @Transactional(readOnly = true)
    public TwoFactorStatusResult getStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new TwoFactorStatusResult(user.isTwoFactorEnabled());
    }

    @Transactional
    public TwoFactorRequiredResult createTwoFactorCode(User user) {
        TwoFactorCode twoFactorCode = TwoFactorCode.createNew(user.getId());
        TwoFactorCode savedCode = twoFactorCodeRepository.save(twoFactorCode);

        String firstName = customerRepository.findByUserId(user.getId())
                .map(customer -> customer.getPersonName().firstName())
                .orElse("Usuario");

        userEventPublisher.publishTwoFactorCodeRequestedEvent(
                user.getId(),
                user.getEmail().value(),
                savedCode.getCode(),
                firstName
        );

        return new TwoFactorRequiredResult(
                savedCode.getSessionToken(),
                user.getEmail().masked(),
                savedCode.getExpirySeconds()
        );
    }
}
