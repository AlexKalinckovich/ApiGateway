package com.example.ApiGateway.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateRequest(
    @NotBlank @Size(min = 3, max = 10) String name,
    @NotBlank @Size(min = 3, max = 10) String surname,
    @Email @NotBlank @Size(max = 30) String email,
    @Past @NotNull LocalDate birthDate
) { }
