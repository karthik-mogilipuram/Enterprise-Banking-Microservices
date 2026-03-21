package com.enterprise.banking.payment.service.saga;

import com.enterprise.banking.payment.client.AccountServiceClient;
import com.enterprise.banking.payment.exception.PaymentFailedException;
import com.enterprise.banking.payment.model.Payment;
import com.enterprise.banking.payment.model.PaymentStatus;
import com.enterprise.banking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferSagaOrchestrator {

    private final AccountServiceClient accountServiceClient;
    private final PaymentRepository paymentRepository;

    public Payment execute(Payment payment) {
        UUID sagaId = UUID.randomUUID();
        payment.setSagaId(sagaId);
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        log.info("Saga {} started: Transfer {} from {} to {}",
                sagaId, payment.getAmount(), payment.getSourceAccountId(), payment.getDestAccountId());

        try {
            // Step 1: Debit source account
            log.info("Saga {} Step 1: Debiting source account {}", sagaId, payment.getSourceAccountId());
            accountServiceClient.debit(payment.getSourceAccountId(), payment.getAmount());

            // Step 2: Credit destination account
            creditDestinationAccount(payment, sagaId);

            // Success
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("Saga {} completed successfully", sagaId);
            return payment;

        } catch (PaymentFailedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("Saga {} failed: {}", sagaId, e.getMessage());
            throw e;
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("Saga {} failed at debit step: {}", sagaId, e.getMessage());
            throw new PaymentFailedException("Transfer failed: " + e.getMessage());
        }
    }

    private void creditDestinationAccount(Payment payment, UUID sagaId) {
        try {
            log.info("Saga {} Step 2: Crediting destination account {}", sagaId, payment.getDestAccountId());
            accountServiceClient.credit(payment.getDestAccountId(), payment.getAmount());
        } catch (Exception e) {
            // Compensation: Reverse the debit
            log.error("Saga {} Step 2 failed, compensating: {}", sagaId, e.getMessage());
            accountServiceClient.credit(payment.getSourceAccountId(), payment.getAmount());
            throw new PaymentFailedException("Transfer failed during credit. Debit reversed. " + e.getMessage());
        }
    }
}