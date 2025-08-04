package com.example.ApiGateway.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.order.url}")
    private String orderServiceUrl;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Bean
    public WebClient userServiceWebClient(final WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(userServiceUrl)
                .clientConnector(reactorClientHttpConnector())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient orderServiceWebClient(final WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clientConnector(reactorClientHttpConnector())
                .baseUrl(orderServiceUrl)
                .build();
    }

    @Bean
    public WebClient authServiceWebClient(final WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(authServiceUrl)
                .clientConnector(reactorClientHttpConnector())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public ReactorClientHttpConnector reactorClientHttpConnector() {
        return new ReactorClientHttpConnector(httpClient());
    }

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider
                .builder("custom")
                .maxIdleTime(Duration.ofSeconds(60))
                .maxLifeTime(Duration.ofSeconds(120))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.create(connectionProvider())
                .option(ChannelOption.SO_KEEPALIVE, true);
    }
}
