package com.example.ApiGateway.exception.response;

import com.example.ApiGateway.exception.ErrorMessage;
import com.example.ApiGateway.service.messageService.MessageService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ExceptionResponseService {

    private final MessageService messageService;


    @NotNull
    public ResponseEntity<ErrorResponse> buildErrorResponse(
            final @NotNull Exception ex,
            final ServerWebExchange request,
            final @NotNull HttpStatus status,
            final @NotNull ErrorMessage errorCode
    ) {
        final ErrorDetails details = new SimpleErrorDetails(ex.getMessage());

        final ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                errorCode.name(),
                messageService.getMessage(errorCode),
                request.getRequest().getPath().toString(),
                details
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}
