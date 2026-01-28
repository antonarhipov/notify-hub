-- Sample notification templates for demonstration
-- These will be loaded on application startup

-- Email templates
INSERT INTO notification_template (channel, template_code, subject_template, body_template, locale, active, created_at, updated_at)
VALUES ('email', 'welcome', 'Welcome to NotifyHub!',
        'Hello {{name}}, welcome to NotifyHub! We are excited to have you on board.',
        'en', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO notification_template (channel, template_code, subject_template, body_template, locale, active, created_at, updated_at)
VALUES ('email', 'welcome', 'Bienvenido a NotifyHub!',
        'Hola {{name}}, bienvenido a NotifyHub! Estamos emocionados de tenerte con nosotros.',
        'es', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO notification_template (channel, template_code, subject_template, body_template, locale, active, created_at, updated_at)
VALUES ('email', 'password-reset', 'Password Reset Request',
        'Hello {{name}}, you requested a password reset. Click here: {{resetLink}}',
        'en', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- SMS templates
INSERT INTO notification_template (channel, template_code, subject_template, body_template, locale, active, created_at, updated_at)
VALUES ('sms', 'welcome', null,
        'Welcome {{name}}! Thanks for joining NotifyHub.',
        'en', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO notification_template (channel, template_code, subject_template, body_template, locale, active, created_at, updated_at)
VALUES ('sms', 'verification-code', null,
        'Your verification code is: {{code}}',
        'en', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Push notification templates
INSERT INTO notification_template (channel, template_code, subject_template, body_template, locale, active, created_at, updated_at)
VALUES ('push', 'welcome', 'Welcome!',
        'Welcome to NotifyHub, {{name}}!',
        'en', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO notification_template (channel, template_code, subject_template, body_template, locale, active, created_at, updated_at)
VALUES ('push', 'new-message', 'New Message',
        'You have a new message from {{sender}}',
        'en', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
