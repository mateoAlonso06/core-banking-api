package com.banking.system.account.infraestructure.adapter.in.rest;

import com.banking.system.account.application.dto.command.CreateAccountCommand;
import com.banking.system.account.application.dto.result.AccountResult;
import com.banking.system.account.application.usecase.CreateAccountUseCase;
import com.banking.system.account.application.usecase.FindAccountByIdUseCase;
import com.banking.system.account.application.usecase.FindAllAccountUseCase;
import com.banking.system.account.infraestructure.adapter.in.rest.dto.request.CreateAccountRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountRestController {

    private final CreateAccountUseCase createAccountUseCase;
    private final FindAccountByIdUseCase findAccountByIdUseCase;
    private final FindAllAccountUseCase findAllAccountUseCase;

    @PostMapping
    public ResponseEntity<AccountResult> createAccount(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid CreateAccountRequest request) {


        var command = new CreateAccountCommand(
                userId,
                request.accountType(),
                request.currency()
        );

        var result = createAccountUseCase.createAccount(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/accounts/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @GetMapping
    public ResponseEntity<List<AccountResult>> getAllAccounts(@RequestParam(defaultValue = "10", required = false) int size,
                                                              @RequestParam(defaultValue = "0", required = false) int page) {
        var results = findAllAccountUseCase.findAll(size, page);

        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResult> getAllAccounts(@PathVariable @NotNull UUID id) {
        var result = findAccountByIdUseCase.findById(id);

        return ResponseEntity.ok().body(result);
    }
}
