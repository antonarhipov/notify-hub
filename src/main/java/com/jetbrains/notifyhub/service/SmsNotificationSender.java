package com.jetbrains.notifyhub.service;

import com.jetbrains.notifyhub.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SMS notification sender implementation.
 * Demonstrates IntelliJ IDEA's bean navigation - this is one of multiple NotificationSender implementations.
 * Gutter icon on the class will show it implements NotificationSender interface.
 */
@Service
@Slf4j
public class SmsNotificationSender implements NotificationSender {

    private static final String CHANNEL = "sms";

    @Override
    public void send(Notification notification) {
        log.info("Sending SMS notification to: {} with template: {}",
                notification.recipient(), notification.templateCode());

        // Simulate SMS sending
        log.debug("SMS details - Locale: {}, Payload: {}",
                notification.locale(), notification.payload());
        log.info("SMS notification sent successfully to: {}", notification.recipient());
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
