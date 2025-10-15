package com.example.ApiGateway.config;

import com.example.ApiGateway.security.filter.AuthValidationFilter;
import com.example.ApiGateway.service.auth.AuthenticationServiceClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ALL_ENDPOINTS = "/**";

    @Value("${services.frontend.url:http://localhost}")
    private String frontendUrl;

    @Value("${services.auth.base_endpoint}")
    private String authBaseEndpoint;

    @Value("${services.auth.endpoints.login}")
    private String authLoginRoute;

    @Value("${services.auth.endpoints.validate}")
    private String authValidateRoute;

    @Value("${services.api-gateway.base_endpoint}")
    private String apiBaseEndpoint;

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
    @Profile("production")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(frontendUrl));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(Arrays.asList("X-XSRF-TOKEN"));
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(ALL_ENDPOINTS, config);
        return source;
    }

    @Bean
    @Profile("dev")
    public CorsConfigurationSource corsConfigurationSourceDev() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(Arrays.asList("X-XSRF-TOKEN"));
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(ALL_ENDPOINTS, config);
        return source;
    }

    @Bean
    @Profile("dev")
    public SecurityWebFilterChain securityWebFilterChainDev(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable()) // CSRF отключен в dev
                .cors(cors -> cors.configurationSource(corsConfigurationSourceDev())) // CORS включен с разрешением всего
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(authBaseEndpoint + authLoginRoute).permitAll()
                        .pathMatchers(authBaseEndpoint + authValidateRoute).permitAll()
                        .pathMatchers(apiBaseEndpoint + ALL_ENDPOINTS).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    @Profile("production")
    public SecurityWebFilterChain securityWebFilterChainProduction(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> {
                    csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
                    csrf.csrfTokenRequestHandler(csrfTokenRequestHandler());
                    csrf.requireCsrfProtectionMatcher(this::requireCsrfProtection);
                })
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS с настройками для production
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(authBaseEndpoint + authLoginRoute).permitAll()
                        .pathMatchers(authBaseEndpoint + authValidateRoute).permitAll()
                        .pathMatchers(apiBaseEndpoint + ALL_ENDPOINTS).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private Mono<ServerWebExchangeMatcher.MatchResult> requireCsrfProtection(
            @NonNull ServerWebExchange exchange) {

        final String method = exchange.getRequest().getMethod().name();
        final String path = exchange.getRequest().getPath().value();

        if (method.matches("GET|HEAD|OPTIONS|TRACE") ||
                path.startsWith(authBaseEndpoint) ||
                path.startsWith(apiBaseEndpoint)) {
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        }

        return ServerWebExchangeMatcher.MatchResult.match();
    }
}