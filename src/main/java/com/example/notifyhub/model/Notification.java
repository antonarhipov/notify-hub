package com.example.notifyhub.model;

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

    Map<String, Object> payload,

    Map<String, String> rules
) {
    public Notification {
        // Compact constructor for defaults
        if (locale == null || locale.isBlank()) {
            locale = "en";
        }
        if (payload == null) {
            payload = Map.of();
        }
        if (rules == null) {
            rules = Map.of();
        }
    }

    /**
     * Returns a new Notification instance with the specified channel value.
     *
     * @param channel the new channel value
     * @return a new Notification with the updated channel
     */
    public Notification withChannel(String channel) {
        return new Notification(recipient, channel, templateCode, locale, payload, rules);
    }

}
