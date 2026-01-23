package com.banking.system.transaction.infraestructure.adapter.in.rest;

import com.banking.system.transaction.application.dto.result.TransferResult;
import com.banking.system.transaction.application.usecase.GetTransferByIdUseCase;
import com.banking.system.transaction.application.usecase.TransferMoneyUseCase;
import com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request.TransferMoneyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transfers")
public class TransfersRestController {

    private final TransferMoneyUseCase transferMoneyUseCase;
    private final GetTransferByIdUseCase getTransferByIdUseCase;

    @PostMapping
    public ResponseEntity<TransferResult> transfer(@RequestBody @Valid TransferMoneyRequest request) {
        var command = request.toCommand();

        var result = transferMoneyUseCase.transfer(command);
        var location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/transfers/{id}")
                .buildAndExpand(result.transferId())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResult> getTransferById(@PathVariable UUID id) {
        var result = getTransferByIdUseCase.findById(id);
        return ResponseEntity.ok(result);
    }
}
