package com.example.ApiGateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            final WebExchangeBindException ex,
            final ServerWebExchange exchange) {

        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        final ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                exchange.getRequest().getPath().toString(),
                errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({UserCreationException.class, RegistrationException.class, AuthCreationException.class})
    public ResponseEntity<String> handleServiceExceptions(final RuntimeException ex) {
        if (ex instanceof UserCreationException uex) {
            return ResponseEntity.status(uex.getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(uex.getResponseBody());
        }
        if (ex instanceof AuthCreationException aex) {
            return ResponseEntity.status(aex.getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(aex.getResponseBody());
        }
        if (ex instanceof RegistrationException rex) {
            return ResponseEntity.status(rex.getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(rex.getResponseBody());
        }
        return ResponseEntity.internalServerError().body("Unknown error");
    }
}
