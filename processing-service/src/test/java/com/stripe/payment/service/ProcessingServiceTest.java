package com.stripe.payment.service;

import com.stripe.payment.dto.CreateTxnRequest;
import com.stripe.payment.dto.TransactionDTO;
import com.stripe.payment.exception.TransactionNotFoundException;
import com.stripe.payment.model.PaymentTransaction;
import com.stripe.payment.model.TxnStatus;
import com.stripe.payment.repository.PaymentStatusSystemDAO;
import com.stripe.payment.repository.TransactionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * W8D4 - Unittesting-Mocking-CodeCoverage
 * Tests for ProcessingService using Mockito
 */
@ExtendWith(MockitoExtension.class)
class ProcessingServiceTest {

    @Mock
    private TransactionDAO transactionDAO;

    @Mock
    private PaymentStatusSystemDAO statusSystemDAO;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private ProcessingService processingService;

    private CreateTxnRequest createTxnRequest;
    private PaymentTransaction savedTransaction;

    @BeforeEach
    void setUp() {
        createTxnRequest = CreateTxnRequest.builder()
                .customerName("Rahul Sharma")
                .customerEmail("rahul@example.com")
                .amount(new BigDecimal("4999.00"))
                .currency("INR")
                .paymentMethod("card")
                .build();

        savedTransaction = PaymentTransaction.builder()
                .id(1L)
                .txnId("TXN-ABC12345")
                .customerName("Rahul Sharma")
                .customerEmail("rahul@example.com")
                .amount(new BigDecimal("4999.00"))
                .currency("INR")
                .status(TxnStatus.INITIATED)
                .paymentMethod("card")
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        when(transactionDAO.save(any(PaymentTransaction.class))).thenReturn(savedTransaction);
        doNothing().when(statusSystemDAO).recordStatusChange(anyString(), any(), any(), anyString(), anyString());

        TransactionDTO result = processingService.createTransaction(createTxnRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTxnId()).isEqualTo("TXN-ABC12345");
        assertThat(result.getStatus()).isEqualTo(TxnStatus.INITIATED);
        assertThat(result.getAmount()).isEqualByComparingTo("4999.00");

        verify(transactionDAO, times(1)).save(any(PaymentTransaction.class));
        verify(statusSystemDAO, times(1)).recordStatusChange(
                eq("TXN-ABC12345"), isNull(), eq(TxnStatus.INITIATED), anyString(), anyString());
    }

    @Test
    @DisplayName("Should update transaction status successfully")
    void shouldUpdateTransactionStatus() {
        when(transactionDAO.findByTxnId("TXN-ABC12345")).thenReturn(Optional.of(savedTransaction));
        when(transactionDAO.updateStatus(anyString(), any(TxnStatus.class))).thenReturn(1);
        doNothing().when(statusSystemDAO).recordStatusChange(anyString(), any(), any(), anyString(), anyString());

        TransactionDTO result = processingService.updateTransactionStatus(
                "TXN-ABC12345", TxnStatus.COMPLETED, "WEBHOOK");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TxnStatus.COMPLETED);

        verify(transactionDAO).updateStatus("TXN-ABC12345", TxnStatus.COMPLETED);
        verify(statusSystemDAO).recordStatusChange(
                eq("TXN-ABC12345"), eq(TxnStatus.INITIATED), eq(TxnStatus.COMPLETED), eq("WEBHOOK"), anyString());
    }

    @Test
    @DisplayName("Should throw TransactionNotFoundException when txnId not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        when(transactionDAO.findByTxnId("INVALID-TXN")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                processingService.updateTransactionStatus("INVALID-TXN", TxnStatus.COMPLETED, "SYSTEM"))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("INVALID-TXN");
    }

    @Test
    @DisplayName("Should return all pending transactions")
    void shouldReturnPendingTransactions() {
        PaymentTransaction pending = PaymentTransaction.builder()
                .txnId("TXN-PEND01")
                .status(TxnStatus.PENDING)
                .amount(BigDecimal.TEN)
                .currency("INR")
                .build();

        when(transactionDAO.findByStatus(TxnStatus.PENDING)).thenReturn(List.of(pending));

        List<TransactionDTO> result = processingService.getPendingTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TxnStatus.PENDING);
    }
}
