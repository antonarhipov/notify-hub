package com.example.notifyhub.service;

import com.example.notifyhub.model.ChannelStat;
import com.example.notifyhub.model.DashboardStats;
import com.example.notifyhub.model.NotificationStatus;
import com.example.notifyhub.model.RecentNotification;
import com.example.notifyhub.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates audit-log data into the statistics shown on the mission control
 * dashboard's main screen.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final NotificationLogRepository logRepository;
    private final ChannelService channelService;

    /**
     * Build the full dashboard statistics snapshot: overall totals, success rate,
     * a per-channel breakdown (including channels that have no activity yet) and
     * the most recent activity.
     */
    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        Map<String, Long> totalsByStatus = toStatusCounts(logRepository.countGroupByStatus());

        long sent = totalsByStatus.getOrDefault(NotificationStatus.SENT.name(), 0L);
        long failed = totalsByStatus.getOrDefault(NotificationStatus.FAILED.name(), 0L);
        long blocked = totalsByStatus.getOrDefault(NotificationStatus.BLOCKED.name(), 0L);
        long total = sent + failed + blocked;

        double successRate = total == 0 ? 0.0 : Math.round((sent * 10000.0) / total) / 100.0;

        List<ChannelStat> byChannel = buildChannelStats();

        List<RecentNotification> recent = logRepository.findTop10ByOrderBySentAtDescIdDesc().stream()
                .map(RecentNotification::from)
                .toList();

        return new DashboardStats(total, sent, failed, blocked, successRate, byChannel, recent);
    }

    private List<ChannelStat> buildChannelStats() {
        // Seed every known channel so the dashboard always lists all services,
        // even those that have not processed anything yet.
        Map<String, long[]> counters = new LinkedHashMap<>();
        for (String channel : channelService.getChannels()) {
            counters.put(channel, new long[3]); // [sent, failed, blocked]
        }

        for (Object[] row : logRepository.countGroupByChannelAndStatus()) {
            String channel = String.valueOf(row[0]).toLowerCase();
            String status = String.valueOf(row[1]);
            long count = ((Number) row[2]).longValue();
            long[] counter = counters.computeIfAbsent(channel, c -> new long[3]);
            int index = statusIndex(status);
            if (index >= 0) {
                counter[index] += count;
            }
        }

        List<ChannelStat> stats = new ArrayList<>();
        counters.forEach((channel, c) -> {
            long total = c[0] + c[1] + c[2];
            stats.add(new ChannelStat(channel, total, c[0], c[1], c[2]));
        });
        stats.sort((a, b) -> a.channel().compareTo(b.channel()));
        return stats;
    }

    private static int statusIndex(String status) {
        if (NotificationStatus.SENT.name().equals(status)) {
            return 0;
        }
        if (NotificationStatus.FAILED.name().equals(status)) {
            return 1;
        }
        if (NotificationStatus.BLOCKED.name().equals(status)) {
            return 2;
        }
        return -1;
    }

    private static Map<String, Long> toStatusCounts(List<Object[]> rows) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : rows) {
            counts.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return counts;
    }
}
