package com.example.ApiGateway.exception.creation;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserCreationException extends RuntimeException {
    private final HttpStatus status;
    private final String responseBody;

    public UserCreationException(String message, HttpStatus status, String responseBody) {
        super(message);
        this.status = status;
        this.responseBody = responseBody;
    }

}

