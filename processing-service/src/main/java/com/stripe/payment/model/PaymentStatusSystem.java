package com.stripe.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * W5D4 - Coding PaymentStatusSystem - TransactionDTO - ModelMapper
 * Tracks every status change for auditing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusSystem {
    private Long id;
    private String txnId;
    private TxnStatus oldStatus;
    private TxnStatus newStatus;
    private String changedBy;
    private String remarks;
    private LocalDateTime changedAt;
}
