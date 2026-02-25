package com.example.notifyhub.service;

public class ChannelNotSupportedException extends IllegalArgumentException {
    public ChannelNotSupportedException(String message) {
        super(message);
    }
}
