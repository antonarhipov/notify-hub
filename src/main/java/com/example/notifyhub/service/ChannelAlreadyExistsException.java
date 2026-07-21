package com.example.notifyhub.service;

/**
 * Raised when attempting to register a notification channel that the hub
 * already knows about.
 */
public class ChannelAlreadyExistsException extends IllegalStateException {
    public ChannelAlreadyExistsException(String message) {
        super(message);
    }
}
