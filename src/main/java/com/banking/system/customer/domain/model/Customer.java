package com.banking.system.customer.domain.model;

import com.banking.system.common.domain.Address;
import com.banking.system.common.domain.IdentityDocument;
import com.banking.system.common.domain.PersonName;
import com.banking.system.common.domain.Phone;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Customer {
    private final UUID id;
    private final UUID userId;
    private PersonName personName;
    private final IdentityDocument identityDocument;
    private LocalDate birthDate;
    private Phone phone;
    private Address address;
    private final LocalDate customerSince;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;

    private Customer(
            UUID id,
            UUID userId,
            PersonName personName,
            IdentityDocument identityDocument,
            LocalDate birthDate,
            Phone phone,
            Address address,
            LocalDate customerSince,
            KycStatus kycStatus,
            RiskLevel riskLevel) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(personName, "personName must not be null");
        Objects.requireNonNull(identityDocument, "identityDocument must not be null");
        Objects.requireNonNull(birthDate, "birthDate must not be null");
        Objects.requireNonNull(phone, "phone must not be null");
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(customerSince, "customerSince must not be null");
        Objects.requireNonNull(kycStatus, "kycStatus must not be null");
        Objects.requireNonNull(riskLevel, "riskLevel must not be null");

        this.id = id;
        this.userId = userId;
        this.personName = personName;
        this.identityDocument = identityDocument;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.customerSince = customerSince;
        this.kycStatus = kycStatus;
        this.riskLevel = riskLevel;
    }

    /**
     * Reconstitutes a Customer from existing data.
     *
     * @param id               the unique identifier of the customer
     * @param userId           the unique identifier of the associated user
     * @param personName       the person's name
     * @param identityDocument the identity document of the customer
     * @param birthDate        the birth date of the customer
     * @param phone            the phone number of the customer
     * @param address          the address of the customer
     * @param customerSince    the date when the customer was created
     * @param kycStatus        the KYC status of the customer
     * @param riskLevel        the risk level of the customer
     */
    public static Customer reconstitute(
            UUID id,
            UUID userId,
            PersonName personName,
            IdentityDocument identityDocument,
            LocalDate birthDate,
            Phone phone,
            Address address,
            LocalDate customerSince,
            KycStatus kycStatus,
            RiskLevel riskLevel
    ) {
        return new Customer(
                id,
                userId,
                personName,
                identityDocument,
                birthDate,
                phone,
                address,
                customerSince,
                kycStatus,
                riskLevel
        );
    }


    /**
     * Creates a new Customer with default values for certain fields.
     * <p>
     * Sets the customerSince field to the current date, KYC status to PENDING, and risk level to LOW.
     *
     * @param userId           the unique identifier of the associated user
     * @param personName       the person's name
     * @param identityDocument the identity document of the customer
     * @param birthDate        the birth date of the customer
     * @param phone            the phone number of the customer
     * @param address          the address of the customer
     * @return a new Customer instance with default values
     */
    public static Customer createNewCustomer(
            UUID userId,
            PersonName personName,
            IdentityDocument identityDocument,
            LocalDate birthDate,
            Phone phone,
            Address address
    ) {
        return new Customer(
                null,
                userId,
                personName,
                identityDocument,
                birthDate,
                phone,
                address,
                LocalDate.now(),
                KycStatus.PENDING,
                RiskLevel.LOW
        );
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
