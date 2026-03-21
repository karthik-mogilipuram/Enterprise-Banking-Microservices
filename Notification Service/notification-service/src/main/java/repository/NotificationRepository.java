package com.enterprise.banking.notification.repository;

import com.enterprise.banking.notification.model.Notification;
import com.enterprise.banking.notification.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserId(UUID userId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    boolean existsByReferenceId(String referenceId);
}