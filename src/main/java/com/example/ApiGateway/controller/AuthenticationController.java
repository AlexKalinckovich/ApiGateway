package com.example.ApiGateway.controller;

import com.example.ApiGateway.dto.auth.AuthCredentialsRequest;
import com.example.ApiGateway.dto.auth.GatewayRegistrationRequest;
import com.example.ApiGateway.dto.auth.AuthenticationResponse;
import com.example.ApiGateway.service.user.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("${services.api-gateway.base_endpoint}")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Value("${services.user.base_endpoint}")
    private String userBaseEndpoint;

    @Autowired
    public AuthenticationController(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("${services.api-gateway.endpoints.registration}")
    public Mono<ResponseEntity<AuthenticationResponse>> register(
            @RequestBody @Valid GatewayRegistrationRequest request
    ) {
        return authenticationService.register(request)
                .map(response -> ResponseEntity
                        .created(URI.create(userBaseEndpoint + "/" + response.userResponseDto().id()))
                        .body(response));
    }

    @PostMapping("${services.api-gateway.endpoints.login}")
    public Mono<ResponseEntity<AuthenticationResponse>> login(
            @RequestBody @Valid AuthCredentialsRequest request
    ){
        return authenticationService.authenticate(request)
                .map(response -> ResponseEntity
                        .created(URI.create(userBaseEndpoint + "/" + response.userResponseDto().id()))
                        .body(response));
    }
}