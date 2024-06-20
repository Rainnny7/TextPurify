package me.braydon.profanity.notification;

/**
 * A source that can receive notifications.
 *
 * @author Braydon
 */
public abstract class NotificationSource {
    /**
     * Send an alert to this notification source.
     */
    public abstract void alert();
}