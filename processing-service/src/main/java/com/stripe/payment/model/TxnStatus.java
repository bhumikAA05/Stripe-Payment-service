package com.stripe.payment.model;

/**
 * W5D5 - Enums for Transaction Status
 */
public enum TxnStatus {
    INITIATED,
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    EXPIRED,
    CANCELLED
}
