package com.banking.system.customer.application.dto.command;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCustomerCommand(
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        LocalDate birthDate,
        String phone,
        String address,
        String city,
        String country
) {
}
