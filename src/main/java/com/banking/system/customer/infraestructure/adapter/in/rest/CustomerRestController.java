package com.banking.system.customer.infraestructure.adapter.in.rest;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customers", description = "Customer management and KYC operations")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerRestController {
    private final GetCustomerUseCase getCustomerUseCase;
    private final GetAllCustomerUseCase getAllCustomerUseCase;
    private final ApproveKycUseCase approveKycUseCase;
    private final RejectKycUseCase rejectKycUseCase;

    @Operation(
            summary = "Get my customer profile",
            description = "Retrieves the customer profile for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "404", description = "Customer profile not found for this user")
    })
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW_OWN')")
    @GetMapping("/me")
    public ResponseEntity<CustomerResult> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        var result = getCustomerUseCase.getCustomerByUserId(userId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get customer by ID",
            description = "Retrieves a specific customer by their ID. Only accessible by ADMIN or BRANCH_MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Role not authorized for this operation"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW')")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResult> getCustomerById(
            @Parameter(description = "Customer ID to retrieve", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NotNull UUID customerId) {
        var result = getCustomerUseCase.getCustomerById(customerId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get all customers",
            description = "Retrieves a paginated list of all customers. Only accessible by ADMIN or BRANCH_MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Role not authorized for this operation")
    })
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW_ALL')")
    @GetMapping
    public ResponseEntity<PagedResult<CustomerResult>> getAllCustomers(@ParameterObject Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        var customers = getAllCustomerUseCase.getAllCustomers(pageRequest);
        return ResponseEntity.ok(customers);
    }

    @Operation(
            summary = "Approve customer KYC",
            description = "Approves the KYC verification for a customer. Only accessible by ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "KYC approved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Role not authorized for this operation"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "422", description = "KYC already processed")
    })
    @PreAuthorize("hasAuthority('KYC_APPROVE')")
    @PutMapping("/{customerId}/kyc/approve")
    public ResponseEntity<Void> approveKyc(
            @Parameter(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NotNull UUID customerId) {
        approveKycUseCase.approveKyc(customerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reject customer KYC",
            description = "Rejects the KYC verification for a customer. Only accessible by ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "KYC rejected successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Role not authorized for this operation"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "422", description = "KYC already processed")
    })
    @PreAuthorize("hasAuthority('KYC_REJECT')")
    @PutMapping("/{customerId}/kyc/reject")
    public ResponseEntity<Void> rejectKyc(
            @Parameter(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NotNull UUID customerId) {
        rejectKycUseCase.rejectKyc(customerId);
        return ResponseEntity.noContent().build();
    }
}
