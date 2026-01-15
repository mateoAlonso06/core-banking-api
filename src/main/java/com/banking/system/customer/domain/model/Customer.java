package com.banking.system.customer.domain.model;

import com.banking.system.common.domain.Address;
import com.banking.system.common.domain.IdentityDocument;
import com.banking.system.common.domain.PersonName;
import com.banking.system.common.domain.Phone;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Customer {
    private UUID id;
    private UUID userId;
    private PersonName personName;
    private IdentityDocument identityDocument;
    private LocalDate birthDate;
    private Phone phone;
    private Address address;
    private LocalDate customerSince;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;

    public static Customer createNewCustomer() {
        return null;
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
