package com.banking.system.auth.application.service;

import com.banking.system.auth.application.dto.command.ResendVerificationCommand;
import com.banking.system.auth.application.dto.command.VerifyEmailCommand;
import com.banking.system.auth.application.event.publisher.UserEventPublisher;
import com.banking.system.auth.application.usecase.ResendVerificationEmailUseCase;
import com.banking.system.auth.application.usecase.VerifyEmailUseCase;
import com.banking.system.auth.domain.exception.InvalidVerificationTokenException;
import com.banking.system.auth.domain.exception.UserIsAlreadyProcessedException;
import com.banking.system.auth.domain.exception.UserNotVerifiedException;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.domain.model.UserStatus;
import com.banking.system.auth.domain.model.VerificationToken;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import com.banking.system.auth.domain.port.out.VerificationTokenRepositoryPort;
import com.banking.system.auth.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService implements VerifyEmailUseCase, ResendVerificationEmailUseCase {

    private final VerificationTokenRepositoryPort tokenRepository;
    private final UserRepositoryPort userRepository;
    private final UserEventPublisher eventPublisher;

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailCommand command) {
        log.info("Verifying email with token");

        VerificationToken token = tokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidVerificationTokenException("Verification token not found"));

        try {
            token.markUsed();
        } catch (IllegalStateException e) {
            throw new InvalidVerificationTokenException(e.getMessage());
        }

        tokenRepository.save(token);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.activate();
        userRepository.save(user);

        log.info("Email verified successfully for user {}", user.getId());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(ResendVerificationCommand command) {
        log.info("Resending verification email to {}", command.email());

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new UserIsAlreadyProcessedException("User is already verified");
        }

        VerificationToken token = VerificationToken.createNew(user.getId());
        tokenRepository.save(token);

        eventPublisher.publishEmailVerificationRequestedEvent(
                user.getId(), user.getEmail().value(), token.getToken(), null
        );

        log.info("Verification email resent for user {}", user.getId());
    }
}