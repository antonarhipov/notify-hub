package com.jetbrains.notifyhub.service;

import com.jetbrains.notifyhub.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Push notification sender implementation.
 * Demonstrates IntelliJ IDEA's @Primary bean handling.
 * This is marked as @Primary, making it the default choice when a single NotificationSender is autowired.
 * IDE will indicate this bean is the primary candidate in bean navigation.
 */
//DEMO @Primary Bean - This bean is the default choice when NotificationSender is injected
//DEMO See NotificationConfig.auditor() - without @Qualifier, this bean would be injected
@Service
//@Primary
@Slf4j
public class PushNotificationSender implements NotificationSender {

    private static final String CHANNEL = "push";

    @Override
    public void send(Notification notification) {
        log.info("Sending PUSH notification to: {} with template: {}",
                notification.recipient(), notification.templateCode());

        // Simulate push notification sending
        log.debug("Push notification details - Locale: {}, Payload: {}",
                notification.locale(), notification.payload());
        log.info("PUSH notification sent successfully to: {}", notification.recipient());
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
