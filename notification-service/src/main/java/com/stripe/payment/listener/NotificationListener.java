package com.stripe.payment.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * W8D1 - Notification Processing - Coding usecases
 * W8D5 - Project Release process - Recon Service usecase - ActiveMQ usecase
 *
 * Listens to ActiveMQ queue and processes payment notification events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final ObjectMapper objectMapper;

    @JmsListener(destination = "payment.notification.queue")
    public void onMessage(String message) {
        log.info("[ActiveMQ] Received message: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType   = node.get("eventType").asText();
            String txnId       = node.get("txnId").asText();
            String email       = node.get("email").asText();

            switch (eventType) {
                case "TXN_CREATED"       -> handleTxnCreated(txnId, email);
                case "TXN_STATUS_UPDATE" -> handleStatusUpdate(txnId, email);
                default -> log.warn("Unhandled notification eventType: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", e.getMessage());
        }
    }

    private void handleTxnCreated(String txnId, String email) {
        log.info("Sending TXN_CREATED notification to {} for txnId: {}", email, txnId);
        // In production: send email via JavaMailSender or SMS via Twilio
        // emailService.sendTransactionCreatedEmail(email, txnId);
    }

    private void handleStatusUpdate(String txnId, String email) {
        log.info("Sending TXN_STATUS_UPDATE notification to {} for txnId: {}", email, txnId);
        // emailService.sendStatusUpdateEmail(email, txnId);
    }
}
