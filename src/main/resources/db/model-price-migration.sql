USE keybridge_ai;

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
