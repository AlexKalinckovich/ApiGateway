package com.example.ApiGateway.dto.auth;

import com.example.ApiGateway.dto.user.UserResponseDto;

public record RegistrationResponse(
    UserResponseDto userResponseDto,
    AuthResponse authResponse
) { }
