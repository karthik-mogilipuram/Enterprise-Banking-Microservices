package com.enterprise.banking.transaction.client;

import lombok.RequiredArgsConstructor;
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

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountServiceClient(
            RestTemplate restTemplate,
            @Value("${services.account-service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    public Map<String, Object> getAccount(UUID accountId) {
        String url = accountServiceUrl + "/api/accounts/" + accountId;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public Map<String, Object> getBalance(UUID accountId) {
        String url = accountServiceUrl + "/api/accounts/" + accountId + "/balance";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public void credit(UUID accountId, BigDecimal amount) {
        String url = accountServiceUrl + "/api/accounts/" + accountId + "/credit";
        restTemplate.postForEntity(url, Map.of("amount", amount), Void.class);
    }

    public void debit(UUID accountId, BigDecimal amount) {
        String url = accountServiceUrl + "/api/accounts/" + accountId + "/debit";
        restTemplate.postForEntity(url, Map.of("amount", amount), Void.class);
    }
}
