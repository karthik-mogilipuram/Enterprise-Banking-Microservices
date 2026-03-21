package com.enterprise.banking.account.controller;

import com.enterprise.banking.account.model.AccountStatus;
import com.enterprise.banking.account.model.AccountType;
import com.enterprise.banking.account.model.dto.AccountResponse;
import com.enterprise.banking.account.model.dto.BalanceResponse;
import com.enterprise.banking.account.model.dto.CreateAccountRequest;
import com.enterprise.banking.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private final UUID testAccountId = UUID.randomUUID();
    private final UUID testUserId = UUID.randomUUID();

    @Test
    void createAccount_ValidRequest_Returns201() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(testUserId)
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("1000.00"))
                .build();

        AccountResponse response = AccountResponse.builder()
                .id(testAccountId)
                .userId(testUserId)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .dailyLimit(new BigDecimal("10000.00"))
                .createdAt(LocalDateTime.now())
                .build();

        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void createAccount_MissingUserId_Returns400() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountType(AccountType.SAVINGS)
                .build();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccount_Returns200() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id(testAccountId)
                .userId(testUserId)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountService.getAccount(testAccountId)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/" + testAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    void getAccountsByUserId_Returns200() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id(testAccountId)
                .userId(testUserId)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountService.getAccountsByUserId(testUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts/user/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));
    }

    @Test
    void getBalance_Returns200() throws Exception {
        BalanceResponse response = BalanceResponse.builder()
                .accountId(testAccountId)
                .accountNumber("1234567890")
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .build();

        when(accountService.getBalance(testAccountId)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/" + testAccountId + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    void updateStatus_Returns200() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id(testAccountId)
                .userId(testUserId)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(AccountStatus.FROZEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountService.updateStatus(any(UUID.class), any(AccountStatus.class))).thenReturn(response);

        mockMvc.perform(patch("/api/accounts/" + testAccountId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"FROZEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
    }
}