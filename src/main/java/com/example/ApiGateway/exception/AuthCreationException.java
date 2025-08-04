package com.example.ApiGateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthCreationException extends RuntimeException {
    private final HttpStatus status;
    private final String responseBody;

    public AuthCreationException(final String message,
                                 final HttpStatus status,
                                 final String responseBody) {
        super(message);
        this.status = status;
        this.responseBody = responseBody;
    }
}
