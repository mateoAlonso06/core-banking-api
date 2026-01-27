package com.banking.system.customer.infraestructure.adapter.out.mapper;

import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.infraestructure.adapter.out.persistence.entity.CustomerJpaEntity;

public class CustomerJpaEntityMapper {
    public static CustomerJpaEntity toJpaEntity(Customer customer) {
        return CustomerJpaEntity.builder()
                .id(customer.getId() != null ? customer.getId() : null)
                .userId(customer.getUserId())
                .firstName(customer.getPersonName().firstName())
                .lastName(customer.getPersonName().lastName())
                .documentNumber(customer.getIdentityDocument().number())
                .documentType(customer.getIdentityDocument().type())
                .address(customer.getAddress().address())
                .city(customer.getAddress().city())
                .country(customer.getAddress().country())
                .birthDate(customer.getBirthDate())
                .phone(customer.getPhone().number())
                .customerSince(customer.getCustomerSince())
                .kycStatus(customer.getKycStatus())
                .riskLevel(customer.getRiskLevel())
                .kycVerifiedAt(customer.getKycVerifiedAt())
                .build();
    }

    public static Customer toDomainEntity(CustomerJpaEntity entity) {
        return Customer.reconstitute(
                entity.getId(),
                entity.getUserId(),
                new com.banking.system.common.domain.PersonName(
                        entity.getFirstName(),
                        entity.getLastName()
                ),
                new com.banking.system.common.domain.IdentityDocument(
                        entity.getDocumentNumber(),
                        entity.getDocumentType()
                ),
                entity.getBirthDate(),
                new com.banking.system.common.domain.Phone(
                        entity.getPhone()
                ),
                new com.banking.system.common.domain.Address(
                        entity.getAddress(),
                        entity.getCity(),
                        entity.getCountry()
                ),
                entity.getCustomerSince(),
                entity.getKycStatus(),
                entity.getRiskLevel(),
                entity.getKycVerifiedAt()
        );
    }
}
