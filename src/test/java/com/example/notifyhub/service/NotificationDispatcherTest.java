package com.example.notifyhub.service;

import com.example.notifyhub.model.Notification;
import com.example.notifyhub.repository.NotificationLogRepository;
import com.example.notifyhub.service.transformers.MessageTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the rule-filtering pipeline in {@link NotificationDispatcher}.
 * Requests are parsed from JSON, exactly like the REST API does.
 */
class NotificationDispatcherTest {

    private final MessageTransformer header = mock(MessageTransformer.class);
    private final MessageTransformer truncate = mock(MessageTransformer.class);
    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        when(header.getName()).thenReturn("header");
        when(truncate.getName()).thenReturn("truncate");
        dispatcher = new NotificationDispatcher(mock(ChannelService.class), mock(TemplateResolver.class),
                mock(NotificationLogRepository.class), List.of(header, truncate));
    }

    @Test
    void appliesEnabledTransformRules() {
        dispatcher.dispatch(fromJson("""
                {"recipient":"anton@example.com","channel":"email","templateCode":"welcome",
                 "rules":{"transform.header":"Important Update"}}
                """));

        verify(header).apply(eq("Important Update"), any());
    }

    @Test
    void skipsDisabledRules() {
        dispatcher.dispatch(fromJson("""
                {"recipient":"anton@example.com","channel":"email","templateCode":"welcome",
                 "rules":{"transform.header":"Important Update","transform.truncate":"disabled"}}
                """));

        verify(header).apply(eq("Important Update"), any());
        verify(truncate, never()).apply(any(), any());
    }

    private static Notification fromJson(String json) {
        return JsonMapper.builder().build().readValue(json, Notification.class);
    }
}
