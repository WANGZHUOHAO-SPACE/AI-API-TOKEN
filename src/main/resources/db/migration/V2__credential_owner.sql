USE keybridge_ai;

ALTER TABLE provider_credential
    ADD COLUMN user_id BIGINT UNSIGNED NULL AFTER id;

-- Existing credentials are assigned to the earliest administrator, or the earliest user.
SET @default_owner_id = COALESCE(
    (SELECT id FROM sys_user WHERE role = 'ADMIN' ORDER BY id LIMIT 1),
    (SELECT id FROM sys_user ORDER BY id LIMIT 1)
);

UPDATE provider_credential
SET user_id = @default_owner_id
WHERE user_id IS NULL;

ALTER TABLE provider_credential
    MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL,
    ADD KEY idx_credential_user_status (user_id, status),
    ADD CONSTRAINT fk_credential_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
