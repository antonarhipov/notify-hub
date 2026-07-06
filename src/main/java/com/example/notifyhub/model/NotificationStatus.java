package com.example.notifyhub.model;

/**
 * Lifecycle status of a processed notification, as recorded in the audit log
 * and surfaced on the mission control dashboard.
 */
public enum NotificationStatus {

    /** The notification was successfully handed off to its channel sender. */
    SENT,

    /** The channel sender threw an error while processing the notification. */
    FAILED,

    /** The target channel service was disabled, so the notification was not sent. */
    BLOCKED
}
