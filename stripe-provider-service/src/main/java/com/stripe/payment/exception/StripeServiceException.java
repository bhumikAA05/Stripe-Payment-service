package com.stripe.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StripeServiceException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public StripeServiceException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public StripeServiceException(String message, HttpStatus status) {
        this(message, status, "STRIPE_ERROR");
    }
}
