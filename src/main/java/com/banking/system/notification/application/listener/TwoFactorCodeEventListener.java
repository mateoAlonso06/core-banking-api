package com.banking.system.notification.application.listener;

import com.banking.system.auth.application.event.TwoFactorCodeRequestedEvent;
import com.banking.system.notification.application.service.AccountEmailService;
import com.banking.system.notification.domain.model.EmailNotification;
import com.banking.system.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwoFactorCodeEventListener {

    private final AccountEmailService accountEmailService;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TwoFactorCodeRequestedEvent event) {
        log.info("Sending 2FA code email to user {}", event.userId());

        String customerName = event.firstName() != null ? event.firstName() : "Usuario";

        accountEmailService.sendEmail(
                new EmailNotification(
                        event.email(),
                        NotificationType.TWO_FACTOR_CODE.getDefaultSubject(),
                        NotificationType.TWO_FACTOR_CODE.getTemplateName(),
                        Map.of(
                                "customerName", customerName,
                                "twoFactorCode", event.code()
                        ),
                        NotificationType.TWO_FACTOR_CODE
                )
        );
    }
}
