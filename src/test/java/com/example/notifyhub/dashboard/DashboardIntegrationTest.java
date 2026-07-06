package com.example.notifyhub.dashboard;

import com.example.notifyhub.IntegrationTestBase;
import com.example.notifyhub.model.DashboardStats;
import com.example.notifyhub.model.Notification;
import com.example.notifyhub.model.NotificationResult;
import com.example.notifyhub.model.NotificationStatus;
import com.example.notifyhub.repository.NotificationLogRepository;
import com.example.notifyhub.service.ChannelService;
import com.example.notifyhub.service.DashboardService;
import com.example.notifyhub.service.NotificationDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the mission control dashboard backend: verifies that the
 * dispatcher persists processing outcomes, that disabling a service blocks
 * notifications, and that the {@link DashboardService} aggregates the stats.
 */
class DashboardIntegrationTest extends IntegrationTestBase {

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private NotificationLogRepository logRepository;

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        // Reset every channel to enabled so tests are independent of one another.
        channelService.getChannels().forEach(channel -> channelService.setEnabled(channel, true));
    }

    private Notification notification(String recipient, String channel) {
        return new Notification(recipient, channel, "welcome", "en", Map.of(), Map.of());
    }

    @Test
    void dispatch_whenServiceEnabled_recordsSentAndCountsStats() {
        NotificationResult result = dispatcher.dispatch(notification("a@example.com", "email"));

        assertThat(result.success()).isTrue();

        DashboardStats stats = dashboardService.getStats();
        assertThat(stats.totalProcessed()).isEqualTo(1);
        assertThat(stats.sent()).isEqualTo(1);
        assertThat(stats.failed()).isZero();
        assertThat(stats.blocked()).isZero();
        assertThat(stats.successRate()).isEqualTo(100.0);
        assertThat(stats.recent()).hasSize(1);
        assertThat(stats.recent().get(0).status()).isEqualTo(NotificationStatus.SENT.name());
    }

    @Test
    void dispatch_whenServiceDisabled_blocksAndRecordsBlocked() {
        channelService.setEnabled("email", false);

        NotificationResult result = dispatcher.dispatch(notification("b@example.com", "email"));

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("disabled");

        DashboardStats stats = dashboardService.getStats();
        assertThat(stats.totalProcessed()).isEqualTo(1);
        assertThat(stats.sent()).isZero();
        assertThat(stats.blocked()).isEqualTo(1);
        assertThat(stats.successRate()).isZero();
    }

    @Test
    void dispatch_buildsPerChannelBreakdown() {
        dispatcher.dispatch(notification("c@example.com", "email"));
        dispatcher.dispatch(notification("d@example.com", "sms"));
        channelService.setEnabled("push", false);
        dispatcher.dispatch(notification("e@example.com", "push"));

        DashboardStats stats = dashboardService.getStats();

        assertThat(stats.totalProcessed()).isEqualTo(3);
        assertThat(stats.byChannel()).extracting("channel")
                .contains("email", "sms", "push");

        var pushStat = stats.byChannel().stream()
                .filter(c -> c.channel().equals("push"))
                .findFirst()
                .orElseThrow();
        assertThat(pushStat.blocked()).isEqualTo(1);
        assertThat(pushStat.sent()).isZero();
    }

    @Test
    void channelService_exposesAllSenderChannels() {
        assertThat(channelService.getChannels())
                .contains("email", "sms", "push");
        assertThat(channelService.isEnabled("email")).isTrue();

        channelService.setEnabled("sms", false);
        assertThat(channelService.isEnabled("sms")).isFalse();
    }
}
