USE keybridge_ai;

CREATE TABLE IF NOT EXISTS user_wallet (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    balance_usd DECIMAL(18, 6) NOT NULL DEFAULT 0,
    total_recharged_usd DECIMAL(18, 6) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_wallet_user (user_id),
    CONSTRAINT fk_user_wallet_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB COMMENT='User USD wallets';

CREATE TABLE IF NOT EXISTS recharge_order (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    amount_usd DECIMAL(18, 2) NOT NULL,
    pay_amount DECIMAL(18, 2) NOT NULL,
    pay_currency VARCHAR(10) NOT NULL,
    exchange_rate DECIMAL(18, 6) NOT NULL,
    payment_network VARCHAR(30),
    payment_address VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT UNSIGNED,
    reviewed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_recharge_order_no (order_no),
    KEY idx_recharge_user_created (user_id, created_at),
    KEY idx_recharge_status_created (status, created_at),
    CONSTRAINT fk_recharge_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_recharge_reviewer FOREIGN KEY (reviewed_by) REFERENCES sys_user(id)
) ENGINE=InnoDB COMMENT='Wallet recharge orders';
