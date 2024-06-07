package me.braydon.profanity.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;

import java.util.List;

/**
 * @author Braydon
 */
@AllArgsConstructor @Getter
public final class ContentProcessResponse {
    /**
     * Does the content contain profanity?
     */
    private final boolean containsProfanity;

    /**
     * The replacement for the content.
     */
    @NonNull private final String replacement;

    /**
     * The matched elements in the content.
     */
    @NonNull private final List<String> matched;

    /**
     * The tags obtained from the content.
     */
    @NonNull private final List<ContentTag> tags;

    /**
     * The score of the content.
     * <p>
     * This is a value from 0-1 representing the
     * probability that the content is profane.
     * </p>
     */
    private final double score;
}