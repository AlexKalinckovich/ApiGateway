package com.example.ApiGateway.dto.auth;


public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
