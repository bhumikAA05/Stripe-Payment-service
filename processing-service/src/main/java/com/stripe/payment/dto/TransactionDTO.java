package com.stripe.payment.dto;

import com.stripe.payment.model.TxnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * W5D4 - TransactionDTO with ModelMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String txnId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String currency;
    private TxnStatus status;
    private String sessionId;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
