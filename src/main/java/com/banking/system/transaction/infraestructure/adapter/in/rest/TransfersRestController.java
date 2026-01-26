package com.banking.system.transaction.infraestructure.adapter.in.rest;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers", description = "Endpoints for money transfers between accounts")
@SecurityRequirement(name = "Bearer Authentication")
public class TransfersRestController {

    private final TransferMoneyUseCase transferMoneyUseCase;
    private final GetTransferByIdUseCase getTransferByIdUseCase;

    @Operation(
            summary = "Transfer money",
            description = "Transfers money from one account to another. Validates sufficient funds, active accounts, and currency compatibility."
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
    public ResponseEntity<TransferResult> transfer(@RequestBody @Valid TransferMoneyRequest request) {
        var command = request.toCommand();

        var result = transferMoneyUseCase.transfer(command);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get transfer by ID",
            description = "Retrieves the details of a specific money transfer by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Role not authorized for this operation"),
            @ApiResponse(responseCode = "404", description = "Transfer not found")
    })
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW_OWN', 'TRANSACTION_VIEW_ALL')")
    @GetMapping("/{id}")
    public ResponseEntity<TransferResult> getTransferById(
            @Parameter(description = "Transfer ID to retrieve", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        var result = getTransferByIdUseCase.findById(id);
        return ResponseEntity.ok(result);
    }
}
