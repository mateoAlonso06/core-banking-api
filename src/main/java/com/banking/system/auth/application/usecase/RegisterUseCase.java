package com.banking.system.auth.application.usecase;

import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserRequest;
import com.banking.system.auth.infraestructure.adapter.in.rest.dto.RegisterUserResponse;

public interface RegisterUseCase {
    RegisterUserResponse register(RegisterUserRequest registerUserRequest);
}
