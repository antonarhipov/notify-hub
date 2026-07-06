package com.example.notifyhub.model;

import java.time.LocalDateTime;

/**
 * Compact view of a recently processed notification for the dashboard activity feed.
 *
 * @param notificationId the generated notification identifier
 * @param recipient      the recipient address
 * @param channel        the channel the notification was processed on
 * @param templateCode   the template used
 * @param status         the processing status
 * @param sentAt         when the notification was processed
 */
public record RecentNotification(
        String notificationId,
        String recipient,
        String channel,
        String templateCode,
        String status,
        LocalDateTime sentAt
) {
    public static RecentNotification from(NotificationLog log) {
        return new RecentNotification(
                log.getNotificationId(),
                log.getRecipient(),
                log.getChannel(),
                log.getTemplateCode(),
                log.getStatus(),
                log.getSentAt()
        );
    }
}
