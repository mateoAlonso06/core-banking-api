package com.banking.system.customer.infraestructure.adapter.out.mapper;

import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.infraestructure.adapter.out.persistence.entity.CustomerJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerEntityMapper {
    CustomerJpaEntity toEntity(Customer customer);

    Customer toDomain(CustomerJpaEntity customerJpaEntity);
}
