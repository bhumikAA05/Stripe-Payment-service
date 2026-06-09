package com.stripe.payment.repository;

import com.stripe.payment.model.PaymentStatusSystem;
import com.stripe.payment.model.PaymentTransaction;
import com.stripe.payment.model.TxnStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

/**
 * W6D1 - Coding Update Status & Pending Status - Transaction DAO Logic
 * W5D5 - Spring JDBC
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PaymentTransaction> txnRowMapper = (rs, rowNum) -> PaymentTransaction.builder()
            .id(rs.getLong("id"))
            .txnId(rs.getString("txn_id"))
            .customerName(rs.getString("customer_name"))
            .customerEmail(rs.getString("customer_email"))
            .amount(rs.getBigDecimal("amount"))
            .currency(rs.getString("currency"))
            .status(TxnStatus.valueOf(rs.getString("status")))
            .sessionId(rs.getString("session_id"))
            .paymentMethod(rs.getString("payment_method"))
            .createdAt(rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null
                    ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
            .build();

    public PaymentTransaction save(PaymentTransaction txn) {
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String sql = """
            INSERT INTO payment_transaction
                (txn_id, customer_name, customer_email, amount, currency, status,
                 session_id, payment_method, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, txnId);
            ps.setString(2, txn.getCustomerName());
            ps.setString(3, txn.getCustomerEmail());
            ps.setBigDecimal(4, txn.getAmount());
            ps.setString(5, txn.getCurrency());
            ps.setString(6, TxnStatus.INITIATED.name());
            ps.setString(7, txn.getSessionId());
            ps.setString(8, txn.getPaymentMethod());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        Long id = ((Number) keys.get("ID")).longValue();
        txn.setTxnId(txnId);
        txn.setStatus(TxnStatus.INITIATED);
        log.info("Transaction saved: txnId={}", txnId);
        return txn;
    }

    public Optional<PaymentTransaction> findByTxnId(String txnId) {
        String sql = "SELECT * FROM payment_transaction WHERE txn_id = ?";
        List<PaymentTransaction> results = jdbcTemplate.query(sql, txnRowMapper, txnId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<PaymentTransaction> findAll() {
        return jdbcTemplate.query("SELECT * FROM payment_transaction ORDER BY created_at DESC", txnRowMapper);
    }

    public List<PaymentTransaction> findByStatus(TxnStatus status) {
        return jdbcTemplate.query(
                "SELECT * FROM payment_transaction WHERE status = ? ORDER BY created_at DESC",
                txnRowMapper, status.name());
    }

    public int updateStatus(String txnId, TxnStatus newStatus) {
        String sql = "UPDATE payment_transaction SET status = ?, updated_at = ? WHERE txn_id = ?";
        int updated = jdbcTemplate.update(sql, newStatus.name(), Timestamp.valueOf(LocalDateTime.now()), txnId);
        log.info("Transaction {} status updated to {}", txnId, newStatus);
        return updated;
    }

    public int updateSessionId(String txnId, String sessionId) {
        String sql = "UPDATE payment_transaction SET session_id = ?, updated_at = ? WHERE txn_id = ?";
        return jdbcTemplate.update(sql, sessionId, Timestamp.valueOf(LocalDateTime.now()), txnId);
    }
}
