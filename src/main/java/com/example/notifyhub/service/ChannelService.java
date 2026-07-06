package com.example.notifyhub.service;

import com.example.notifyhub.model.ServiceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of the available notification channel services.
 * <p>
 * Discovers every {@link NotificationSender} bean and exposes, for each channel,
 * the sender to route to and an in-memory enabled/disabled flag that the mission
 * control dashboard can toggle at runtime. All channels start enabled.
 */
@Service
@Slf4j
public class ChannelService {

    private final Map<String, NotificationSender> sendersByChannel;
    private final Map<String, Boolean> enabledByChannel = new ConcurrentHashMap<>();

    public ChannelService(List<NotificationSender> senders) {
        this.sendersByChannel = senders.stream()
                .collect(Collectors.toMap(
                        s -> s.getChannel().toLowerCase(),
                        Function.identity(),
                        (a, b) -> a));
        this.sendersByChannel.keySet().forEach(channel -> enabledByChannel.put(channel, true));
        log.info("Registered notification channels: {}", enabledByChannel.keySet());
    }

    /**
     * @return all known channel identifiers, in a stable alphabetical order
     */
    public List<String> getChannels() {
        return enabledByChannel.keySet().stream().sorted().toList();
    }

    /**
     * @return whether the given channel is known to the hub
     */
    public boolean isKnownChannel(String channel) {
        return channel != null && enabledByChannel.containsKey(channel.toLowerCase());
    }

    /**
     * @return whether the given channel service is currently enabled
     */
    public boolean isEnabled(String channel) {
        return channel != null && Boolean.TRUE.equals(enabledByChannel.get(channel.toLowerCase()));
    }

    /**
     * Enable or disable the service for the given channel.
     *
     * @param channel the channel identifier
     * @param enabled the new desired state
     * @return the resulting {@link ServiceStatus}
     * @throws ChannelNotSupportedException if the channel is unknown
     */
    public ServiceStatus setEnabled(String channel, boolean enabled) {
        String key = channel == null ? null : channel.toLowerCase();
        if (key == null || !enabledByChannel.containsKey(key)) {
            throw new ChannelNotSupportedException("Unknown notification channel: " + channel);
        }
        enabledByChannel.put(key, enabled);
        log.info("Channel '{}' {}", key, enabled ? "ENABLED" : "DISABLED");
        return new ServiceStatus(key, enabled);
    }

    /**
     * @return the sender registered for the channel, if any
     */
    public Optional<NotificationSender> getSender(String channel) {
        return channel == null
                ? Optional.empty()
                : Optional.ofNullable(sendersByChannel.get(channel.toLowerCase()));
    }

    /**
     * @return the on/off state of every channel service, ordered by channel name
     */
    public List<ServiceStatus> getServices() {
        return getChannels().stream()
                .map(channel -> new ServiceStatus(channel, isEnabled(channel)))
                .toList();
    }
}
