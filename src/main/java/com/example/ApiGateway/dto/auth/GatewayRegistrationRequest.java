package com.example.ApiGateway.dto.auth;

import com.example.ApiGateway.dto.user.UserCreateRequest;
import jakarta.validation.Valid;

public record GatewayRegistrationRequest(
        @Valid UserCreateRequest userData,
        @Valid AuthCredentialsRequest credentials
) {}
