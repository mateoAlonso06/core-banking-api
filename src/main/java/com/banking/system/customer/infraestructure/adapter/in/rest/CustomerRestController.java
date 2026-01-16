package com.banking.system.customer.infraestructure.adapter.in.rest;

import com.banking.system.customer.application.dto.command.UpdateCustommerCommand;
import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.application.usecase.DeleteCustomerUseCase;
import com.banking.system.customer.application.usecase.GetCustomerUseCase;
import com.banking.system.customer.infraestructure.adapter.in.rest.dto.request.CustomerUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerRestController {
    private final GetCustomerUseCase getCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResult> getCustomerById(@PathVariable @NotNull @Positive UUID id) {
        CustomerResult result = getCustomerUseCase.getCustomerById(id);

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResult>> getAllCustomers(@RequestParam(required = false, defaultValue = "0") int page,
                                                                @RequestParam(required = false, defaultValue = "10") int size) {
        List<CustomerResult> customers = getCustomerUseCase.getAll(page, size);
        return ResponseEntity.ok(customers);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable @NotNull @Positive UUID id) {
        deleteCustomerUseCase.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }
}
