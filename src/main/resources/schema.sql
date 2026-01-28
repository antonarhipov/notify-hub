-- NotificationTemplate table for storing notification templates
CREATE TABLE IF NOT EXISTS notification_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel VARCHAR(50) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    subject_template VARCHAR(500),
    body_template TEXT NOT NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_template_channel_code_locale UNIQUE (channel, template_code, locale)
);

-- NotificationLog table for audit trail
CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id VARCHAR(100) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    template_code VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indices for efficient querying
CREATE INDEX IF NOT EXISTS idx_notification_log_recipient ON notification_log(recipient);
CREATE INDEX IF NOT EXISTS idx_notification_log_status_sent_at ON notification_log(status, sent_at);
CREATE INDEX IF NOT EXISTS idx_notification_log_notification_id ON notification_log(notification_id);
