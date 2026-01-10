package com.banking.system.account.infraestructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class AccountJpaEntity {
    @Id
    private UUID id;
}
