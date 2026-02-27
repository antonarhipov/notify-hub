package com.example.notifyhub.service.transformers;

import com.example.notifyhub.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Appends a footer text to the notification message.
 * Rule example: {@code "transform.footer": "Sent via NotifyHub"}
 */
@Component
@Slf4j
public class FooterTransformer implements MessageTransformer {

    @Override
    public String getName() {
        return "footer";
    }

    @Override
    public String apply(String value, Notification notification) {
        log.debug("Appending footer '{}' for {}", value, notification.recipient());
        return "Appended footer: " + value;
    }
}
