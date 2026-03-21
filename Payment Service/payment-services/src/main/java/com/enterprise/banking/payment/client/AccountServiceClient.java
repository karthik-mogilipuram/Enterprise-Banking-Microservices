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
public class AccountServiceClient {

    private static final String ACCOUNTS_ENDPOINT = "/api/accounts/";
    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountServiceClient(
            RestTemplate restTemplate,
            @Value("${services.account-service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    public Map<String, Object> getAccount(UUID accountId) {
        String url = accountServiceUrl + ACCOUNTS_ENDPOINT + accountId;
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public void credit(UUID accountId, BigDecimal amount) {
        String url = accountServiceUrl + ACCOUNTS_ENDPOINT + accountId + "/credit";
        restTemplate.postForEntity(url, Map.of("amount", amount), Void.class);
    }

    public void debit(UUID accountId, BigDecimal amount) {
        String url = accountServiceUrl + ACCOUNTS_ENDPOINT + accountId + "/debit";
        restTemplate.postForEntity(url, Map.of("amount", amount), Void.class);
    }
}