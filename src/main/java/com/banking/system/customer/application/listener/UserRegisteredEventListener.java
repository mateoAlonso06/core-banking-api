package com.banking.system.customer.application.listener;

import com.banking.system.auth.application.event.UserRegisteredEvent;
import com.banking.system.customer.application.dto.command.CreateCustomerCommand;
import com.banking.system.customer.application.usecase.CreateCustomerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener responsible for reacting to the {@link UserRegisteredEvent}
 * coming from the authentication module.
 *
 * <p>
 * This listener is executed <b>only after the transaction
 * that creates the User has been successfully committed</b>.
 * </p>
 *
 * <p>
 * The creation of the Customer is performed in a <b>new independent transaction</b>
 * (REQUIRES_NEW) to avoid transactional coupling between modules
 * and to allow eventual consistency.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {
    private final CreateCustomerUseCase createCustomerUseCase;

    /**
     * Handles the user registration event and creates the associated Customer.
     *
     * <p>
     * This method:
     * <ul>
     *   <li>Is executed only after the User transaction commit</li>
     *   <li>Starts a new independent transaction (REQUIRES_NEW)</li>
     *   <li>Does not participate in the original transaction</li>
     * </ul>
     *
     * <p>
     * If the Customer creation fails, the already persisted User is not rolled back,
     * maintaining eventual consistency between modules.
     * </p>
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(UserRegisteredEvent event) {
        CreateCustomerCommand command = new CreateCustomerCommand(
                event.userId(),
                event.firstName(),
                event.lastName(),
                event.documentType(),
                event.documentNumber(),
                event.birthDate(),
                event.phone(),
                event.address(),
                event.city(),
                event.country()
        );

        createCustomerUseCase.createCustomer(command);
    }
}
