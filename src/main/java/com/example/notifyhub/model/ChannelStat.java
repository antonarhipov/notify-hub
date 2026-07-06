package com.example.notifyhub.model;

/**
 * Aggregated processing counters for a single notification channel.
 *
 * @param channel the channel identifier
 * @param total   total notifications processed for the channel
 * @param sent    notifications successfully sent
 * @param failed  notifications that failed while sending
 * @param blocked notifications blocked because the channel service was disabled
 */
public record ChannelStat(String channel, long total, long sent, long failed, long blocked) {
}
