package com.banking.system.customer.infraestructure.adapter.out.mapper;

import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.infraestructure.adapter.out.persistence.entity.CustomerJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerEntityMapper {

    CustomerJpaEntity toEntity(Customer customer);

    Customer toDomain(CustomerJpaEntity customerJpaEntity);
}
