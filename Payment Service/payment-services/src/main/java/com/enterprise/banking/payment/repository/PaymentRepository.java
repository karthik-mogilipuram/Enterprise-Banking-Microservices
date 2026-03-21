package com.enterprise.banking.payment.repository;

import com.enterprise.banking.payment.model.Payment;
import com.enterprise.banking.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findBySourceAccountIdOrDestAccountId(UUID sourceAccountId, UUID destAccountId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findBySagaId(UUID sagaId);
}