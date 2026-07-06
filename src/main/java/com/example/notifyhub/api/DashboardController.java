package com.example.notifyhub.api;

import com.example.notifyhub.model.DashboardStats;
import com.example.notifyhub.model.ServiceStatus;
import com.example.notifyhub.service.ChannelNotSupportedException;
import com.example.notifyhub.service.ChannelService;
import com.example.notifyhub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API backing the mission control dashboard.
 * <p>
 * Exposes processed-notification statistics and the controls to enable or
 * disable individual notification channel services at runtime.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final ChannelService channelService;

    /**
     * Aggregated statistics for processed notifications.
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> stats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /**
     * Current on/off state of every notification channel service.
     * GET /api/dashboard/services
     */
    @GetMapping("/services")
    public ResponseEntity<List<ServiceStatus>> services() {
        return ResponseEntity.ok(channelService.getServices());
    }

    /**
     * Enable the service for a channel.
     * POST /api/dashboard/services/{channel}/enable
     */
    @PostMapping("/services/{channel}/enable")
    public ResponseEntity<ServiceStatus> enable(@PathVariable String channel) {
        return ResponseEntity.ok(channelService.setEnabled(channel, true));
    }

    /**
     * Disable the service for a channel.
     * POST /api/dashboard/services/{channel}/disable
     */
    @PostMapping("/services/{channel}/disable")
    public ResponseEntity<ServiceStatus> disable(@PathVariable String channel) {
        return ResponseEntity.ok(channelService.setEnabled(channel, false));
    }

    /**
     * Set a channel service to an explicit state.
     * PUT /api/dashboard/services/{channel}
     * Body: {"enabled": true|false}
     */
    @PutMapping("/services/{channel}")
    public ResponseEntity<ServiceStatus> setState(@PathVariable String channel,
                                                  @RequestBody ServiceToggleRequest request) {
        return ResponseEntity.ok(channelService.setEnabled(channel, request.enabled()));
    }

    @ExceptionHandler(ChannelNotSupportedException.class)
    public ResponseEntity<String> handleUnknownChannel(ChannelNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    /**
     * Request body for explicitly setting a channel service state.
     */
    public record ServiceToggleRequest(boolean enabled) {
    }
}
