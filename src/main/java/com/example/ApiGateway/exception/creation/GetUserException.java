package com.example.ApiGateway.exception.creation;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GetUserException extends RuntimeException {
    private final HttpStatus status;
    private final String responseBody;

    public GetUserException(String message, HttpStatus status, String responseBody) {
        super(message);
        this.status = status;
        this.responseBody = responseBody;
    }

}