package com.enterprise.banking.notification.service;

import com.enterprise.banking.notification.model.Notification;
import com.enterprise.banking.notification.model.NotificationStatus;
import com.enterprise.banking.notification.model.NotificationType;
import com.enterprise.banking.notification.model.dto.NotificationEvent;
import com.enterprise.banking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    private static final int MAX_RETRY_COUNT = 3;

    @Transactional
    public Notification sendNotification(NotificationEvent event) {

        // Prevent duplicate notifications for same transaction
        if (event.getReferenceId() != null &&
                notificationRepository.existsByReferenceId(event.getReferenceId())) {
            log.warn("Duplicate notification skipped for referenceId: {}", event.getReferenceId());
            return null;
        }

        // Build the message based on event type
        String subject = buildSubject(event);
        String message = buildMessage(event);

        // Save notification record as PENDING
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .recipient(event.getRecipient())
                .type(event.getType() != null ? event.getType() : NotificationType.EMAIL)
                .subject(subject)
                .message(message)
                .referenceId(event.getReferenceId())
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);

        // Attempt to send
        try {
            if (notification.getType() == NotificationType.EMAIL) {
                emailService.sendSimpleEmail(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage()
                );
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info("Notification sent successfully to: {}", notification.getRecipient());

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            log.error("Failed to send notification to {}: {}", notification.getRecipient(), e.getMessage());
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }

    // Retry failed notifications every 5 minutes
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, MAX_RETRY_COUNT);

        if (failedNotifications.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notifications", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                if (notification.getType() == NotificationType.EMAIL) {
                    emailService.sendSimpleEmail(
                            notification.getRecipient(),
                            notification.getSubject(),
                            notification.getMessage()
                    );
                }

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setErrorMessage(null);
                log.info("Retry succeeded for notification: {}", notification.getId());

            } catch (Exception e) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setErrorMessage(e.getMessage());

                if (notification.getRetryCount() >= MAX_RETRY_COUNT) {
                    notification.setStatus(NotificationStatus.FAILED);
                    log.error("Notification {} permanently failed after {} retries",
                            notification.getId(), MAX_RETRY_COUNT);
                } else {
                    notification.setStatus(NotificationStatus.RETRY);
                }
            }

            notificationRepository.save(notification);
        }
    }

    private String buildSubject(NotificationEvent event) {
        if (event.getEventType() == null) return "Banking Notification";
        return switch (event.getEventType().toUpperCase()) {
            case "DEPOSIT" -> "Deposit Confirmation — $" + event.getAmount();
            case "WITHDRAWAL" -> "Withdrawal Alert — $" + event.getAmount();
            case "TRANSFER" -> "Transfer Receipt — $" + event.getAmount();
            default -> "Banking Notification";
        };
    }

    private String buildMessage(NotificationEvent event) {
        String name = event.getRecipient();
        String accountNumber = event.getAccountNumber() != null ? event.getAccountNumber() : "N/A";
        String refId = event.getReferenceId() != null ? event.getReferenceId() : "N/A";

        if (event.getEventType() == null) return event.getDescription();

        return switch (event.getEventType().toUpperCase()) {
            case "DEPOSIT" -> emailService.buildDepositEmailBody(
                    name, accountNumber, event.getAmount(), event.getBalanceAfter(), refId);
            case "WITHDRAWAL" -> emailService.buildWithdrawalEmailBody(
                    name, accountNumber, event.getAmount(), event.getBalanceAfter(), refId);
            case "TRANSFER" -> emailService.buildTransferEmailBody(
                    name, accountNumber, event.getAmount(), event.getBalanceAfter(), refId);
            default -> event.getDescription() != null ? event.getDescription() : "Banking transaction processed.";
        };
    }
}