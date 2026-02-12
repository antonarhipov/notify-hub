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

    @Value("${notifyhub.default-channel:push}")
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
        String notificationId = UUID.randomUUID().toString();

        log.info("Dispatching notification: id={}, recipient={}, template={}",
                notificationId, notification.recipient(), notification.templateCode());

        try {
            // Determine channel - demonstrates @Value field usage
            String channel = notification.channel() != null
                    ? notification.channel()
                    : defaultChannel;

            log.debug("Using channel: {} (default: {}, rate limit: {}, max retries: {})",
                    channel, defaultChannel, rateLimit, maxRetryAttempts);

            log.info("Selected sender: {} for channel: {}", sender.getClass().getSimpleName(), channel);

            // Resolve template
            templateResolver.resolve(notification.templateCode(), notification.locale(), channel)
                    .ifPresent(template ->
                            log.debug("Using template: id={}, body={}", template.getId(),
                                    template.getBodyTemplate().substring(0,
                                            Math.min(50, template.getBodyTemplate().length())))
                    );

            // Send notification
            sender.send(notification);

            // Log success
            NotificationLog logEntry = new NotificationLog(
                    notificationId,
                    notification.recipient(),
                    channel,
                    notification.templateCode(),
                    "SUCCESS"
            );
            logRepository.save(logEntry);

            log.info("Notification dispatched successfully: id={}", notificationId);
            return NotificationResult.success(notificationId);

        } catch (Exception e) {
            log.error("Failed to dispatch notification: id={}, error={}",
                    notificationId, e.getMessage(), e);

            // Log failure
            NotificationLog logEntry = new NotificationLog(
                    notificationId,
                    notification.recipient(),
                    notification.channel() != null ? notification.channel() : defaultChannel,
                    notification.templateCode(),
                    "FAILED"
            );
            logEntry.setErrorMessage(e.getMessage());
            logRepository.save(logEntry);

            return NotificationResult.failure("Failed to send notification: " + e.getMessage());
        }
    }
}
