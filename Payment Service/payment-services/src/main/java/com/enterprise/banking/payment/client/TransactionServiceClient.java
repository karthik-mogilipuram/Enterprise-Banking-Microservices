package com.enterprise.banking.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class TransactionServiceClient {

    private final RestTemplate restTemplate;
    private final String transactionServiceUrl;

    public TransactionServiceClient(
            RestTemplate restTemplate,
            @Value("${services.transaction-service.url}") String transactionServiceUrl) {
        this.restTemplate = restTemplate;
        this.transactionServiceUrl = transactionServiceUrl;
    }

    public Map<String, Object> deposit(UUID accountId, BigDecimal amount, String description) {
        String url = transactionServiceUrl + "/api/transactions/deposit";
        Map<String, Object> body = Map.of(
                "accountId", accountId,
                "amount", amount,
                "description", description
        );
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(url, body, Map.class);
        return response.getBody();
    }

    public Map<String, Object> withdraw(UUID accountId, BigDecimal amount, String description) {
        String url = transactionServiceUrl + "/api/transactions/withdraw";
        Map<String, Object> body = Map.of(
                "accountId", accountId,
                "amount", amount,
                "description", description
        );
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(url, body, Map.class);
        return response.getBody();
    }
}