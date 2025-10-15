package com.example.ApiGateway.dto.auth;

import com.example.ApiGateway.dto.user.UserResponseDto;

public record AuthenticationResponse(
    UserResponseDto userResponseDto,
    TokenResponse tokenResponse
) { }
