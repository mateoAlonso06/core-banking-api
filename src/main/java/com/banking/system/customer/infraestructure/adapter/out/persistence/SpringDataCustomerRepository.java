package com.banking.system.customer.infraestructure.adapter.out.persistence;

import com.banking.system.customer.infraestructure.adapter.out.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {
    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByUserId(UUID uuid);

    Optional<CustomerJpaEntity> findByUserId(UUID id);
}
