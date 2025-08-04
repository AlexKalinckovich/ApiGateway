package com.example.ApiGateway.service.auth;

import com.example.ApiGateway.dto.auth.AuthRegisterRequest;
import com.example.ApiGateway.dto.auth.AuthResponse;
import com.example.ApiGateway.exception.AuthCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    @Autowired
    public AuthenticationServiceClient(@Qualifier("authServiceWebClient") WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }


    public Mono<Boolean> validateToken(final String token){
        if(token == null || token.isBlank()){
            return Mono.empty();
        }
        return authWebClient.get()
                .uri(baseAuthEndPoint +  validateAuthRoute)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(2));
    }

    public Mono<AuthResponse> createCredentials(final AuthRegisterRequest credentials) {
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
                    return response.bodyToMono(AuthResponse.class);
                })
                .onErrorResume(Mono::error);
    }
}
