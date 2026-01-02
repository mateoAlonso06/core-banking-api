package com.banking.system.customer.domain.model;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "internalBuilder", builderClassName = "CustomerBuilder")
public class Customer {
    @Setter
    private UUID id; // assigned by the database
    private UUID userId;
    private String firstName;
    private String lastName;
    private String documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String city;
    private String country;
    private LocalDate customerSince;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;

    /**
     * Factory method para crear un nuevo Customer con validación de dominio.
     * Usado en la capa de aplicación para crear nuevos clientes.
     */
    public static Customer createNew(
            UUID userId,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            String phone,
            LocalDate customerSince,
            KycStatus kycStatus,
            RiskLevel riskLevel
    ) {
        // Validación de dominio
        validateRequiredFields(userId, firstName, lastName, documentType, documentNumber,
                birthDate, customerSince, kycStatus, riskLevel);

        // Construcción con builder interno (usado también por MapStruct)
        return internalBuilder()
                .userId(userId)
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .documentType(documentType.trim())
                .documentNumber(documentNumber.trim())
                .birthDate(birthDate)
                .phone(phone)
                .customerSince(customerSince)
                .kycStatus(kycStatus)
                .riskLevel(riskLevel)
                .build();
    }

    /**
     * Validación de campos requeridos del dominio
     */
    private static void validateRequiredFields(
            UUID userId,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            LocalDate customerSince,
            KycStatus kycStatus,
            RiskLevel riskLevel
    ) {
        if (userId == null)
            throw new IllegalArgumentException("userId is required");
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("firstName is required");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("lastName is required");
        if (documentType == null || documentType.isBlank())
            throw new IllegalArgumentException("documentType is required");
        if (documentNumber == null || documentNumber.isBlank())
            throw new IllegalArgumentException("documentNumber is required");
        if (birthDate == null)
            throw new IllegalArgumentException("birthDate is required");
        if (customerSince == null)
            throw new IllegalArgumentException("customerSince is required");
        if (kycStatus == null)
            throw new IllegalArgumentException("kycStatus is required");
        if (riskLevel == null)
            throw new IllegalArgumentException("riskLevel is required");
    }

    public void approveKyc() {
        if (this.kycStatus != KycStatus.PENDING) {
            throw new IllegalStateException("KYC can only be approved from PENDING");
        }
        this.kycStatus = KycStatus.APPROVED;
    }

    public void rejectKyc() {
        if (this.kycStatus != KycStatus.PENDING) {
            throw new IllegalStateException("KYC can only be rejected from PENDING");
        }
        this.kycStatus = KycStatus.REJECTED;
    }

    public enum KycStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }
}
