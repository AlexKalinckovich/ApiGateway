package com.example.ApiGateway.dto.auth;


public record TokenResponse(
        Long id,
        String accessToken,
        String refreshToken
) { }
