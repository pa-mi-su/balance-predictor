package com.example.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TokenAuthFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    public TokenAuthFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var path = exchange.getRequest().getURI().getPath();

        // allow auth & actuator without token
        if (path.startsWith("/api/auth/") || path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        // only guard /api/**
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        var token = exchange.getRequest().getHeaders().getFirst("X-Auth-Token");
        if (token == null || token.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // call auth-service to validate
        return webClientBuilder.build()
                .get()
                .uri("http://auth-service/api/auth/introspect")
                .header("X-Auth-Token", token)
                .retrieve()
                .toBodilessEntity()
                .flatMap(resp -> {
                    // 200 means valid
                    return chain.filter(exchange);
                })
                .onErrorResume(ex -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        // run early
        return -100;
    }
}
