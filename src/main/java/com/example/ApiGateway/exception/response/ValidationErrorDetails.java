package com.example.ApiGateway.exception.response;

import java.util.Map;

public record ValidationErrorDetails(Map<String, String> fieldErrors) implements ErrorDetails {
}
