package com.enterprise.banking.payment.service;

import com.enterprise.banking.payment.client.AccountServiceClient;
import com.enterprise.banking.payment.exception.PaymentFailedException;
import com.enterprise.banking.payment.model.Payment;
import com.enterprise.banking.payment.model.PaymentType;
import com.enterprise.banking.payment.model.dto.PaymentResponse;
import com.enterprise.banking.payment.model.dto.TransferRequest;
import com.enterprise.banking.payment.repository.PaymentRepository;
import com.enterprise.banking.payment.service.notification.NotificationService;
import com.enterprise.banking.payment.service.saga.TransferSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransferSagaOrchestrator sagaOrchestrator;
    private final AccountServiceClient accountServiceClient;
    private final NotificationService notificationService;

    public PaymentResponse transfer(TransferRequest request) {
        if (request.getSourceAccountId().equals(request.getDestAccountId())) {
            throw new PaymentFailedException("Source and destination accounts cannot be the same");
        }

        // Verify both accounts exist and are active
        Map<String, Object> sourceAccount = accountServiceClient.getAccount(request.getSourceAccountId());
        Map<String, Object> destAccount = accountServiceClient.getAccount(request.getDestAccountId());

        if (!"ACTIVE".equals(sourceAccount.get("status"))) {
            throw new PaymentFailedException("Source account is not active");
        }
        if (!"ACTIVE".equals(destAccount.get("status"))) {
            throw new PaymentFailedException("Destination account is not active");
        }

        Payment payment = Payment.builder()
                .sourceAccountId(request.getSourceAccountId())
                .destAccountId(request.getDestAccountId())
                .amount(request.getAmount())
                .type(PaymentType.INTERNAL)
                .build();

        Payment completed = sagaOrchestrator.execute(payment);

        // Send notifications
        try {
            UUID sourceUserId = UUID.fromString(sourceAccount.get("userId").toString());
            UUID destUserId = UUID.fromString(destAccount.get("userId").toString());
            notificationService.sendTransferNotification(completed, sourceUserId, destUserId);
        } catch (Exception e) {
            log.warn("Failed to send notification for payment {}: {}", completed.getId(), e.getMessage());
        }

        return toResponse(completed);
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentFailedException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .sourceAccountId(payment.getSourceAccountId())
                .destAccountId(payment.getDestAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .type(payment.getType())
                .status(payment.getStatus())
                .sagaId(payment.getSagaId())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}

