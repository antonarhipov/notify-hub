package com.example.notifyhub.service.transformers;

import com.example.notifyhub.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Prepends a header text to the notification message.
 * Rule example: {@code "transform.header": "Important Update"}
 */
@Component
@Slf4j
public class HeaderTransformer implements MessageTransformer {

    @Override
    public String getName() {
        return "header";
    }

    @Override
    public String apply(String value, Notification notification) {
        log.debug("Prepending header '{}' for {}", value, notification.recipient());
        return "Prepended header: " + value;
    }
}
