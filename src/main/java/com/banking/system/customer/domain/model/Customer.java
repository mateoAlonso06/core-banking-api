package com.banking.system.customer.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Customer {

    private UUID id; // assigned by the database
    private final UUID userId;
    private final String firstName;
    private final String lastName;
    private final String documentType;
    private final String documentNumber;
    private final LocalDate birthDate;
    private final String phone;
    private final String address;
    private final String city;
    private final String country;
    private final LocalDate customerSince;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;

    @Builder
    private Customer(
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
            LocalDate customerSince,
            KycStatus kycStatus,
            RiskLevel riskLevel
    ) {
        validate(userId, firstName, lastName, documentType, documentNumber, birthDate, customerSince, kycStatus, riskLevel);
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.documentType = documentType.trim();
        this.documentNumber = documentNumber.trim();
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.country = country;
        this.customerSince = customerSince;
        this.kycStatus = kycStatus;
        this.riskLevel = riskLevel;
    }

    private void validate(
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
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName is required");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("lastName is required");
        if (documentType == null || documentType.isBlank())
            throw new IllegalArgumentException("documentType is required");
        if (documentNumber == null || documentNumber.isBlank())
            throw new IllegalArgumentException("documentNumber is required");
        if (birthDate == null) throw new IllegalArgumentException("birthDate is required");
        if (customerSince == null) throw new IllegalArgumentException("customerSince is required");
        if (kycStatus == null) throw new IllegalArgumentException("kycStatus is required");
        if (riskLevel == null) throw new IllegalArgumentException("riskLevel is required");
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
