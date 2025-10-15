package com.example.ApiGateway.exception;

import com.example.ApiGateway.exception.creation.UserCreationException;
import com.example.ApiGateway.exception.response.ErrorResponse;
import com.example.ApiGateway.exception.response.ExceptionResponseService;
import com.example.ApiGateway.exception.response.ValidationErrorDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ExceptionResponseService exceptionResponseService;
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ExceptionResponseService exceptionResponseService, ObjectMapper objectMapper) {
        this.exceptionResponseService = exceptionResponseService;
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            @NonNull final WebExchangeBindException ex,
            final ServerWebExchange exchange) {

        final Map<String, String> fieldErrorsMap = new LinkedHashMap<>();
        for (final FieldError fe : ex.getBindingResult().getFieldErrors()) {
            final String defaultMessage = fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage();
            fieldErrorsMap.putIfAbsent(fe.getField(), defaultMessage);
        }

        final ValidationErrorDetails details = new ValidationErrorDetails(fieldErrorsMap);

        final ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorMessage.VALIDATION_ERROR.name(),
                "Validation failed",
                exchange.getRequest().getPath().toString(),
                details
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserCreationException.class, RegistrationException.class, AuthCreationException.class})
    public ResponseEntity<ErrorResponse> handleDownstreamServiceExceptions(
            final RuntimeException ex,
            final ServerWebExchange exchange) {
        ResponseEntity<ErrorResponse> result;

        log.error("Downstream service exception occurred: {}", ex.getMessage(), ex);

        HttpStatus status;
        String responseBody;

        switch (ex) {
            case UserCreationException uex -> {
                status = uex.getStatus();
                responseBody = uex.getResponseBody();
            }
            case AuthCreationException aex -> {
                status = aex.getStatus();
                responseBody = aex.getResponseBody();
            }
            case RegistrationException rex -> {
                status = rex.getStatus();
                responseBody = rex.getResponseBody();
            }
            default -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                responseBody = "Unknown service error";
            }
        }

        if (responseBody != null) {
            try {
                final ErrorResponse downstreamError = objectMapper.readValue(responseBody, ErrorResponse.class);
                result = new ResponseEntity<>(downstreamError, status);
            } catch (final Exception parseException) {
                log.warn("Failed to parse downstream error response, creating generic error: {}", responseBody, parseException);
                result = handleGenericException(ex, exchange);
            }
        } else {
            result = exceptionResponseService.buildErrorResponse(
                    ex,
                    exchange,
                    status,
                    ErrorMessage.EXTERNAL_SERVICE_ERROR
            );
        }

        return result;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            final AuthenticationException ex,
            final ServerWebExchange exchange) {

        log.error("Authentication exception occurred: {}", ex.getMessage(), ex);

        if (ex.getResponseBody() != null) {
            try {
                ErrorResponse downstreamError = objectMapper.readValue(ex.getResponseBody(), ErrorResponse.class);
                return new ResponseEntity<>(downstreamError, HttpStatus.valueOf(ex.getStatus()));
            } catch (Exception parseException) {
                log.warn("Failed to parse auth service error response: {}", ex.getResponseBody(), parseException);
            }
        }

        return exceptionResponseService.buildErrorResponse(
                ex,
                exchange,
                HttpStatus.UNAUTHORIZED,
                ErrorMessage.AUTHENTICATION_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            final Exception ex,
            final ServerWebExchange exchange) {

        log.error("Unhandled exception in API Gateway: {}", ex.getMessage(), ex);

        return exceptionResponseService.buildErrorResponse(
                ex,
                exchange,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessage.EXTERNAL_SERVICE_ERROR
        );
    }
}