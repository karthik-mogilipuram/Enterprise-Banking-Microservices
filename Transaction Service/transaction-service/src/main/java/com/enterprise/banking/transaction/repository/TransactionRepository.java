package com.enterprise.banking.transaction.repository;

import com.enterprise.banking.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    List<Transaction> findByAccountIdAndCreatedAtBetween(UUID accountId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.accountId = :accountId " +
           "AND t.type IN ('WITHDRAWAL', 'TRANSFER_OUT') " +
           "AND t.status = 'COMPLETED' " +
           "AND t.createdAt >= :startOfDay")
    BigDecimal getDailyWithdrawalTotal(@Param("accountId") UUID accountId, @Param("startOfDay") LocalDateTime startOfDay);
}
