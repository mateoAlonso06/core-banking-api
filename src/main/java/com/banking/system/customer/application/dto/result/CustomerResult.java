package com.banking.system.customer.application.dto.result;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerResult(
    UUID id,
    UUID userId,
    String firstName,
    String lastName,
    String documentType,
    String documentNumber,
    LocalDate birthDate,
    String phone,
    String address,
    String city,
    String country,
    String kycStatus,
    String riskLevel
) {
}
