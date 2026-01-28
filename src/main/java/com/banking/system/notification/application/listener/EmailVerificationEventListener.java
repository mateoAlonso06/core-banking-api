package com.banking.system.notification.application.listener;

import com.banking.system.auth.application.event.EmailVerificationRequestedEvent;
import com.banking.system.notification.application.service.AccountEmailService;
import com.banking.system.notification.domain.model.EmailNotification;
import com.banking.system.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationEventListener {

    private final AccountEmailService accountEmailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EmailVerificationRequestedEvent event) {
        log.info("Sending verification email to user {}", event.userId());

        String customerName = event.firstName() != null ? event.firstName() : "Usuario";

        accountEmailService.sendEmail(
                new EmailNotification(
                        event.email(),
                        NotificationType.EMAIL_VERIFICATION.getDefaultSubject(),
                        NotificationType.EMAIL_VERIFICATION.getTemplateName(),
                        Map.of(
                                "customerName", customerName,
                                "verificationToken", event.token()
                        ),
                        NotificationType.EMAIL_VERIFICATION
                )
        );
    }
}
