package com.jetbrains.notifyhub.controller;

import com.jetbrains.notifyhub.model.Notification;
import com.jetbrains.notifyhub.model.NotificationResult;
import com.jetbrains.notifyhub.service.NotificationDispatcher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification operations.
 * Provides the /api/notify endpoint for sending notifications.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationDispatcher dispatcher;

    /**
     * Send a notification.
     * POST /api/notify
     *
     * Example request:
     * {
     *   "recipient": "user@example.com",
     *   "channel": "email",
     *   "templateCode": "welcome",
     *   "locale": "en",
     *   "payload": {}
     * }
     *
     * @param notification the notification request
     * @return the notification result
     */
    @PostMapping("/notify")
    public ResponseEntity<NotificationResult> sendNotification(
            @Valid @RequestBody Notification notification) {

        log.info("Received notification request for recipient: {}", notification.recipient());

        NotificationResult result = dispatcher.dispatch(notification);

        if (result.success()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("NotifyHub is running");
    }
}
