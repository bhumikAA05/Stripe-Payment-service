package com.stripe.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResponse {
    private String sessionId;
    private String sessionUrl;
    private String status;
    private String txnId;
    private String customerEmail;
    private Long amount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
