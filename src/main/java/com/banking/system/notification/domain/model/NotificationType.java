package com.banking.system.notification.domain.model;

import lombok.Getter;

@Getter
public enum NotificationType {
    EMAIL_VERIFICATION("email-verification", "Verifica tu correo electrónico"),
    ACCOUNT_CREATED("account-created", "Cuenta creada exitosamente"),
    PASSWORD_RESET("password-reset", "Restablecer contraseña"),
    TRANSACTION_COMPLETED("transaction-completed", "Transacción completada");

    private final String templateName;
    private final String defaultSubject;

    NotificationType(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }
}

