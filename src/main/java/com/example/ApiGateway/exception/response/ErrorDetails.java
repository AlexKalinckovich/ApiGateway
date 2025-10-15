package com.example.ApiGateway.exception.response;

sealed interface ErrorDetails permits ValidationErrorDetails, SimpleErrorDetails {
}
