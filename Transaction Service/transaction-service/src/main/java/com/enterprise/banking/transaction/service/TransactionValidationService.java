package com.enterprise.banking.transaction.service;

import com.enterprise.banking.transaction.exception.DailyLimitExceededException;
import com.enterprise.banking.transaction.exception.InvalidTransactionException;
import com.enterprise.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionValidationService {

    private final TransactionRepository transactionRepository;

    public void validateDeposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be greater than 0");
        }
        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            throw new InvalidTransactionException("Single deposit cannot exceed $50,000");
        }
    }

    public void validateWithdrawal(BigDecimal amount, BigDecimal currentBalance, UUID accountId, BigDecimal dailyLimit) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be greater than 0");
        }
        if (amount.compareTo(currentBalance) > 0) {
            throw new InvalidTransactionException(
                    "Insufficient balance. Available: " + currentBalance + ", Requested: " + amount
            );
        }

        // Check daily withdrawal limit
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal dailyTotal = transactionRepository.getDailyWithdrawalTotal(accountId, startOfDay);
        BigDecimal newTotal = dailyTotal.add(amount);

        if (newTotal.compareTo(dailyLimit) > 0) {
            BigDecimal remaining = dailyLimit.subtract(dailyTotal);
            throw new DailyLimitExceededException(
                    "Daily withdrawal limit exceeded. Limit: " + dailyLimit +
                    ", Used today: " + dailyTotal +
                    ", Remaining: " + remaining
            );
        }
    }
}
