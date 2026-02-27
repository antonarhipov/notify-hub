package com.example.notifyhub.service.transformers;

import com.example.notifyhub.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Transforms the notification message according to the requested writing style.
 * Supported styles include {@code formal}, {@code casual}, and {@code urgent}.
 * Rule example: {@code "transform.style": "formal"}
 */
@Component
@Slf4j
public class StyleTransformer implements MessageTransformer {

    @Override
    public String getName() {
        return "style";
    }

    @Override
    public String apply(String value, Notification notification) {
        String description = switch (value.toLowerCase()) {
            case "formal"  -> "Rewritten in formal business tone";
            case "casual"  -> "Rewritten in casual friendly tone";
            case "urgent"  -> "Rewritten with urgent emphasis";
            default        -> "Applied custom style: " + value;
        };
        log.debug("Applying style '{}' for {}: {}", value, notification.recipient(), description);
        return description;
    }
}
