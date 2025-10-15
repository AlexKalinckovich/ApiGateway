package com.example.ApiGateway.service.auth;

import com.example.ApiGateway.dto.auth.AuthCredentialsRequest;
import com.example.ApiGateway.dto.auth.AuthenticationResponse;
import com.example.ApiGateway.dto.auth.TokenResponse;
import com.example.ApiGateway.exception.AuthCreationException;
import com.example.ApiGateway.exception.AuthenticationException;
import com.example.ApiGateway.exception.RegistrationException;
import com.example.ApiGateway.exception.creation.UserCreationException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class AuthenticationServiceClient {
    private static final String BEARER = "Bearer ";
    private final WebClient authWebClient;

    @Value("${services.auth.base_endpoint}")
    private String baseAuthEndPoint;

    @Value("${services.auth.endpoints.validate}")
    private String validateAuthRoute;

    @Value("${services.auth.endpoints.register}")
    private String registerAuthRoute;

    @Value("${services.auth.endpoints.login}")
    private String loginAuthRoute;

    @Value("${services.auth.endpoints.refresh}")
    private String refreshAuthRoute;

    @Value("${services.auth.endpoints.delete}")
    private String deleteAuthRoute;

    @Autowired
    public AuthenticationServiceClient(@Qualifier("authServiceWebClient") WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    public Mono<TokenResponse> authenticate(@NonNull AuthCredentialsRequest authCredentialsRequest) {
        return authWebClient.post()
                .uri(baseAuthEndPoint + loginAuthRoute)
                .bodyValue(authCredentialsRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    // Extract the error response body and throw a custom exception
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new AuthenticationException(
                                    response.statusCode().value(),
                                    errorBody
                            )));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new AuthenticationException(
                                    response.statusCode().value(),
                                    errorBody
                            )));
                })
                .bodyToMono(TokenResponse.class)
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(TimeoutException.class, ex ->
                        Mono.error(new AuthenticationException(408, "Authentication service timeout"))
                );
    }

    public Mono<Boolean> validateToken(final String token) {
        if(token == null || token.isBlank()) {
            return Mono.empty();
        }
        return authWebClient.get()
                .uri(baseAuthEndPoint + validateAuthRoute)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new AuthenticationException(
                                response.statusCode().value(),
                                errorBody
                        ))))
                .onStatus(HttpStatusCode::is5xxServerError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new AuthenticationException(
                                response.statusCode().value(),
                                errorBody
                        ))))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(TimeoutException.class, ex ->
                        Mono.error(new AuthenticationException(HttpStatus.REQUEST_TIMEOUT.value(), "Token validation service timeout"))
                );
    }

    public Mono<TokenResponse> createCredentials(final AuthCredentialsRequest credentials) {
        return authWebClient.post()
                .uri(baseAuthEndPoint + registerAuthRoute)
                .bodyValue(credentials)
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new AuthCreationException(
                                            "Credentials creation failed",
                                            HttpStatus.valueOf(response.statusCode().value()),
                                            body
                                        )
                                    )
                                );
                    }
                    return response.bodyToMono(TokenResponse.class);
                })
                .onErrorResume(Mono::error);
    }

    public Mono<AuthenticationResponse> rollbackCredentials(final Long id, final UserCreationException ex) {
        return authWebClient.delete()
                .uri(baseAuthEndPoint + deleteAuthRoute, id)
                .retrieve()
                .toBodilessEntity()
                .then(Mono.error(new RegistrationException(
                        "Rollback successful after auth failure",
                        ex.getStatus(),
                        ex.getResponseBody(),
                        ex
                )));
    }
}
