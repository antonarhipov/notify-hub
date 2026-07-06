package com.example.notifyhub.model;

/**
 * Represents the current on/off state of a single notification channel service,
 * as shown in the dashboard's service control widget.
 *
 * @param channel the channel identifier (e.g. {@code email}, {@code sms}, {@code push})
 * @param enabled whether the service is currently accepting notifications
 */
public record ServiceStatus(String channel, boolean enabled) {
}
