package com.banking.system.customer.domain.model;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Customer {
    private UUID id;
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
     * Factory method to create a new domain Customer for initial creation.
     * <p>
     * Validates required fields and initializes domain defaults:
     * <ul>
     *   <li>id remains null (to be assigned by persistence)</li>
     *   <li>customerSince is set to the current date</li>
     *   <li>kycStatus defaults to KycStatus.PENDING</li>
     *   <li>riskLevel defaults to RiskLevel.LOW</li>
     * </ul>
     *
     * @param userId         UUID of the associated user (required)
     * @param firstName      Customer's given name (required, non-blank)
     * @param lastName       Customer's family name (required, non-blank)
     * @param documentType   Type of identification document (required, non-blank)
     * @param documentNumber Identification document number (required, non-blank)
     * @param birthDate      Customer's birth date (required, not in the future)
     * @param phone          Contact phone number (required, non-blank)
     * @param address        Postal address (required, non-blank)
     * @param city           City of residence (required, non-blank)
     * @param country        Country of residence (required, non-blank)
     * @return a new Customer instance with validated and defaulted fields
     * @throws IllegalArgumentException if any required parameter is null, blank, or invalid
     */
    public static Customer createNewCustomer(
            UUID userId,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            String phone,
            String address,
            String city,
            String country) {
        validateFields(userId, firstName, lastName, documentType, documentNumber, birthDate, phone, address, city, country);

        return new Customer(
                null,
                userId,
                firstName,
                lastName,
                documentType,
                documentNumber,
                birthDate,
                phone,
                address,
                city,
                country,
                LocalDate.now(), // customerSince
                KycStatus.PENDING, // kycStatus
                RiskLevel.LOW // riskLevel
        );
    }

    private static void validateFields(
            UUID userId,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            String phone,
            String address,
            String city,
            String country) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException("Document type cannot be null or blank");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("Document number cannot be null or blank");
        }
        if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be null or in the future");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be null or blank");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or blank");
        }
    }

    public boolean isKycApproved() {
        return this.kycStatus == KycStatus.APPROVED;
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
