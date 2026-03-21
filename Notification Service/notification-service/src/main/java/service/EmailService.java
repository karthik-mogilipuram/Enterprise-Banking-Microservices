package com.enterprise.banking.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage());
        }
    }

    public String buildDepositEmailBody(String fullName, String accountNumber,
                                         java.math.BigDecimal amount,
                                         java.math.BigDecimal balanceAfter,
                                         String referenceId) {
        return String.format("""
                Dear %s,
                
                Your deposit has been processed successfully.
                
                Transaction Details:
                ─────────────────────────────
                Account Number : %s
                Amount Deposited : $%.2f
                New Balance : $%.2f
                Reference ID : %s
                ─────────────────────────────
                
                If you did not initiate this transaction, please contact us immediately.
                
                Thank you for banking with us.
                Enterprise Banking Platform
                """,
                fullName, accountNumber, amount, balanceAfter, referenceId);
    }

    public String buildWithdrawalEmailBody(String fullName, String accountNumber,
                                            java.math.BigDecimal amount,
                                            java.math.BigDecimal balanceAfter,
                                            String referenceId) {
        return String.format("""
                Dear %s,
                
                Your withdrawal has been processed successfully.
                
                Transaction Details:
                ─────────────────────────────
                Account Number : %s
                Amount Withdrawn : $%.2f
                New Balance : $%.2f
                Reference ID : %s
                ─────────────────────────────
                
                If you did not initiate this transaction, please contact us immediately.
                
                Thank you for banking with us.
                Enterprise Banking Platform
                """,
                fullName, accountNumber, amount, balanceAfter, referenceId);
    }

    public String buildTransferEmailBody(String fullName, String accountNumber,
                                          java.math.BigDecimal amount,
                                          java.math.BigDecimal balanceAfter,
                                          String referenceId) {
        return String.format("""
                Dear %s,
                
                Your transfer has been processed successfully.
                
                Transaction Details:
                ─────────────────────────────
                Account Number : %s
                Amount Transferred : $%.2f
                New Balance : $%.2f
                Reference ID : %s
                ─────────────────────────────
                
                If you did not initiate this transaction, please contact us immediately.
                
                Thank you for banking with us.
                Enterprise Banking Platform
                """,
                fullName, accountNumber, amount, balanceAfter, referenceId);
    }
}