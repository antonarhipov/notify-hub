package com.jetbrains.notifyhub.config;

import com.jetbrains.notifyhub.audit.NotificationAuditor;
import com.jetbrains.notifyhub.service.NotificationSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for notification-related beans.
 *
 * DEMONSTRATES BEAN AMBIGUITY:
 * The auditor() method has an ambiguous parameter - NotificationSender without @Qualifier.
 * IntelliJ IDEA will:
 * - Show a yellow warning on the 'sender' parameter
 * - Display gutter icon showing 3 candidate beans:
 *   * EmailNotificationSender
 *   * SmsNotificationSender
 *   * PushNotificationSender (@Primary - would be selected by default)
 *
 * To resolve, add @Qualifier("emailNotificationSender") to the parameter.
 */
@Configuration
public class NotificationConfig {

    //DEMO Bean Ambiguity - Yellow warning on 'sender' parameter
    //DEMO Click gutter icon to see 3 candidate beans: EmailNotificationSender, SmsNotificationSender, PushNotificationSender
    //DEMO Without @Qualifier, PushNotificationSender is injected (@Primary)
    //DEMO Fix by adding: @Qualifier("emailNotificationSender") to the parameter
    /**
     * Creates a NotificationAuditor bean when audit is enabled.
     * The 'sender' parameter demonstrates ambiguous bean injection.
     *
     * IntelliJ will warn about multiple candidates and show:
     * - 3 matching beans in the gutter icon
     * - Yellow squiggle on 'sender' parameter
     * - @Primary bean (PushNotificationSender) would be injected by default
     *
     * Fix by adding: @Qualifier("emailNotificationSender")
     */
    @Bean
    @ConditionalOnProperty(name = "notifyhub.audit.enabled", havingValue = "true")
    public NotificationAuditor auditor(NotificationSender sender) {
        return new NotificationAuditor(sender);
    }
}
