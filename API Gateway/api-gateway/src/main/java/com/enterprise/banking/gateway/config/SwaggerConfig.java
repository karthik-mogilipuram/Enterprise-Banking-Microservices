package com.enterprise.banking.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        properties.setPath("/swagger-ui.html");

        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();

        AbstractSwaggerUiConfigProperties.SwaggerUrl authUrl =
                new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        authUrl.setName("Auth Service");
        authUrl.setUrl("/api/auth/v3/api-docs");
        urls.add(authUrl);

        AbstractSwaggerUiConfigProperties.SwaggerUrl accountUrl =
                new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        accountUrl.setName("Account Service");
        accountUrl.setUrl("/api/accounts/v3/api-docs");
        urls.add(accountUrl);

        AbstractSwaggerUiConfigProperties.SwaggerUrl transactionUrl =
                new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        transactionUrl.setName("Transaction Service");
        transactionUrl.setUrl("/api/transactions/v3/api-docs");
        urls.add(transactionUrl);

        AbstractSwaggerUiConfigProperties.SwaggerUrl paymentUrl =
                new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        paymentUrl.setName("Payment Service");
        paymentUrl.setUrl("/api/payments/v3/api-docs");
        urls.add(paymentUrl);

        properties.setUrls(urls);
        return properties;
    }
}