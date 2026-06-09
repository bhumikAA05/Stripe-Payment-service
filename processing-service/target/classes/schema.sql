-- W5D2 - PaymentDB Schema
-- W5D5 - Save Created Txn - Spring JDBC - Enums

CREATE TABLE IF NOT EXISTS payment_transaction (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    txn_id         VARCHAR(50)    UNIQUE NOT NULL,
    customer_name  VARCHAR(100)   NOT NULL,
    customer_email VARCHAR(150)   NOT NULL,
    amount         DECIMAL(12,2)  NOT NULL,
    currency       VARCHAR(3)     DEFAULT 'INR',
    status         VARCHAR(20)    NOT NULL DEFAULT 'INITIATED',
    session_id     VARCHAR(300),
    payment_method VARCHAR(50),
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- W5D4 - PaymentStatusSystem audit trail
CREATE TABLE IF NOT EXISTS payment_status_system (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    txn_id      VARCHAR(50)  NOT NULL,
    old_status  VARCHAR(20),
    new_status  VARCHAR(20)  NOT NULL,
    changed_by  VARCHAR(100),
    remarks     VARCHAR(500),
    changed_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
