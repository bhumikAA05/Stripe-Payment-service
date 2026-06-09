package com.stripe.payment.repository;

import com.stripe.payment.model.PaymentStatusSystem;
import com.stripe.payment.model.TxnStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * W5D4 - Coding PaymentStatusSystem
 * Audit trail for all status changes
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentStatusSystemDAO {

    private final JdbcTemplate jdbcTemplate;

    public void recordStatusChange(String txnId, TxnStatus oldStatus, TxnStatus newStatus,
                                   String changedBy, String remarks) {
        String sql = """
            INSERT INTO payment_status_system
                (txn_id, old_status, new_status, changed_by, remarks, changed_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
                txnId,
                oldStatus != null ? oldStatus.name() : null,
                newStatus.name(),
                changedBy,
                remarks,
                Timestamp.valueOf(LocalDateTime.now()));
        log.info("Status change recorded: txnId={} {} -> {}", txnId, oldStatus, newStatus);
    }

    public List<PaymentStatusSystem> findByTxnId(String txnId) {
        String sql = "SELECT * FROM payment_status_system WHERE txn_id = ? ORDER BY changed_at";
        return jdbcTemplate.query(sql, (rs, rowNum) -> PaymentStatusSystem.builder()
                .id(rs.getLong("id"))
                .txnId(rs.getString("txn_id"))
                .oldStatus(rs.getString("old_status") != null
                        ? TxnStatus.valueOf(rs.getString("old_status")) : null)
                .newStatus(TxnStatus.valueOf(rs.getString("new_status")))
                .changedBy(rs.getString("changed_by"))
                .remarks(rs.getString("remarks"))
                .changedAt(rs.getTimestamp("changed_at").toLocalDateTime())
                .build(), txnId);
    }
}
