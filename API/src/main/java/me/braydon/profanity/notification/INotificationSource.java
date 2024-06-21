package me.braydon.profanity.notification;

import lombok.NonNull;
import me.braydon.profanity.model.response.ContentProcessResponse;

/**
 * A source that can receive notifications.
 *
 * @author Braydon
 */
public interface INotificationSource {
    /**
     * Check if this source is enabled.
     *
     * @return whether this source is enabled
     */
    boolean isEnabled();

    /**
     * Send an alert to this source.
     *
     * @param response the content response to alert for
     * @param content the content to send in the alert
     */
    void alert(@NonNull ContentProcessResponse response, @NonNull NotificationContent content);
}