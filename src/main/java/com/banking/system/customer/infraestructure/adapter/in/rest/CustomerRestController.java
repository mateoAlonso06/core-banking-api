package com.banking.system.customer.infraestructure.adapter.in.rest;

import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.application.usecase.ApproveKycUseCase;
import com.banking.system.customer.application.usecase.DeleteCustomerUseCase;
import com.banking.system.customer.application.usecase.GetCustomerUseCase;
import com.banking.system.customer.application.usecase.RejectKycUseCase;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerRestController {
    private final GetCustomerUseCase getCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final ApproveKycUseCase approveKycUseCase;
    private final RejectKycUseCase rejectKycUseCase;

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResult> getCustomerById(@PathVariable @NotNull UUID id) {
        var result = getCustomerUseCase.getCustomerById(id);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @GetMapping
    public ResponseEntity<List<CustomerResult>> getAllCustomers(@RequestParam(required = false, defaultValue = "0") int page,
                                                                @RequestParam(required = false, defaultValue = "10") int size) {
        var customers = getCustomerUseCase.getAll(page, size);
        return ResponseEntity.ok(customers);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable @NotNull UUID id) {
        deleteCustomerUseCase.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/kyc/approve")
    public ResponseEntity<Void> approveKyc(@PathVariable @NotNull UUID id) {
        approveKycUseCase.approveKyc(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/kyc/reject")
    public ResponseEntity<Void> rejectKyc(@PathVariable @NotNull UUID id) {
        rejectKycUseCase.rejectKyc(id);
        return ResponseEntity.noContent().build();
    }
}
