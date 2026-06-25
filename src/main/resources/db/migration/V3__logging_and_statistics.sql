USE keybridge_ai;

ALTER TABLE request_log
    ADD COLUMN provider_code VARCHAR(50) NOT NULL DEFAULT 'unknown' AFTER provider_id,
    ADD COLUMN model_name VARCHAR(150) NOT NULL DEFAULT 'unknown' AFTER provider_code,
    DROP INDEX idx_request_log_model_time,
    ADD KEY idx_request_log_model_time (model_name, created_at),
    ADD KEY idx_request_log_provider_time (provider_code, created_at);

ALTER TABLE usage_daily
    DROP INDEX uk_usage_daily_dimension,
    MODIFY COLUMN model_id BIGINT UNSIGNED NULL,
    ADD COLUMN provider_id BIGINT UNSIGNED NULL AFTER model_id,
    ADD COLUMN provider_code VARCHAR(50) NOT NULL DEFAULT 'unknown' AFTER provider_id,
    ADD COLUMN model_name VARCHAR(150) NOT NULL DEFAULT 'unknown' AFTER provider_code,
    ADD COLUMN failure_count BIGINT NOT NULL DEFAULT 0 AFTER success_count,
    ADD COLUMN total_tokens BIGINT NOT NULL DEFAULT 0 AFTER output_tokens,
    ADD COLUMN total_duration_ms BIGINT NOT NULL DEFAULT 0 AFTER total_tokens,
    ADD UNIQUE KEY uk_usage_daily_dimension (usage_date, user_id, provider_code, model_name),
    ADD KEY idx_usage_daily_provider_date (provider_code, usage_date),
    ADD KEY idx_usage_daily_model_date (model_name, usage_date);
