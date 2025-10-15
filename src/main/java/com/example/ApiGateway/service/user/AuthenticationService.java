package com.example.ApiGateway.service.user;

import com.example.ApiGateway.dto.auth.AuthCredentialsRequest;
import com.example.ApiGateway.dto.auth.GatewayRegistrationRequest;
import com.example.ApiGateway.dto.auth.TokenResponse;
import com.example.ApiGateway.dto.auth.AuthenticationResponse;
import com.example.ApiGateway.dto.user.UserCreateRequest;
import com.example.ApiGateway.dto.user.UserResponseDto;
import com.example.ApiGateway.exception.AuthenticationException;
import com.example.ApiGateway.exception.creation.UserCreationException;
import com.example.ApiGateway.service.auth.AuthenticationServiceClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserServiceClient userServiceClient;
    private final AuthenticationServiceClient authenticationServiceClient;

    public Mono<AuthenticationResponse> authenticate(@NonNull final AuthCredentialsRequest authCredentialsRequest) {
        return authenticationServiceClient.authenticate(authCredentialsRequest)
                .flatMap(tokenResponse ->
                        userServiceClient.getUserByEmail(authCredentialsRequest.email())
                                .map(userResponseDto -> new AuthenticationResponse(userResponseDto, tokenResponse)))
                .onErrorResume(AuthenticationException.class, Mono::error);
    }

    public Mono<AuthenticationResponse> register(@NonNull final GatewayRegistrationRequest request) {
        return createCredentials(request.credentials())
                .flatMap(credentials -> createUser(request.userData())
                        .flatMap(userResponseDto -> Mono.just(
                                        new AuthenticationResponse(userResponseDto, credentials)
                                )
                        )
                        .onErrorResume(e -> rollbackCredentials(credentials.id(), (UserCreationException)e))
                );
    }

    private Mono<UserResponseDto> createUser(final UserCreateRequest userRequest) {
        return userServiceClient.createUser(userRequest);
    }

    private Mono<TokenResponse> createCredentials(final AuthCredentialsRequest credentials) {
        return authenticationServiceClient.createCredentials(credentials);
    }

    private Mono<AuthenticationResponse> rollbackCredentials(final Long id,
                                                             final UserCreationException ex){
        return authenticationServiceClient.rollbackCredentials(id,ex);
    }
}
