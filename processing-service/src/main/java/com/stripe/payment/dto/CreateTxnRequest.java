package com.stripe.payment.dto;

import com.stripe.payment.model.TxnStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * W5D2 - CreateTxnRequest DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTxnRequest {

    @NotBlank(message = "Customer name required")
    private String customerName;

    @Email @NotBlank
    private String customerEmail;

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private String paymentMethod;
    private String sessionId;
}

