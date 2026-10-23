package com.example.notifyhub.service;

import com.example.notifyhub.model.Notification;
import com.example.notifyhub.text.MessageDecorations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Email notification sender implementation.
 * Demonstrates IntelliJ IDEA's bean navigation - this is one of multiple NotificationSender implementations.
 * Gutter icon on the class will show it implements NotificationSender interface.
 */
@Service
@Slf4j
public class EmailNotificationSender implements NotificationSender {

    private static final String CHANNEL = "email";

    @Override
    public void send(Notification notification) {
        // Validate that the available sender supports the requested channel
        if (!supports(notification.channel())) {
            throw new ChannelNotSupportedException("No sender available for channel: " + notification.channel());
        }

        log.info("Sending EMAIL notification to: {} with template: {}",
                MessageDecorations.maskEmail(notification.recipient()), notification.templateCode());

        // Simulate email sending
        log.debug("Email details - Locale: {}, Payload: {}",
                notification.locale(), notification.payload());
        log.info("EMAIL notification sent successfully to: {}", MessageDecorations.maskEmail(notification.recipient()));
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
