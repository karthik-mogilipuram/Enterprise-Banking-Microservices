package com.enterprise.banking.account.service;

import com.enterprise.banking.account.exception.AccountNotFoundException;
import com.enterprise.banking.account.exception.InsufficientBalanceException;
import com.enterprise.banking.account.model.Account;
import com.enterprise.banking.account.model.AccountStatus;
import com.enterprise.banking.account.model.dto.*;
import com.enterprise.banking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final BalanceCacheService balanceCacheService;
    private static final Random RANDOM = new Random();

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String accountNumber = generateAccountNumber();

        BigDecimal initialBalance = request.getInitialDeposit() != null
                ? request.getInitialDeposit()
                : BigDecimal.ZERO;

        Account account = Account.builder()
                .userId(request.getUserId())
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(initialBalance)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .build();

        Account saved = accountRepository.save(account);
        balanceCacheService.cacheBalance(saved.getId(), saved.getBalance());

        return toResponse(saved);
    }

    public AccountResponse getAccount(UUID accountId) {
        Account account = findAccountOrThrow(accountId);
        return toResponse(account);
    }

    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BalanceResponse getBalance(UUID accountId) {
        // Try cache first
        var cachedBalance = balanceCacheService.getCachedBalance(accountId);
        if (cachedBalance.isPresent()) {
            Account account = findAccountOrThrow(accountId);
            return BalanceResponse.builder()
                    .accountId(accountId)
                    .accountNumber(account.getAccountNumber())
                    .balance(cachedBalance.get())
                    .currency(account.getCurrency())
                    .build();
        }

        // Cache miss — fetch from DB
        Account account = findAccountOrThrow(accountId);
        balanceCacheService.cacheBalance(accountId, account.getBalance());

        return BalanceResponse.builder()
                .accountId(accountId)
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build();
    }

    @Transactional
    public AccountResponse updateAccount(UUID accountId, CreateAccountRequest request) {
        Account account = findAccountOrThrow(accountId);

        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }

        Account updated = accountRepository.save(account);
        return toResponse(updated);
    }

    @Transactional
    public AccountResponse updateStatus(UUID accountId, AccountStatus status) {
        Account account = findAccountOrThrow(accountId);
        account.setStatus(status);
        Account updated = accountRepository.save(account);

        if (status == AccountStatus.CLOSED) {
            balanceCacheService.evictBalance(accountId);
        }

        return toResponse(updated);
    }

    @Transactional
public AccountResponse credit(UUID accountId, BigDecimal amount) {
    Account account = findAccountOrThrow(accountId);
    account.setBalance(account.getBalance().add(amount));
    Account updated = accountRepository.save(account);
    balanceCacheService.cacheBalance(accountId, updated.getBalance());
    return toResponse(updated);
}

@Transactional
public AccountResponse debit(UUID accountId, BigDecimal amount) {
    Account account = findAccountOrThrow(accountId);

    if (account.getBalance().compareTo(amount) < 0) {
        throw new InsufficientBalanceException(
                "Insufficient balance. Available: " + account.getBalance() + ", Requested: " + amount
        );
    }

    account.setBalance(account.getBalance().subtract(amount));
    Account updated = accountRepository.save(account);
    balanceCacheService.cacheBalance(accountId, updated.getBalance());
    return toResponse(updated);
    }

    private Account findAccountOrThrow(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("%010d", RANDOM.nextLong(1_000_000_000L, 9_999_999_999L));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .dailyLimit(account.getDailyLimit())
                .createdAt(account.getCreatedAt())
                .build();
    }
}