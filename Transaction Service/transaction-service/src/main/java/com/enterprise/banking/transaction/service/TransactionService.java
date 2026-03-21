package com.enterprise.banking.transaction.service;

import com.enterprise.banking.transaction.client.AccountServiceClient;
import com.enterprise.banking.transaction.exception.InvalidTransactionException;
import com.enterprise.banking.transaction.model.Transaction;
import com.enterprise.banking.transaction.model.TransactionStatus;
import com.enterprise.banking.transaction.model.TransactionType;
import com.enterprise.banking.transaction.model.dto.*;
import com.enterprise.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionValidationService validationService;
    private final AccountServiceClient accountServiceClient;

    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        validationService.validateDeposit(request.getAmount());

        // Get current account info
        Map<String, Object> account = accountServiceClient.getAccount(request.getAccountId());
        String status = (String) account.get("status");

        if (!"ACTIVE".equals(status)) {
            throw new InvalidTransactionException("Account is not active");
        }

        // Credit the account
        accountServiceClient.credit(request.getAccountId(), request.getAmount());

        // Get updated balance
        Map<String, Object> balance = accountServiceClient.getBalance(request.getAccountId());
        BigDecimal newBalance = new BigDecimal(balance.get("balance").toString());

        // Record transaction
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .status(TransactionStatus.COMPLETED)
                .referenceId(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .description(request.getDescription() != null ? request.getDescription() : "Deposit")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Deposit completed: {} to account {}", request.getAmount(), request.getAccountId());

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request) {
        // Get current account info
        Map<String, Object> account = accountServiceClient.getAccount(request.getAccountId());
        String status = (String) account.get("status");

        if (!"ACTIVE".equals(status)) {
            throw new InvalidTransactionException("Account is not active");
        }

        BigDecimal currentBalance = new BigDecimal(account.get("balance").toString());
        BigDecimal dailyLimit = new BigDecimal(account.get("dailyLimit").toString());

        // Validate withdrawal
        validationService.validateWithdrawal(
                request.getAmount(), currentBalance, request.getAccountId(), dailyLimit
        );

        // Debit the account
        accountServiceClient.debit(request.getAccountId(), request.getAmount());

        // Get updated balance
        Map<String, Object> balance = accountServiceClient.getBalance(request.getAccountId());
        BigDecimal newBalance = new BigDecimal(balance.get("balance").toString());

        // Record transaction
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .status(TransactionStatus.COMPLETED)
                .referenceId(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Withdrawal completed: {} from account {}", request.getAmount(), request.getAccountId());

        return toResponse(saved);
    }

    public TransactionResponse getTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new InvalidTransactionException("Transaction not found: " + transactionId));
        return toResponse(transaction);
    }

    public Page<TransactionResponse> getTransactionHistory(UUID accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(this::toResponse);
    }

    public Map<String, Object> getDailySummary(UUID accountId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Transaction> todayTransactions = transactionRepository
                .findByAccountIdAndCreatedAtBetween(accountId, startOfDay, endOfDay);

        BigDecimal totalDeposits = todayTransactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = todayTransactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "accountId", accountId,
                "date", LocalDate.now(),
                "totalDeposits", totalDeposits,
                "totalWithdrawals", totalWithdrawals,
                "transactionCount", todayTransactions.size()
        );
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .status(transaction.getStatus())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
