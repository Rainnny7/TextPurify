package me.braydon.profanity.model.input;

import lombok.Getter;
import lombok.Setter;

/**
 * The input to use for processing content.
 *
 * @author Braydon
 */
@Setter @Getter
public final class ContentProcessInput {
    /**
     * The content to process.
     */
    private String content;

    /**
     * Check if this input is malformed.
     *
     * @return whether the input is malformed
     */
    public boolean isMalformed() {
        return content == null || content.isEmpty();
    }
}