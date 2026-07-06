package com.example.notifyhub.model;

import java.util.List;

/**
 * Aggregated statistics for processed notifications, rendered on the mission
 * control dashboard's main screen.
 *
 * @param totalProcessed total number of notifications processed
 * @param sent           number successfully sent
 * @param failed         number that failed while sending
 * @param blocked        number blocked because the target service was disabled
 * @param successRate    percentage of processed notifications that were sent (0-100)
 * @param byChannel      per-channel breakdown of processing counters
 * @param recent         the most recently processed notifications
 */
public record DashboardStats(
        long totalProcessed,
        long sent,
        long failed,
        long blocked,
        double successRate,
        List<ChannelStat> byChannel,
        List<RecentNotification> recent
) {
}
