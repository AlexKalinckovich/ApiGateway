package com.example.ApiGateway.controller;

import com.example.ApiGateway.dto.auth.GatewayRegistrationRequest;
import com.example.ApiGateway.dto.auth.RegistrationResponse;
import com.example.ApiGateway.service.user.RegistrationService;
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
public class RegistrationController {
    private final RegistrationService registrationService;

    @Value("${services.user.base_endpoint}")
    private String userBaseEndpoint;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("${services.api-gateway.endpoints.registration}")
    public Mono<ResponseEntity<RegistrationResponse>> register(
            @RequestBody @Valid GatewayRegistrationRequest request
    ) {
        return registrationService.register(request)
                .map(response -> ResponseEntity
                        .created(URI.create(userBaseEndpoint + "/" + response.userResponseDto().id()))
                        .body(response));
    }
}