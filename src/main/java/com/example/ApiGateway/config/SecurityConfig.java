package com.example.ApiGateway.config;

import com.example.ApiGateway.security.filter.AuthValidationFilter;
import com.example.ApiGateway.service.auth.AuthenticationServiceClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ALL_ENDPOINTS = "/**";

    @Value("${services.api-gateway.url}")
    private String apiGatewayUri;

    @Value("${services.auth.base_endpoint}")
    private String authBaseEndpoint;

    @Value("${services.auth.endpoints.login}")
    private String authLoginRoute;

    @Value("${services.auth.endpoints.validate}")
    private String authValidateRoute;

    @Value("${services.api-gateway.base_endpoint}")
    private String registrationBaseEndpoint;

    private final AuthenticationServiceClient authenticationServiceClient;

    @Bean
    public AuthValidationFilter authenticationWebFilter() {
        return new AuthValidationFilter(authenticationServiceClient);
    }

    @Bean
    public ServerCsrfTokenRequestHandler csrfTokenRequestHandler() {
        return new HeaderOrParamCsrfTokenRequestHandler();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(apiGatewayUri);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.addExposedHeader("X-XSRF-TOKEN");
        config.setMaxAge(Duration.ofHours(1));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(ALL_ENDPOINTS, config);
        return source;
    }

    @Bean
    @Profile("dev")
    public Customizer<ServerHttpSecurity.CsrfSpec> csrfSpec() {
        return ServerHttpSecurity.CsrfSpec::disable;
    }

    @Bean
    @Profile("production")
    public Customizer<ServerHttpSecurity.CsrfSpec> csrfSpecProduction() {
        return csrf -> {
            csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
            csrf.csrfTokenRequestHandler(csrfTokenRequestHandler());
            csrf.requireCsrfProtectionMatcher(this::requireCsrfProtection);
        };
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http,
                                                         final Customizer<ServerHttpSecurity.CsrfSpec> csrfSpec) {
        return http
                .csrf(csrfSpec)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(registrationBaseEndpoint + ALL_ENDPOINTS).permitAll()
                        .pathMatchers(authBaseEndpoint + authLoginRoute).permitAll()
                        .pathMatchers(authBaseEndpoint + authValidateRoute).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private Mono<ServerWebExchangeMatcher.MatchResult> requireCsrfProtection(
            @NonNull final ServerWebExchange exchange) {

        final String method = exchange.getRequest().getMethod().name();
        final String path = exchange.getRequest().getPath().value();

        if (method.matches("GET|HEAD|OPTIONS|TRACE") ||
                path.startsWith(authBaseEndpoint) ||
                path.startsWith(registrationBaseEndpoint)) {
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        }

        return ServerWebExchangeMatcher.MatchResult.match();
    }
}

