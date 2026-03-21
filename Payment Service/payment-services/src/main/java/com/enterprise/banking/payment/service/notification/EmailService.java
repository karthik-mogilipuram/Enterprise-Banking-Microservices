package com.enterprise.banking.payment.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class EmailService {

    public void sendTransferConfirmation(UUID userId, String subject, String body) {
        // Mock email sending - replace with JavaMailSender in production
        log.info("Sending email to user {}: Subject: {}", userId, subject);
        log.info("Email body: {}", body);
    }
}