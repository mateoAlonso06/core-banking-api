package com.banking.system.transaction.infraestructure.adapter.in.rest;

import com.banking.system.transaction.application.dto.receipt.TransferReceipt;
import com.banking.system.transaction.application.dto.result.TransferResult;
import com.banking.system.transaction.application.usecase.GetTransferByIdUseCase;
import com.banking.system.transaction.application.usecase.TransferMoneyUseCase;
import com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request.TransferMoneyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers", description = "Endpoints for money transfers between accounts")
@SecurityRequirement(name = "Bearer Authentication")
public class TransfersRestController {

    private final TransferMoneyUseCase transferMoneyUseCase;
    private final GetTransferByIdUseCase getTransferByIdUseCase;

    @Operation(
            summary = "Transfer money",
            description = "Transfers money from one account to another using the destination account's alias or account number. Exactly one of 'toAlias' or 'toAccountNumber' must be provided. Validates sufficient funds, active accounts, and currency compatibility."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (validation failed)"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Role not authorized for this operation"),
            @ApiResponse(responseCode = "404", description = "Source or target account not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation (insufficient funds, inactive account, same account transfer, or currency mismatch)")
    })
    @PreAuthorize("hasAuthority('TRANSACTION_TRANSFER')")
    @PostMapping
    public ResponseEntity<TransferReceipt> transfer(@RequestBody @Valid TransferMoneyRequest request,
                                                    @AuthenticationPrincipal UUID userId) {
        var receipt = transferMoneyUseCase.transfer(request.toCommand(), userId);
        return ResponseEntity.ok(receipt);
    }

    @Operation(
            summary = "Get my transfer by ID",
            description = "Retrieves the details of a specific money transfer by its ID. Only returns transfers where the authenticated user is the sender or recipient."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Transfer does not belong to the authenticated user"),
            @ApiResponse(responseCode = "404", description = "Transfer not found")
    })
    @PreAuthorize("hasAuthority('TRANSACTION_VIEW_OWN')")
    @GetMapping("/{id}/me")
    public ResponseEntity<TransferResult> getTransferByIdForCustomer(
            @Parameter(description = "Transfer ID to retrieve", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NotNull UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        var result = getTransferByIdUseCase.findByIdForCustomer(id, userId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get transfer by ID",
            description = "Retrieves the details of any money transfer by its ID. Only accessible by ADMIN or authorized roles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Role not authorized for this operation"),
            @ApiResponse(responseCode = "404", description = "Transfer not found")
    })
    @PreAuthorize("hasAuthority('TRANSACTION_VIEW_ALL')")
    @GetMapping("/{id}")
    public ResponseEntity<TransferResult> getTransferById(
            @Parameter(description = "Transfer ID to retrieve", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NotNull UUID id) {
        var result = getTransferByIdUseCase.findById(id);
        return ResponseEntity.ok(result);
    }
}
