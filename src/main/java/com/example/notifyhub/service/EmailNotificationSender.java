package com.example.notifyhub.service;

import com.example.notifyhub.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Email notification sender implementation.
 * Demonstrates IntelliJ IDEA's bean navigation - this is one of multiple NotificationSender implementations.
 * Gutter icon on the class will show it implements NotificationSender interface.
 */
//DEMO Bean Navigation - Gutter icon on class shows it implements NotificationSender
//DEMO Click gutter icon to navigate to interface and see all 3 implementations
@Service
@Slf4j
public class EmailNotificationSender implements NotificationSender {

    private static final String CHANNEL = "email";

    @Override
    public void send(Notification notification) {
        log.info("Sending EMAIL notification to: {} with template: {}",
                notification.recipient(), notification.templateCode());

        // Simulate email sending
        log.debug("Email details - Locale: {}, Payload: {}",
                notification.locale(), notification.payload());
        log.info("EMAIL notification sent successfully to: {}", notification.recipient());
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
