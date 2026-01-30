package com.jetbrains.notifyhub.service;

import com.jetbrains.notifyhub.model.Notification;
import com.jetbrains.notifyhub.model.NotificationLog;
import com.jetbrains.notifyhub.model.NotificationResult;
import com.jetbrains.notifyhub.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * PRIMARY DEMO FILE for NotifyHub application.
 *
 * Demonstrates THREE key IntelliJ IDEA features:
 *
 * 1. BEAN NAVIGATION:
 *    - Gutter icon on List<NotificationSender> shows all 3 implementations
 *    - Click to navigate to EmailNotificationSender, SmsNotificationSender, PushNotificationSender
 *    - Shows @Primary handling on PushNotificationSender
 *
 * 2. DEBUGGER WITH SPRING INSIGHTS:
 *    - Set breakpoint in dispatch() method
 *    - Run with prod profile: --spring.profiles.active=prod
 *    - In debugger, @Value fields show resolved values:
 *      * defaultChannel = "push" (from application-prod.yml, NOT "email" from base)
 *      * rateLimit = 200 (from application-prod.yml, NOT 50 from base)
 *      * maxRetryAttempts = 3 (from application.yml, no override)
 *    - Can evaluate expressions: this.defaultChannel returns "push"
 *
 * 3. LOMBOK @RequiredArgsConstructor:
 *    - No visible constructor in code
 *    - Gutter icons on fields show dependency injection
 *    - IDE understands Lombok-generated constructor
 */
@Service
//@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    //DEMO Debugger Insights - Set breakpoint at line 70 and inspect these @Value fields in debugger
    //DEMO With prod profile: defaultChannel="push", rateLimit=200 (from application-prod.yml)
    //DEMO With dev profile: defaultChannel="email", rateLimit=50 (from application.yml)
    @Value("${notifyhub.default-channel:push}")
    private String defaultChannel;

    @Value("${notifyhub.rate-limit:50}")
    private int rateLimit;

    @Value("${notifyhub.retry.max-attempts:3}")
    private int maxRetryAttempts;

//    private final List<NotificationSender> senders;
    private final NotificationSender sender;
    private final TemplateResolver templateResolver;
    private final NotificationLogRepository logRepository;

    //DEMO Demonstrates bean navigation - gutter icon shows 3 implementations
    public NotificationDispatcher(/*@Qualifier("pushNotificationSender")*/ NotificationSender sender, TemplateResolver templateResolver, NotificationLogRepository logRepository) {
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

            //DEMO Bean Navigation - Click gutter icon on 'senders' to see all 3 implementations
//            NotificationSender sender = senders.stream()
//                    .filter(s -> s.supports(channel))
//                    .findFirst()
//                    .orElseThrow(() -> new IllegalArgumentException(
//                            "No sender found for channel: " + channel));

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
