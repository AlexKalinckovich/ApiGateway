package com.example.ApiGateway.dto.user;

import com.example.ApiGateway.dto.card.CardInfoResponseDto;

import java.time.LocalDate;
import java.util.List;

public record UserResponseDto(
        Long id,
        String name,
        String surname,
        String email,
        LocalDate birthDate,
        List<CardInfoResponseDto> cards
){}
