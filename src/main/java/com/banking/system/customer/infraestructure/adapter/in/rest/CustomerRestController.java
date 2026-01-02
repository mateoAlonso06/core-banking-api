package com.banking.system.customer.infraestructure.adapter.in.rest;

import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.application.usecase.DeleteCustomerUseCase;
import com.banking.system.customer.application.usecase.GetCustomerUseCase;
import com.banking.system.customer.application.usecase.UpdateCustomerUseCase;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerRestController {
    private final GetCustomerUseCase getCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResult> getCustomerById(@PathVariable @NotNull @Positive UUID id) {
        CustomerResult result = getCustomerUseCase.getCustomerById(id);

        return ResponseEntity.ok(result);
    }
}
