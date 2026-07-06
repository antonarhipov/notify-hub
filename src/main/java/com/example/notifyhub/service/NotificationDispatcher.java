package com.example.notifyhub.service;

import com.example.notifyhub.model.Notification;
import com.example.notifyhub.model.NotificationLog;
import com.example.notifyhub.model.NotificationResult;
import com.example.notifyhub.model.NotificationStatus;
import com.example.notifyhub.repository.NotificationLogRepository;
import com.example.notifyhub.service.transformers.MessageTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationDispatcher {

    @Value("${notifyhub.default-channel:email}")
    private String defaultChannel;

    @Value("${notifyhub.rate-limit:50}")
    private int rateLimit;

    @Value("${notifyhub.retry.max-attempts:3}")
    private int maxRetryAttempts;

    private final ChannelService channelService;
    private final TemplateResolver templateResolver;
    private final NotificationLogRepository logRepository;
    private final Map<String, MessageTransformer> transformersByName;

    public NotificationDispatcher(ChannelService channelService,
                                  TemplateResolver templateResolver,
                                  NotificationLogRepository logRepository,
                                  List<MessageTransformer> transformers) {
        this.channelService = channelService;
        this.templateResolver = templateResolver;
        this.logRepository = logRepository;
        this.transformersByName = transformers.stream()
                .collect(Collectors.toMap(MessageTransformer::getName, Function.identity()));
    }

    /**
     * Dispatch a notification to the appropriate channel.
     * <p>
     * Before sending, applies content-enrichment rules from the notification's
     * {@code rules} map using a Stream-based pipeline:
     * <ol>
     *   <li><b>filter</b> – skip rules whose value is {@code "disabled"}</li>
     *   <li><b>filter</b> – keep only rules whose key starts with {@code "transform."}</li>
     *   <li><b>map</b>   – apply each rule via the matching {@link MessageTransformer} and produce a human-readable summary</li>
     * </ol>
     * The channel is determined solely by the {@code channel} attribute (or the
     * configured default); rules never influence channel selection.
     * <p>
     * The target channel service must be enabled in the mission control dashboard;
     * notifications for a disabled service are recorded as {@link NotificationStatus#BLOCKED}
     * and not delivered. Every outcome is persisted to the audit log so the dashboard
     * can report accurate processing statistics.
     *
     * @param notification the notification to send
     * @return the result of the notification operation
     */
    public NotificationResult dispatch(Notification notification) {
        String messageId = UUID.randomUUID().toString();
        String channel = resolveChannel(notification);
        Notification routed = notification.withChannel(channel);

        log.info("Dispatching notification: id={}, channel={}", messageId, channel);

        var enrichments = routed.rules().entrySet().stream()
                .filter(entry -> !"disabled".equalsIgnoreCase(entry.getValue()))
                .filter(entry -> entry.getKey().startsWith("transform."))
                .map(entry -> applyTransformer(entry, routed))
                .toList();

        log.info("Applied {} enrichment rules for id={}: {}", enrichments.size(), messageId, enrichments);

        if (!channelService.isKnownChannel(channel)) {
            log.warn("No sender available for channel '{}', id={}", channel, messageId);
            recordLog(messageId, routed, NotificationStatus.FAILED,
                    "No sender available for channel: " + channel);
            return NotificationResult.failure("No sender available for channel: " + channel);
        }

        if (!channelService.isEnabled(channel)) {
            log.warn("Channel '{}' service is disabled; blocking notification id={}", channel, messageId);
            recordLog(messageId, routed, NotificationStatus.BLOCKED,
                    "Notification service for channel '" + channel + "' is disabled");
            return NotificationResult.failure(
                    "Notification service for channel '" + channel + "' is disabled");
        }

        try {
            NotificationSender sender = channelService.getSender(channel)
                    .orElseThrow(() -> new ChannelNotSupportedException(
                            "No sender available for channel: " + channel));
            sender.send(routed);

            recordLog(messageId, routed, NotificationStatus.SENT, null);
            return NotificationResult.success(messageId);
        } catch (Exception e) {
            log.error("Failed to dispatch notification: id={}, error={}", messageId, e.getMessage(), e);
            recordLog(messageId, routed, NotificationStatus.FAILED, e.getMessage());
            return NotificationResult.failure("Failed to send notification: " + e.getMessage());
        }
    }

    private String resolveChannel(Notification notification) {
        String channel = notification.channel();
        if (channel == null || channel.isBlank()) {
            channel = defaultChannel;
        }
        return channel.toLowerCase();
    }

    private void recordLog(String messageId, Notification notification,
                           NotificationStatus status, String errorMessage) {
        try {
            NotificationLog entry = new NotificationLog(messageId, notification, status.name());
            if (errorMessage != null) {
                entry.setErrorMessage(errorMessage);
            }
            logRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to persist notification log for id={}: {}", messageId, e.getMessage(), e);
        }
    }

    private String applyTransformer(Map.Entry<String, String> rule, Notification notification) {
        String transformName = rule.getKey().substring("transform.".length());
        MessageTransformer transformer = transformersByName.get(transformName);
        if (transformer != null) {
            return transformer.apply(rule.getValue(), notification);
        }
        log.warn("No transformer found for '{}', skipping", transformName);
        return "Unknown transform [" + transformName + "]: " + rule.getValue();
    }
}
