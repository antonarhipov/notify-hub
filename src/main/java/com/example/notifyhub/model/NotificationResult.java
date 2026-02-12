package com.example.notifyhub.model;

/**
 * Response DTO for notification sending operations.
 * Demonstrates IntelliJ IDEA's record support.
 */
public record NotificationResult(
    boolean success,
    String message,
    String notificationId
) {
    public static NotificationResult success(String notificationId) {
        return new NotificationResult(true, "Notification sent successfully", notificationId);
    }

    public static NotificationResult failure(String message) {
        return new NotificationResult(false, message, null);
    }
}
