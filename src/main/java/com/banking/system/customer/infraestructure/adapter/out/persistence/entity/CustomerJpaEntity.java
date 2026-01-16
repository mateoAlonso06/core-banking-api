package com.banking.system.customer.infraestructure.adapter.out.persistence.entity;

import com.banking.system.customer.domain.model.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "customers")
public class CustomerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(length = 50)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(length = 100, nullable = false)
    private String city;

    @Column(length = 2, nullable = false) // ISO 3166-1 alpha-2
    private String country;

    @Column(name = "customer_since", nullable = false)
    private LocalDate customerSince;

    @Column(name = "kyc_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Customer.KycStatus kycStatus;

    @Column(name = "risk_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Customer.RiskLevel riskLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}
