package com.example.ApiGateway.service.user;

import com.example.ApiGateway.dto.auth.AuthRegisterRequest;
import com.example.ApiGateway.dto.auth.GatewayRegistrationRequest;
import com.example.ApiGateway.dto.auth.AuthResponse;
import com.example.ApiGateway.dto.auth.RegistrationResponse;
import com.example.ApiGateway.dto.user.UserCreateRequest;
import com.example.ApiGateway.dto.user.UserResponseDto;
import com.example.ApiGateway.exception.AuthCreationException;
import com.example.ApiGateway.service.auth.AuthenticationServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserServiceClient userServiceClient;
    private final AuthenticationServiceClient authenticationServiceClient;

    public Mono<RegistrationResponse> register(final GatewayRegistrationRequest request) {
        return createUser(request.userData())
                .flatMap(userResponse -> createCredentials(request.credentials())
                        .flatMap(auth -> Mono.just(
                                    new RegistrationResponse(userResponse, auth)
                                )
                        )
                        .onErrorResume(e -> {
                            if (e instanceof AuthCreationException) {
                                return rollbackUser(userResponse.id(), (AuthCreationException)e);
                            }
                            return Mono.error(e);
                        })
                );
    }

    private Mono<UserResponseDto> createUser(final UserCreateRequest userRequest) {
        return userServiceClient.createUser(userRequest);
    }

    private Mono<AuthResponse> createCredentials(final AuthRegisterRequest credentials) {
        return authenticationServiceClient.createCredentials(credentials);
    }

    private Mono<RegistrationResponse> rollbackUser(final Long userId,
                                                    final AuthCreationException authEx) {
        return userServiceClient.rollbackUser(userId, authEx);
    }
}
