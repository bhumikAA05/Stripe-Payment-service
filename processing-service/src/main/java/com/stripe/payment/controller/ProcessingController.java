package com.stripe.payment.controller;

import com.stripe.payment.dto.CreateTxnRequest;
import com.stripe.payment.dto.TransactionDTO;
import com.stripe.payment.model.TxnStatus;
import com.stripe.payment.service.ProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * W3D2 - Coding RestAPI & RestAPI Standards
 * W5D3 - API Versioning (/api/v1/)
 * W3D5 - Packaging, Swagger
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "Payment transaction management")
public class ProcessingController {

    private final ProcessingService processingService;

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody CreateTxnRequest request) {
        log.info("POST /transactions - creating transaction for {}", request.getCustomerEmail());
        TransactionDTO dto = processingService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    @Operation(summary = "Get all transactions")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(processingService.getAllTransactions());
    }

    @GetMapping("/{txnId}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable String txnId) {
        return ResponseEntity.ok(processingService.getTransaction(txnId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending transactions")
    public ResponseEntity<List<TransactionDTO>> getPendingTransactions() {
        return ResponseEntity.ok(processingService.getPendingTransactions());
    }

    /**
     * W6D1 - Update status endpoint
     */
    @PutMapping("/{txnId}/status")
    @Operation(summary = "Update transaction status")
    public ResponseEntity<TransactionDTO> updateStatus(
            @PathVariable String txnId,
            @RequestBody Map<String, String> body) {
        TxnStatus newStatus = TxnStatus.valueOf(body.get("status").toUpperCase());
        String changedBy = body.getOrDefault("changedBy", "SYSTEM");
        log.info("PUT /transactions/{}/status -> {}", txnId, newStatus);
        return ResponseEntity.ok(processingService.updateTransactionStatus(txnId, newStatus, changedBy));
    }
}
