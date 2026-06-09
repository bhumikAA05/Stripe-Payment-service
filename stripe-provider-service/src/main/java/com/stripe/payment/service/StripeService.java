package com.stripe.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionExpireParams;
import com.stripe.payment.dto.CreateSessionRequest;
import com.stripe.payment.dto.CreateSessionResponse;
import com.stripe.payment.exception.StripeServiceException;
import com.stripe.payment.util.StripeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class StripeService {

    /**
     * W5D1 - Coding Stripe CreateSession API
     * Creates a Stripe Checkout Session for payment
     */
    public CreateSessionResponse createSession(CreateSessionRequest request) {
        log.info("Creating Stripe session for txnId: {}, amount: {} {}",
                request.getTxnId(), request.getAmount(), request.getCurrency());
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(request.getCustomerEmail())
                    .setSuccessUrl(request.getSuccessUrl() != null
                            ? request.getSuccessUrl() : StripeConstants.DEFAULT_SUCCESS_URL)
                    .setCancelUrl(request.getCancelUrl() != null
                            ? request.getCancelUrl() : StripeConstants.DEFAULT_CANCEL_URL)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency().toLowerCase())
                                                    .setUnitAmount(request.getAmount())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Payment - " + request.getCustomerName())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata(StripeConstants.METADATA_TXN_ID, request.getTxnId())
                    .putMetadata(StripeConstants.METADATA_CUSTOMER_NAME, request.getCustomerName())
                    .build();

            Session session = Session.create(params);
            log.info("Stripe session created successfully: sessionId={}", session.getId());

            return CreateSessionResponse.builder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status(session.getStatus())
                    .txnId(request.getTxnId())
                    .customerEmail(request.getCustomerEmail())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();

        } catch (StripeException e) {
            log.error("Stripe API error during session creation: {}", e.getMessage());
            throw new StripeServiceException(
                    "Failed to create Stripe session: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    "STRIPE_SESSION_CREATE_FAILED"
            );
        }
    }

    /**
     * W5D1 - Coding Stripe GetSession API
     * Retrieves an existing Stripe Checkout Session by ID
     */
    public Session getSession(String sessionId) {
        log.info("Retrieving Stripe session: {}", sessionId);
        try {
            Session session = Session.retrieve(sessionId);
            log.info("Session retrieved: id={}, status={}", session.getId(), session.getStatus());
            return session;
        } catch (StripeException e) {
            log.error("Error retrieving session {}: {}", sessionId, e.getMessage());
            throw new StripeServiceException(
                    "Failed to get Stripe session: " + e.getMessage(),
                    HttpStatus.NOT_FOUND,
                    "STRIPE_SESSION_NOT_FOUND"
            );
        }
    }

    /**
     * W5D1 - Coding Stripe ExpireSession API
     * Expires a Stripe Checkout Session
     */
    public Session expireSession(String sessionId) {
        log.info("Expiring Stripe session: {}", sessionId);
        try {
            Session session = Session.retrieve(sessionId);
            Session expiredSession = session.expire(SessionExpireParams.builder().build());
            log.info("Session expired: id={}, status={}", expiredSession.getId(), expiredSession.getStatus());
            return expiredSession;
        } catch (StripeException e) {
            log.error("Error expiring session {}: {}", sessionId, e.getMessage());
            throw new StripeServiceException(
                    "Failed to expire Stripe session: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    "STRIPE_SESSION_EXPIRE_FAILED"
            );
        }
    }
}
