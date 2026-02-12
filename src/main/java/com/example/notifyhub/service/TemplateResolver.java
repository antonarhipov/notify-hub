package com.example.notifyhub.service;

import com.example.notifyhub.model.NotificationTemplate;
import com.example.notifyhub.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

/**
 * Service for resolving notification templates.
 * Demonstrates Lombok's @RequiredArgsConstructor - no visible constructor but fields are injected.
 * IntelliJ IDEA will show gutter icons on fields indicating dependency injection.
 */
//DEMO Lombok @RequiredArgsConstructor - No visible constructor in code
//DEMO Gutter icons on fields below show dependency injection
//DEMO IDE understands Lombok-generated constructor
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateResolver {

    private final NotificationTemplateRepository templateRepository;
    private final MessageSource messageSource;

    /**
     * Resolve a notification template by code, locale, and channel.
     *
     * @param code    the template code
     * @param locale  the locale string
     * @param channel the notification channel
     * @return the resolved template, or empty if not found
     */
    public Optional<NotificationTemplate> resolve(String code, String locale, String channel) {
        log.debug("Resolving template: code={}, locale={}, channel={}", code, locale, channel);

        Optional<NotificationTemplate> template = templateRepository
                .findByCodeLocaleAndChannel(code, locale, channel);

        if (template.isEmpty()) {
            log.warn("Template not found: code={}, locale={}, channel={}", code, locale, channel);
            // Try fallback to English
            if (!"en".equals(locale)) {
                log.debug("Attempting fallback to English locale");
                template = templateRepository.findByCodeLocaleAndChannel(code, "en", channel);
            }
        }

        template.ifPresent(t ->
                log.debug("Template resolved: id={}, channel={}", t.getId(), t.getChannel())
        );

        return template;
    }

    /**
     * Get a message from MessageSource for demonstration purposes.
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }
}
