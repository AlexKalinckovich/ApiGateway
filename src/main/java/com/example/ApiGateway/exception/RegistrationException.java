package com.example.ApiGateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RegistrationException extends RuntimeException {
    private final HttpStatus status;
    private final String responseBody;

    public RegistrationException(String message, HttpStatus status, String responseBody, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.responseBody = responseBody;
    }

}