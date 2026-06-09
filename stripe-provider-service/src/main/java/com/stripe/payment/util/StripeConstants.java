package com.stripe.payment.util;

public final class StripeConstants {

    private StripeConstants() {}

    // Session statuses
    public static final String SESSION_STATUS_OPEN       = "open";
    public static final String SESSION_STATUS_COMPLETE   = "complete";
    public static final String SESSION_STATUS_EXPIRED    = "expired";

    // Payment method types
    public static final String PAYMENT_METHOD_CARD        = "card";
    public static final String PAYMENT_METHOD_UPI         = "upi";
    public static final String PAYMENT_METHOD_NETBANKING  = "netbanking";

    // Stripe checkout mode
    public static final String MODE_PAYMENT = "payment";

    // Default URLs (overridden via application.yml)
    public static final String DEFAULT_SUCCESS_URL = "http://localhost:8080/payment/success?session_id={CHECKOUT_SESSION_ID}";
    public static final String DEFAULT_CANCEL_URL  = "http://localhost:8080/payment/cancel";

    // Header keys
    public static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

    // Metadata keys stored in Stripe session
    public static final String METADATA_TXN_ID       = "txnId";
    public static final String METADATA_CUSTOMER_NAME = "customerName";
}
