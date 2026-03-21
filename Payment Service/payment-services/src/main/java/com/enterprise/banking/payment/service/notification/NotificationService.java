package com.enterprise.banking.payment.service.notification;

import com.enterprise.banking.payment.model.Notification;
import com.enterprise.banking.payment.model.Payment;
import com.enterprise.banking.payment.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public void sendTransferNotification(Payment payment, UUID sourceUserId, UUID destUserId) {
        // Notify sender
        String senderSubject = "Transfer Sent: $" + payment.getAmount();
        String senderBody = "You transferred $" + payment.getAmount() + " to account " + payment.getDestAccountId();

        Notification senderNotification = Notification.builder()
                .userId(sourceUserId)
                .type("EMAIL")
                .subject(senderSubject)
                .body(senderBody)
                .status("SENT")
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(senderNotification);
        emailService.sendTransferConfirmation(sourceUserId, senderSubject, senderBody);

        // Notify receiver
        String receiverSubject = "Transfer Received: $" + payment.getAmount();
        String receiverBody = "You received $" + payment.getAmount() + " from account " + payment.getSourceAccountId();

        Notification receiverNotification = Notification.builder()
                .userId(destUserId)
                .type("EMAIL")
                .subject(receiverSubject)
                .body(receiverBody)
                .status("SENT")
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(receiverNotification);
        emailService.sendTransferConfirmation(destUserId, receiverSubject, receiverBody);

        log.info("Transfer notifications sent for payment {}", payment.getId());
    }

    public List<Notification> getNotificationsByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}