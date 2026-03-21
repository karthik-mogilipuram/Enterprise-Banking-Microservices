package com.enterprise.banking.notification.controller;

import com.enterprise.banking.notification.model.Notification;
import com.enterprise.banking.notification.model.NotificationStatus;
import com.enterprise.banking.notification.model.dto.NotificationEvent;
import com.enterprise.banking.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(
            @Valid @RequestBody NotificationEvent event) {
        Notification notification = notificationService.sendNotification(event);
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(
            @PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.getNotificationsByStatus(status));
    }

    @PostMapping("/retry")
    public ResponseEntity<String> triggerRetry() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok("Retry triggered successfully");
    }
}