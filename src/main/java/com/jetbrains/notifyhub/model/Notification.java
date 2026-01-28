package com.jetbrains.notifyhub.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request DTO for sending a notification.
 * Demonstrates IntelliJ IDEA's record support and validation integration.
 */
public record Notification(
    @NotBlank(message = "Recipient is required")
    String recipient,

    String channel,

    @NotBlank(message = "Template code is required")
    String templateCode,

    @NotNull
    String locale,

    Map<String, Object> payload
) {
    public Notification {
        // Compact constructor for defaults
        if (locale == null || locale.isBlank()) {
            locale = "en";
        }
        if (payload == null) {
            payload = Map.of();
        }
    }
}
