package com.stripe.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * W2D3 - Payment Domain & Microservices
 * W5D5 - Save Created Txn - Spring JDBC - Enums - ModelMapper Converters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

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
