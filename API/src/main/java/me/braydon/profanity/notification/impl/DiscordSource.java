package me.braydon.profanity.notification.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import me.braydon.profanity.notification.NotificationSource;

/**
 * A notification source that
 * sends alerts to a Discord webhook.
 *
 * @author Braydon
 */
@AllArgsConstructor
public final class DiscordSource extends NotificationSource {
    /**
     * The URL of the webhook to send alerts to.
     */
    @NonNull private final String webhookUrl;

    /**
     * Send an alert to this notification source.
     */
    @Override
    public void alert() {}
}