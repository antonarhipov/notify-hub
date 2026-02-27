package com.example.notifyhub.service;

import com.example.notifyhub.model.Notification;
import com.example.notifyhub.model.NotificationLog;
import com.example.notifyhub.model.NotificationResult;
import com.example.notifyhub.repository.NotificationLogRepository;
import com.example.notifyhub.service.transformers.MessageTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final NotificationSender sender;
    private final TemplateResolver templateResolver;
    private final NotificationLogRepository logRepository;
    private final Map<String, MessageTransformer> transformersByName;

    public NotificationDispatcher(@Qualifier("emailNotificationSender") NotificationSender sender,
                                  TemplateResolver templateResolver,
                                  NotificationLogRepository logRepository,
                                  List<MessageTransformer> transformers) {
        this.sender = sender;
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
     *
     * @param notification the notification to send
     * @return the result of the notification operation
     */
    public NotificationResult dispatch(Notification notification) {
        String messageId = UUID.randomUUID().toString();

        log.info("Dispatching notification: id={}", messageId);

        try {
            var enrichments = notification.rules().entrySet().stream()
                    .filter(entry -> !"disabled".equalsIgnoreCase(entry.getValue()))
                    .filter(entry -> entry.getKey().startsWith("transform."))
                    .map(entry -> applyTransformer(entry, notification))
                    .toList();

            log.info("Applied {} enrichment rules for id={}: {}", enrichments.size(), messageId, enrichments);

            sender.send(notification.withChannel(defaultChannel));

            return NotificationResult.success(messageId);
        } catch (Exception e) {
            log.error("Failed to dispatch notification: id={}, error={}", messageId, e.getMessage(), e);

            return NotificationResult.failure("Failed to send notification: " + e.getMessage());
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
