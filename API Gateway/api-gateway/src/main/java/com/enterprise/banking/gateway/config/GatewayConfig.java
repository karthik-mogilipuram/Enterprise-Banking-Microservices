package com.enterprise.banking.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://banking-auth:8081"))
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .uri("http://banking-account:8082"))
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .uri("http://banking-transaction:8083"))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("http://banking-payment:8084"))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://banking-notification:8085"))
                .build();
    }
}