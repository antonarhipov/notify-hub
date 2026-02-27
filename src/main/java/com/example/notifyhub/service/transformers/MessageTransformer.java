package com.example.notifyhub.service.transformers;

import com.example.notifyhub.model.Notification;

/**
 * Interface for notification message transformers.
 * Each implementation handles a specific content-enrichment rule
 * identified by its {@code transform.*} key in the notification's rules map.
 */
public interface MessageTransformer {

    /**
     * Returns the transform name this transformer handles
     * (the part after the {@code "transform."} prefix).
     *
     * @return the transform name, e.g. {@code "priority"}, {@code "append-footer"}
     */
    String getName();

    /**
     * Applies the transformation to the notification and returns a human-readable
     * description of what was applied.
     *
     * @param value        the rule value from the rules map
     * @param notification the notification being processed
     * @return a description of the applied transformation
     */
    String apply(String value, Notification notification);
}
