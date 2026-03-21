package com.enterprise.banking.payment.model.dto;

import com.enterprise.banking.payment.model.PaymentStatus;
import com.enterprise.banking.payment.model.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private UUID id;
    private UUID sourceAccountId;
    private UUID destAccountId;
    private BigDecimal amount;
    private String currency;
    private PaymentType type;
    private PaymentStatus status;
    private UUID sagaId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}