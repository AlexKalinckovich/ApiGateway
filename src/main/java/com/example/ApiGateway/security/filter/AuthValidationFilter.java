package com.example.ApiGateway.security.filter;

import com.example.ApiGateway.service.auth.AuthenticationServiceClient;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

public class AuthValidationFilter implements WebFilter {

    private static final String BEARER = "Bearer ";
    private static final int BEARER_LENGTH = BEARER.length();
    private static final String REGISTRATION_ENDPOINT = "/registration";

    @Value("${services.auth.base_endpoint}")
    private String authBaseEndpoint;

    private final AuthenticationServiceClient authenticationServiceClient;

    public AuthValidationFilter(final AuthenticationServiceClient authenticationServiceClient) {
        this.authenticationServiceClient = authenticationServiceClient;
    }

    @Override
    public @NonNull Mono<Void> filter(final ServerWebExchange exchange,
                                      @NonNull final WebFilterChain chain) {
        final String path = exchange.getRequest().getPath().value();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        return extractToken(exchange.getRequest())
                .flatMap(token -> token == null ?
                        unauthorizedResponse(exchange) :
                        validateAndAuthenticate(exchange, chain, token));
    }

    private Mono<Void> validateAndAuthenticate(ServerWebExchange exchange,
                                               WebFilterChain chain,
                                               String token) {
        return validateToken(token)
                .flatMap(valid -> {
                    if (valid) {
                        final String username = extractNameFromToken(token);
                        final Authentication authentication =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                );

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    }
                    return unauthorizedResponse(exchange);
                })
                .onErrorResume((e) -> serviceUnavailable(exchange));
    }

    private boolean isWhitelisted(final String path) {
        return path.startsWith(authBaseEndpoint) || path.startsWith(REGISTRATION_ENDPOINT);
    }

    private String extractNameFromToken(final String token) {
        final Base64.Decoder decoder = Base64.getUrlDecoder();
        try {
            final String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                return "unknown";
            }
            final String payload = new String(decoder.decode(chunks[1]));
            final JsonObject json = JsonParser
                    .parseString(payload)
                    .getAsJsonObject();

            return json.has("sub") ? json.get("sub").getAsString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Mono<String> extractToken(final ServerHttpRequest request) {
        final List<String> headers = request.getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (headers != null && !headers.isEmpty()) {
            final String header = headers.getFirst();
            if (header != null && header.startsWith(BEARER)) {
                return Mono.just(header.substring(BEARER_LENGTH));
            }
        }
        return Mono.empty();
    }

    private Mono<Boolean> validateToken(final String token) {
        return authenticationServiceClient.validateToken(token)
                .onErrorReturn(false);
    }

    private Mono<Void> unauthorizedResponse(final ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> serviceUnavailable(final ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return exchange.getResponse().setComplete();
    }

}
