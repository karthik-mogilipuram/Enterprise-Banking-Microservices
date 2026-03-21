package com.enterprise.banking.notification.model.dto;

import com.enterprise.banking.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private UUID userId;
    private String recipient;
    private NotificationType type;
    private String subject;
    private String eventType;
    private String referenceId;

    // Transaction/Payment details for message body
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String accountNumber;
    private String description;
}