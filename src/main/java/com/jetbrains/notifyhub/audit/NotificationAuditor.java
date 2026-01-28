package com.jetbrains.notifyhub.audit;

import com.jetbrains.notifyhub.service.NotificationSender;

/**
 * Simple auditor component for demonstration purposes.
 * Used to demonstrate ambiguous bean injection in NotificationConfig.
 */
public class NotificationAuditor {

    private final NotificationSender sender;

    public NotificationAuditor(NotificationSender sender) {
        this.sender = sender;
    }

    public void audit(String message) {
        // Audit implementation
        System.out.println("Auditing with channel: " + sender.getChannel() + " - " + message);
    }

    public NotificationSender getSender() {
        return sender;
    }
}
