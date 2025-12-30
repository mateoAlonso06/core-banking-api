package com.banking.system.auth.domain.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor // sirve para traer toda la info de la DB
@NoArgsConstructor
@Builder
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private UserStatus status; // ACTIVE, BLOCKED, PENDING_VERIFICATION
    private Role role; // CUSTOMER, ADMIN, BRANCH_MANAGER

    /**
     * Constructs a new User with the specified email and password hash.
     * The ID is automatically generated, the status is set to PENDING_VERIFICATION,
     * and the role is set to CUSTOMER by default.
     *
     * @param email        the email address of the user.
     * @param passwordHash the hashed password of the user.
     */
    public User(String email, String passwordHash) {
        if (email == null || passwordHash == null)
            throw new IllegalArgumentException("Email and passwordHash cannot be null");

        this.id = UUID.randomUUID();
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = UserStatus.PENDING_VERIFICATION; // Default status
        this.role = Role.CUSTOMER;
    }

    public void activate() {
        if (this.status != UserStatus.PENDING_VERIFICATION)
            throw new IllegalStateException("Only users with PENDING_VERIFICATION status can be activated");

        this.status = UserStatus.ACTIVE;
    }

    public void block() {
        if (this.status != UserStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE users can be blocked");

        this.status = UserStatus.BLOCKED;
    }
}
