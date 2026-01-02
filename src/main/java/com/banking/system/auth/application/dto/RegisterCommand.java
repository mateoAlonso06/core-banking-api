package com.banking.system.auth.application.dto;

import java.time.LocalDate;

public record RegisterCommand(
    String email,
    String password,
    String firstName,
    String lastName,
    String documentType,
    String documentNumber,
    LocalDate birthDate,
    String phone
) {
}
