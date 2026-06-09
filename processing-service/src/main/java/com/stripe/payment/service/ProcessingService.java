package com.stripe.payment.service;

import com.stripe.payment.dto.CreateTxnRequest;
import com.stripe.payment.dto.TransactionDTO;
import com.stripe.payment.exception.TransactionNotFoundException;
import com.stripe.payment.model.PaymentTransaction;
import com.stripe.payment.model.TxnStatus;
import com.stripe.payment.repository.PaymentStatusSystemDAO;
import com.stripe.payment.repository.TransactionDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * W5D2 - Working with DB - PaymentDB Integration Overview
 * W5D3 - Coding processing 2 APIs - API Versioning - CreateTxnRequest - Factory Design Pattern
 * W6D1 - Coding Update Status & Pending Status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final TransactionDAO transactionDAO;
    private final PaymentStatusSystemDAO statusSystemDAO;
    private final JmsTemplate jmsTemplate;

    /**
     * Creates a new transaction and initiates payment via Stripe
     */
    @Transactional
    public TransactionDTO createTransaction(CreateTxnRequest request) {
        log.info("Creating transaction for customer: {}", request.getCustomerEmail());

        PaymentTransaction txn = PaymentTransaction.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .sessionId(request.getSessionId())
                .build();

        PaymentTransaction saved = transactionDAO.save(txn);

        // Record initial status in audit trail
        statusSystemDAO.recordStatusChange(
                saved.getTxnId(), null, TxnStatus.INITIATED,
                "SYSTEM", "Transaction initiated"
        );

        // Send notification via ActiveMQ
        sendNotification("TXN_CREATED", saved.getTxnId(), saved.getCustomerEmail());

        return mapToDTO(saved);
    }

    /**
     * W6D1 - Update Status & Pending Status logic
     */
    @Transactional
    public TransactionDTO updateTransactionStatus(String txnId, TxnStatus newStatus, String changedBy) {
        PaymentTransaction txn = transactionDAO.findByTxnId(txnId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + txnId));

        TxnStatus oldStatus = txn.getStatus();
        transactionDAO.updateStatus(txnId, newStatus);

        // Record status change in audit trail
        statusSystemDAO.recordStatusChange(txnId, oldStatus, newStatus, changedBy,
                "Status updated from " + oldStatus + " to " + newStatus);

        // Trigger notification on completion or failure
        if (newStatus == TxnStatus.COMPLETED || newStatus == TxnStatus.FAILED) {
            sendNotification("TXN_STATUS_UPDATE", txnId, txn.getCustomerEmail());
        }

        txn.setStatus(newStatus);
        return mapToDTO(txn);
    }

    /**
     * W6D1 - Get all Pending transactions (batch processing use case)
     */
    public List<TransactionDTO> getPendingTransactions() {
        return transactionDAO.findByStatus(TxnStatus.PENDING)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransaction(String txnId) {
        PaymentTransaction txn = transactionDAO.findByTxnId(txnId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + txnId));
        return mapToDTO(txn);
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionDAO.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * W8D5 - ActiveMQ: Publish notification event to queue
     */
    private void sendNotification(String eventType, String txnId, String customerEmail) {
        try {
            String message = String.format("{\"eventType\":\"%s\",\"txnId\":\"%s\",\"email\":\"%s\"}",
                    eventType, txnId, customerEmail);
            jmsTemplate.convertAndSend("payment.notification.queue", message);
            log.info("Notification sent to queue: eventType={}, txnId={}", eventType, txnId);
        } catch (Exception e) {
            log.error("Failed to send notification for txnId={}: {}", txnId, e.getMessage());
        }
    }

    /**
     * W5D4 - ModelMapper converter (manual mapping pattern)
     */
    private TransactionDTO mapToDTO(PaymentTransaction txn) {
        return TransactionDTO.builder()
                .id(txn.getId())
                .txnId(txn.getTxnId())
                .customerName(txn.getCustomerName())
                .customerEmail(txn.getCustomerEmail())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus())
                .sessionId(txn.getSessionId())
                .paymentMethod(txn.getPaymentMethod())
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }
}
