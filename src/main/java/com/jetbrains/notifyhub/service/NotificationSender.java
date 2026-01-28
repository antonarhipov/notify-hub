package com.jetbrains.notifyhub.service;

import com.jetbrains.notifyhub.model.Notification;

/**
 * Interface for notification sending implementations.
 * Demonstrates IntelliJ IDEA's bean navigation with multiple implementations.
 * Gutter icons will show all implementing classes.
 */
//DEMO Bean Navigation - Click gutter icon on interface name to see 3 implementations
//DEMO Implementations: EmailNotificationSender, SmsNotificationSender, PushNotificationSender
public interface NotificationSender {

    /**
     * Send a notification through this channel.
     *
     * @param notification the notification to send
     */
    void send(Notification notification);

    /**
     * Get the channel identifier for this sender.
     *
     * @return the channel name (e.g., "email", "sms", "push")
     */
    String getChannel();

    /**
     * Check if this sender supports the given channel.
     *
     * @param channel the channel to check
     * @return true if this sender handles the channel
     */
    default boolean supports(String channel) {
        return getChannel().equalsIgnoreCase(channel);
    }
}
