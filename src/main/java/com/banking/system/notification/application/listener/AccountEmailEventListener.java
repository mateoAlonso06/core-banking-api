package com.banking.system.notification.application.listener;

import com.banking.system.account.application.event.AccountCreatedEvent;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import com.banking.system.notification.application.service.AccountEmailService;
import com.banking.system.notification.domain.model.EmailNotification;
import com.banking.system.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccountEmailEventListener {

    private final UserRepositoryPort userRepositoryPort;
    private final AccountEmailService accountEmailService;
    private final CustomerRepositoryPort customerRepositoryPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AccountCreatedEvent event) {
        User user = userRepositoryPort.findById(event.userId())
                .orElseThrow();

        Customer customer = customerRepositoryPort.findById(event.customerId())
                .orElseThrow();

        String fullName = customer.getPersonName().fullName();

        accountEmailService.sendEmail(
                new EmailNotification(
                        user.getEmail(),
                        NotificationType.ACCOUNT_CREATED.getDefaultSubject(),
                        NotificationType.ACCOUNT_CREATED.getTemplateName(),
                        Map.of(
                                user.getEmail(),
                                Map.of(
                                        "customerName", fullName,
                                        "accountNumber", event.accountNumber(),
                                        "accountAlias", event.alias(),
                                        "currency", event.currency(),
                                        "accountType", event.accountType(),
                                        "openedAt", event.openedAt()
                                )
                        ),
                        NotificationType.ACCOUNT_CREATED
                )
        );
    }
}
