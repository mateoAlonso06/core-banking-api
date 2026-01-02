package com.banking.system.auth.application.event;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Application Event
 * Represents the fact that a user has been successfully registered.
 * This event is immutable and is published AFTER the transaction commits.
 */
public record UserRegisteredEvent(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        LocalDate birthDate,
        String phone
) {
}
