package com.enterprise.banking.account.service;

import com.enterprise.banking.account.exception.AccountNotFoundException;
import com.enterprise.banking.account.exception.InsufficientBalanceException;
import com.enterprise.banking.account.model.Account;
import com.enterprise.banking.account.model.AccountStatus;
import com.enterprise.banking.account.model.AccountType;
import com.enterprise.banking.account.model.dto.AccountResponse;
import com.enterprise.banking.account.model.dto.BalanceResponse;
import com.enterprise.banking.account.model.dto.CreateAccountRequest;
import com.enterprise.banking.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BalanceCacheService balanceCacheService;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private UUID testAccountId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testAccount = Account.builder()
                .id(testAccountId)
                .userId(testUserId)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .dailyLimit(new BigDecimal("10000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAccount_Success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(testUserId)
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("1000.00"))
                .build();

        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals(testUserId, response.getUserId());
        assertEquals(AccountType.SAVINGS, response.getAccountType());
        verify(accountRepository).save(any(Account.class));
        verify(balanceCacheService).cacheBalance(any(), any());
    }

    @Test
    void getAccount_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        AccountResponse response = accountService.getAccount(testAccountId);

        assertNotNull(response);
        assertEquals(testAccountId, response.getId());
        assertEquals("1234567890", response.getAccountNumber());
    }

    @Test
    void getAccount_NotFound_ThrowsException() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(testAccountId));
    }

    @Test
    void getAccountsByUserId_Success() {
        when(accountRepository.findByUserId(testUserId)).thenReturn(List.of(testAccount));

        List<AccountResponse> responses = accountService.getAccountsByUserId(testUserId);

        assertEquals(1, responses.size());
        assertEquals(testUserId, responses.get(0).getUserId());
    }

    @Test
    void getBalance_CacheHit() {
        when(balanceCacheService.getCachedBalance(testAccountId))
                .thenReturn(Optional.of(new BigDecimal("5000.00")));
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        BalanceResponse response = accountService.getBalance(testAccountId);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
    }

    @Test
    void getBalance_CacheMiss() {
        when(balanceCacheService.getCachedBalance(testAccountId)).thenReturn(Optional.empty());
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        BalanceResponse response = accountService.getBalance(testAccountId);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
        verify(balanceCacheService).cacheBalance(testAccountId, new BigDecimal("5000.00"));
    }

    @Test
    void credit_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.credit(testAccountId, new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("6000.00"), testAccount.getBalance());
        verify(accountRepository).save(testAccount);
        verify(balanceCacheService).cacheBalance(any(), any());
    }

    @Test
    void debit_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.debit(testAccountId, new BigDecimal("2000.00"));

        assertEquals(new BigDecimal("3000.00"), testAccount.getBalance());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void debit_InsufficientBalance_ThrowsException() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientBalanceException.class,
                () -> accountService.debit(testAccountId, new BigDecimal("10000.00")));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateStatus_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.updateStatus(testAccountId, AccountStatus.FROZEN);

        assertEquals(AccountStatus.FROZEN, testAccount.getStatus());
        verify(accountRepository).save(testAccount);
    }
}