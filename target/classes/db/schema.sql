CREATE DATABASE IF NOT EXISTS keybridge_ai
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE keybridge_ai;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled, 1-enabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB COMMENT='Platform users';

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

CREATE TABLE IF NOT EXISTS ai_provider (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    protocol_type VARCHAR(30) NOT NULL DEFAULT 'OPENAI_COMPATIBLE',
    status TINYINT NOT NULL DEFAULT 1,
    timeout_seconds INT NOT NULL DEFAULT 60,
    description VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ai_provider_code (code)
) ENGINE=InnoDB COMMENT='AI providers';

CREATE TABLE IF NOT EXISTS provider_credential (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    provider_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(100) NOT NULL,
    encrypted_key TEXT NOT NULL,
    key_suffix VARCHAR(8) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    weight INT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    failure_count INT NOT NULL DEFAULT 0,
    last_checked_at DATETIME,
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_credential_user_status (user_id, status),
    KEY idx_credential_provider_status (provider_id, status),
    CONSTRAINT fk_credential_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_credential_provider FOREIGN KEY (provider_id) REFERENCES ai_provider(id)
) ENGINE=InnoDB COMMENT='Encrypted upstream API credentials';

CREATE TABLE IF NOT EXISTS ai_model (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    model_code VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(30) NOT NULL DEFAULT 'CHAT',
    input_price DECIMAL(14, 6) NOT NULL DEFAULT 0 COMMENT 'Price per one million tokens',
    output_price DECIMAL(14, 6) NOT NULL DEFAULT 0 COMMENT 'Price per one million tokens',
    max_tokens INT NOT NULL DEFAULT 4096,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ai_model_code (model_code)
) ENGINE=InnoDB COMMENT='Public platform models';

CREATE TABLE IF NOT EXISTS model_price_override (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    model_code VARCHAR(150) NOT NULL,
    input_price VARCHAR(120),
    cached_price VARCHAR(120),
    output_price VARCHAR(120),
    extra_price VARCHAR(500),
    updated_by BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_model_price_override_code (model_code),
    KEY idx_model_price_override_updated_by (updated_by),
    CONSTRAINT fk_model_price_override_user FOREIGN KEY (updated_by) REFERENCES sys_user(id)
) ENGINE=InnoDB COMMENT='Administrator model price overrides';

CREATE TABLE IF NOT EXISTS model_channel (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT UNSIGNED NOT NULL,
    provider_id BIGINT UNSIGNED NOT NULL,
    credential_id BIGINT UNSIGNED NOT NULL,
    upstream_model VARCHAR(150) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    weight INT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_channel_model_status (model_id, status, priority),
    CONSTRAINT fk_channel_model FOREIGN KEY (model_id) REFERENCES ai_model(id),
    CONSTRAINT fk_channel_provider FOREIGN KEY (provider_id) REFERENCES ai_provider(id),
    CONSTRAINT fk_channel_credential FOREIGN KEY (credential_id) REFERENCES provider_credential(id)
) ENGINE=InnoDB COMMENT='Model to upstream routing channels';

CREATE TABLE IF NOT EXISTS access_token (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(100) NOT NULL,
    token_prefix VARCHAR(20) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    expires_at DATETIME,
    request_limit BIGINT,
    token_limit BIGINT,
    used_tokens BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_access_token_hash (token_hash),
    KEY idx_access_token_user_status (user_id, status),
    CONSTRAINT fk_access_token_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB COMMENT='Hashed platform access tokens';

CREATE TABLE IF NOT EXISTS token_model_permission (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    access_token_id BIGINT UNSIGNED NOT NULL,
    model_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_token_model (access_token_id, model_id),
    CONSTRAINT fk_permission_token FOREIGN KEY (access_token_id) REFERENCES access_token(id) ON DELETE CASCADE,
    CONSTRAINT fk_permission_model FOREIGN KEY (model_id) REFERENCES ai_model(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Models allowed for each platform token';

CREATE TABLE IF NOT EXISTS request_log (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    request_id VARCHAR(64) NOT NULL,
    user_id BIGINT UNSIGNED,
    access_token_id BIGINT UNSIGNED,
    model_id BIGINT UNSIGNED,
    provider_id BIGINT UNSIGNED,
    provider_code VARCHAR(50) NOT NULL,
    model_name VARCHAR(150) NOT NULL,
    credential_id BIGINT UNSIGNED,
    input_tokens INT NOT NULL DEFAULT 0,
    output_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    cost DECIMAL(16, 8) NOT NULL DEFAULT 0,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    status_code INT NOT NULL,
    success TINYINT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_request_log_request_id (request_id),
    KEY idx_request_log_user_time (user_id, created_at),
    KEY idx_request_log_model_time (model_name, created_at),
    KEY idx_request_log_provider_time (provider_code, created_at)
) ENGINE=InnoDB COMMENT='AI forwarding request logs';

CREATE TABLE IF NOT EXISTS usage_daily (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    usage_date DATE NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    model_id BIGINT UNSIGNED,
    provider_id BIGINT UNSIGNED,
    provider_code VARCHAR(50) NOT NULL,
    model_name VARCHAR(150) NOT NULL,
    request_count BIGINT NOT NULL DEFAULT 0,
    success_count BIGINT NOT NULL DEFAULT 0,
    failure_count BIGINT NOT NULL DEFAULT 0,
    input_tokens BIGINT NOT NULL DEFAULT 0,
    output_tokens BIGINT NOT NULL DEFAULT 0,
    total_tokens BIGINT NOT NULL DEFAULT 0,
    total_duration_ms BIGINT NOT NULL DEFAULT 0,
    total_cost DECIMAL(18, 8) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_usage_daily_dimension (usage_date, user_id, provider_code, model_name),
    KEY idx_usage_daily_provider_date (provider_code, usage_date),
    KEY idx_usage_daily_model_date (model_name, usage_date)
) ENGINE=InnoDB COMMENT='Daily usage aggregation';

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(64),
    detail VARCHAR(2000),
    ip_address VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_log_user_time (user_id, created_at)
) ENGINE=InnoDB COMMENT='Administrative audit logs';
