package com.example.ApiGateway.dto.card;

import java.time.LocalDate;

public record CardInfoResponseDto(
    Long id,
    Long userId,
    String number,
    String holder,
    LocalDate expirationData
) { }
