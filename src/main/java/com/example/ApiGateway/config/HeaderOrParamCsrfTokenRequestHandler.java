package com.example.ApiGateway.config;

import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestHandler;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class HeaderOrParamCsrfTokenRequestHandler implements ServerCsrfTokenRequestHandler {

    @Override
    public void handle(final ServerWebExchange exchange, final Mono<CsrfToken> csrfToken){
        exchange.getAttributes().put(CsrfToken.class.getName(), csrfToken);
    }

    @Override
    public Mono<String> resolveCsrfTokenValue(final ServerWebExchange exchange, final CsrfToken csrfToken) {
        final String headerName = csrfToken.getHeaderName();
        final String paramName  = csrfToken.getParameterName();

        final String fromHeader = exchange.getRequest().getHeaders().getFirst(headerName);
        if (fromHeader != null) {
            return Mono.just(fromHeader);
        }

        final String fromQuery = exchange.getRequest()
                .getQueryParams()
                .getFirst(paramName);

        return fromQuery != null ? Mono.just(fromQuery) : Mono.empty();
    }
}
