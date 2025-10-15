package com.example.ApiGateway.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    PAYMENT_NOT_FOUND("payment_not_found"),
    PAYMENT_ALREADY_EXISTS("payment_already_exists"),
    PAYMENT_PROCESSING_ERROR("payment_processing_error"),
    PAYMENT_DECLINED("payment_declined"),
    PAYMENT_GATEWAY_ERROR("payment_gateway_error"),
    INSUFFICIENT_FUNDS("insufficient_funds"),
    INVALID_PAYMENT_METHOD("invalid_payment_method"),

    ORDER_NOT_FOUND("order_not_found"),
    ORDER_ALREADY_PAID("order_already_paid"),

    USER_NOT_FOUND("user_not_found"),

    VALIDATION_ERROR("validation_error"),
    INVALID_REQUEST("invalid_request"),

    DATABASE_ERROR("database_error"),
    DATABASE_CONNECTION_ERROR("database_connection_error"),
    DUPLICATE_KEY_ERROR("duplicate_key_error"),

    KAFKA_PRODUCER_ERROR("kafka_producer_error"),
    KAFKA_CONSUMER_ERROR("kafka_consumer_error"),
    KAFKA_SERIALIZATION_ERROR("kafka_serialization_error"),
    KAFKA_DESERIALIZATION_ERROR("kafka_deserialization_error"),

    EXTERNAL_SERVICE_ERROR("external_service_error"),
    EXTERNAL_SERVICE_TIMEOUT("external_service_timeout"),
    EXTERNAL_SERVICE_UNAVAILABLE("external_service_unavailable"),

    NETWORK_ERROR("network_error"),

    AUTHENTICATION_ERROR("authentication_error"),
    AUTHORIZATION_ERROR("authorization_error"),

    SERVER_ERROR("server_error"),
    SERVICE_UNAVAILABLE("service_unavailable"),

    JSON_PARSING_ERROR("json_parsing_error"),
    JSON_SERIALIZATION_ERROR("json_serialization_error");

    private final String key;

    ErrorMessage(final String key) {
        this.key = key;
    }
}