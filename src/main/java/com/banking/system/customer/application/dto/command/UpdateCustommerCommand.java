package com.banking.system.customer.application.dto.command;

import java.time.LocalDate;

public record UpdateCustommerCommand(
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
