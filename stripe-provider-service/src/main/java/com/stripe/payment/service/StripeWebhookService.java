package com.stripe.payment.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.payment.exception.StripeServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * W6D4 - Stripe Webhook processing - Part1
 * HmacSHA256 security + Async Thread processing
 */
@Slf4j
@Service
public class StripeWebhookService {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Verifies Stripe webhook signature using HmacSHA256
     * and processes the event asynchronously
     */
    public Event verifyAndParseEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Webhook signature verified. Event type: {}, id: {}", event.getType(), event.getId());
            return event;
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            throw new StripeServiceException(
                    "Invalid webhook signature",
                    HttpStatus.BAD_REQUEST,
                    "WEBHOOK_SIGNATURE_INVALID"
            );
        }
    }

    /**
     * W6D5 - Stripe Webhook processing logic
     * Processes webhook events asynchronously in a separate thread
     */
    @Async
    public void processWebhookEvent(Event event) {
        log.info("[ASYNC THREAD: {}] Processing webhook event: {}",
                Thread.currentThread().getName(), event.getType());

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                deserializer.getObject().ifPresent(obj -> {
                    PaymentIntent paymentIntent = (PaymentIntent) obj;
                    String txnId = paymentIntent.getMetadata().get("txnId");
                    log.info("Payment succeeded for txnId: {}, amount: {} {}",
                            txnId, paymentIntent.getAmount(), paymentIntent.getCurrency());
                    // Notify processing-service to update status -> COMPLETED
                });
            }
            case "payment_intent.payment_failed" -> {
                deserializer.getObject().ifPresent(obj -> {
                    PaymentIntent pi = (PaymentIntent) obj;
                    log.warn("Payment FAILED for txnId: {}", pi.getMetadata().get("txnId"));
                    // Notify processing-service to update status -> FAILED
                });
            }
            case "checkout.session.completed" -> {
                deserializer.getObject().ifPresent(obj -> {
                    Session session = (Session) obj;
                    String txnId = session.getMetadata().get("txnId");
                    log.info("Checkout session COMPLETED: sessionId={}, txnId={}", session.getId(), txnId);
                });
            }
            case "checkout.session.expired" -> {
                deserializer.getObject().ifPresent(obj -> {
                    Session session = (Session) obj;
                    log.warn("Checkout session EXPIRED: sessionId={}", session.getId());
                });
            }
            default -> log.info("Unhandled webhook event type: {}", event.getType());
        }
    }
}
