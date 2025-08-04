package com.example.ApiGateway.service.user;

import com.example.ApiGateway.dto.auth.RegistrationResponse;
import com.example.ApiGateway.dto.user.UserCreateRequest;
import com.example.ApiGateway.dto.user.UserResponseDto;
import com.example.ApiGateway.exception.AuthCreationException;
import com.example.ApiGateway.exception.RegistrationException;
import com.example.ApiGateway.exception.UserCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserServiceClient {

    @Value("${services.user.base_endpoint}")
    private String userBaseEndpoint;

    @Value("${services.user.endpoints.create}")
    private String userCreateRoute;

    @Value("${services.user.endpoints.getById}")
    private String userGetByIdRoute;

    private final WebClient userWebClient;

    @Autowired
    public UserServiceClient(@Qualifier("userServiceWebClient") final WebClient userWebClient) {
        this.userWebClient = userWebClient;
    }

    public Mono<UserResponseDto> createUser(final UserCreateRequest userRequest) {
        return userWebClient.post()
                .uri(userBaseEndpoint + userCreateRoute)
                .bodyValue(userRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new UserCreationException(
                                        "User creation failed",
                                        HttpStatus.valueOf(response.statusCode().value()),
                                        body
                                )));
                    }
                    return response.bodyToMono(UserResponseDto.class);
                });
    }

    public Mono<RegistrationResponse> rollbackUser(final Long userId,
                                                   final AuthCreationException authEx) {
        return userWebClient.delete()
                .uri(userBaseEndpoint + userGetByIdRoute, userId)
                .retrieve()
                .toBodilessEntity()
                .then(Mono.error(new RegistrationException(
                        "Rollback successful after auth failure",
                        authEx.getStatus(),
                        authEx.getResponseBody(),
                        authEx
                )));
    }

}
