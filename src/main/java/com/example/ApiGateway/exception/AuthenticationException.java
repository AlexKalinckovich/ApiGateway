package com.example.ApiGateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends RuntimeException {
    private final int status;
    private final String responseBody;

    public AuthenticationException(int status, String responseBody) {
        super("Authentication failed with status: " + status);
        this.status = status;
        this.responseBody = responseBody;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.responseBody = null;
    }

}