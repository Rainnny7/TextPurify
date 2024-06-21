package me.braydon.profanity.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The content that should
 * be sent within a notification.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
public final class NotificationContent {
    /**
     * Whether content should be displayed.
     */
    private final boolean displayContent;

    /**
     * Whether matched content should be displayed.
     */
    private final boolean displayMatched;

    /**
     * Whether obtained tags should be displayed.
     */
    private final boolean displayTags;

    /**
     * Whether the score should be displayed.
     */
    private final boolean displayScore;
}