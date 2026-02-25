package com.example.notifyhub.service;

import com.example.notifyhub.model.Notification;
import com.example.notifyhub.model.NotificationLog;
import com.example.notifyhub.model.NotificationResult;
import com.example.notifyhub.repository.NotificationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificationDispatcher {

    @Value("${notifyhub.default-channel:email}")
    private String defaultChannel;

    @Value("${notifyhub.rate-limit:50}")
    private int rateLimit;

    @Value("${notifyhub.retry.max-attempts:3}")
    private int maxRetryAttempts;

    private final NotificationSender sender;
    private final TemplateResolver templateResolver;
    private final NotificationLogRepository logRepository;

    public NotificationDispatcher(@Qualifier("emailNotificationSender") NotificationSender sender, TemplateResolver templateResolver, NotificationLogRepository logRepository) {
        this.sender = sender;
        this.templateResolver = templateResolver;
        this.logRepository = logRepository;
    }

    /**
     * Dispatch a notification to the appropriate channel.
     * Set breakpoint here to see @Value field resolution in debugger.
     *
     * @param notification the notification to send
     * @return the result of the notification operation
     */
    public NotificationResult dispatch(Notification notification) {
        String messageId = UUID.randomUUID().toString();

        log.info("Dispatching notification: id={}", messageId);

        try {
            // Send notification
            sender.send(notification.withChannel(defaultChannel));

            // Log success
            NotificationLog logEntry = new NotificationLog(messageId, notification, "SUCCESS");
            logRepository.save(logEntry);

            return NotificationResult.success(messageId);

        } catch (Exception e) {
            log.error("Failed to dispatch notification: id={}, error={}", messageId, e.getMessage(), e);

            return NotificationResult.failure("Failed to send notification: " + e.getMessage());
        }
    }
}
