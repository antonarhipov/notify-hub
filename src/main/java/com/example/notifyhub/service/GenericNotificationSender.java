package com.example.notifyhub.service;

import com.example.notifyhub.model.Notification;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link NotificationSender} for channel types registered at runtime through
 * the mission control dashboard, for which no dedicated bean implementation exists.
 * <p>
 * It performs no real delivery — it records that the notification was routed —
 * so a newly added channel type is immediately usable end to end.
 */
@Slf4j
public class GenericNotificationSender implements NotificationSender {

    private final String channel;

    public GenericNotificationSender(String channel) {
        this.channel = channel;
    }

    @Override
    public void send(Notification notification) {
        if (!supports(notification.channel())) {
            throw new ChannelNotSupportedException("No sender available for channel: " + notification.channel());
        }

        log.info("Sending {} notification to: {} with template: {}",
                channel.toUpperCase(), notification.recipient(), notification.templateCode());

        // Simulate delivery through the dynamically registered channel
        log.debug("{} notification details - Locale: {}, Payload: {}",
                channel, notification.locale(), notification.payload());
        log.info("{} notification sent successfully to: {}", channel.toUpperCase(), notification.recipient());
    }

    @Override
    public String getChannel() {
        return channel;
    }
}
