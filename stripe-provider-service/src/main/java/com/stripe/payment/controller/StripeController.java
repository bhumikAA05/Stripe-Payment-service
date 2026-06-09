package com.stripe.payment.controller;

import com.stripe.model.checkout.Session;
import com.stripe.payment.dto.CreateSessionRequest;
import com.stripe.payment.dto.CreateSessionResponse;
import com.stripe.payment.service.StripeService;
import com.stripe.payment.service.StripeWebhookService;
import com.stripe.payment.util.StripeConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.model.Event;

@Slf4j
@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
@Tag(name = "Stripe API", description = "Stripe Checkout Session management endpoints")
public class StripeController {

    private final StripeService stripeService;
    private final StripeWebhookService webhookService;

    /**
     * POST /api/v1/stripe/create-session
     * W5D1 - CreateSession API
     */
    @PostMapping("/create-session")
    @Operation(summary = "Create Stripe checkout session", description = "Creates a new Stripe Checkout Session for payment processing")
    public ResponseEntity<CreateSessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request) {
        log.info("REST: create-session called for txnId: {}", request.getTxnId());
        CreateSessionResponse response = stripeService.createSession(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/stripe/get-session/{sessionId}
     * W5D1 - GetSession API
     */
    @GetMapping("/get-session/{sessionId}")
    @Operation(summary = "Get Stripe session by ID")
    public ResponseEntity<Session> getSession(@PathVariable String sessionId) {
        log.info("REST: get-session called for: {}", sessionId);
        Session session = stripeService.getSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * PUT /api/v1/stripe/expire/{sessionId}
     * W5D1 - ExpireSession API
     */
    @PutMapping("/expire/{sessionId}")
    @Operation(summary = "Expire a Stripe checkout session")
    public ResponseEntity<Session> expireSession(@PathVariable String sessionId) {
        log.info("REST: expire-session called for: {}", sessionId);
        Session session = stripeService.expireSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * POST /api/v1/stripe/webhook
     * W6D4 - Webhook endpoint with HmacSHA256 verification
     */
    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook endpoint", description = "Receives and processes Stripe webhook events with signature verification")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(StripeConstants.STRIPE_SIGNATURE_HEADER) String sigHeader) {
        log.info("Webhook received. Verifying Stripe signature...");
        Event event = webhookService.verifyAndParseEvent(payload, sigHeader);
        webhookService.processWebhookEvent(event);
        return ResponseEntity.ok("Webhook received and queued for processing");
    }
}
