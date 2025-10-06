package com.example.plaid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PlaidConfig {

    @Value("${plaid.env:sandbox}")
    private String env;

    @Value("${plaid.client-id}")
    private String clientId;

    @Value("${plaid.secret}")
    private String secret;

    @Bean
    public String plaidBaseUrl() {
        return switch (env.toLowerCase()) {
            case "development" -> "https://development.plaid.com";
            case "production" -> "https://production.plaid.com";
            default -> "https://sandbox.plaid.com";
        };
    }

    @Bean
    public WebClient plaidWebClient(String plaidBaseUrl) {
        return WebClient.builder()
                .baseUrl(plaidBaseUrl)
                // Increase buffer size for large JSON responses
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .defaultHeader("Plaid-Client-Id", clientId)
                .defaultHeader("Plaid-Secret", secret)
                .build();
    }
}
