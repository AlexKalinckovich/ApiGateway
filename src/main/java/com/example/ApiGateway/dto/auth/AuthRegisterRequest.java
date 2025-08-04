package com.example.ApiGateway.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @Email @NotBlank @Size(max = 30) String email,
        @NotBlank @Size(min = 12, max = 30) String passwordHash
) { }
