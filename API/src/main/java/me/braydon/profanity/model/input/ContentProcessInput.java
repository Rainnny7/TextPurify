package me.braydon.profanity.model.input;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.model.FilterContext;

import java.util.List;
import java.util.Map;

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
     * The char to use for matched
     * replacement operations.
     */
    private Character replaceChar = '*';

    /**
     * An optional list of tags to ignore.
     * <p>
     * E.g: If {@link ContentTag#ADVERTISEMENT}
     * is ignored, advertisements will not be
     * filtered.
     * </p>
     */
    private List<ContentTag> ignoredTags;

    /**
     * Optional caller metadata (e.g. player, server).
     */
    private Map<String, String> context;

    /**
     * Return the sanitized context for this input.
     */
    @NonNull
    public FilterContext getFilterContext() {
        return FilterContext.sanitize(this.context);
    }

    /**
     * Check if the given tag is ignored.
     *
     * @param tag the tag to check
     * @return whether the tag is ignored
     */
    public boolean isTagIgnored(@NonNull ContentTag tag) {
        return ignoredTags != null && (ignoredTags.contains(tag));
    }

    /**
     * Check if this input is malformed.
     *
     * @return whether the input is malformed
     */
    public boolean isMalformed() {
        return content == null || content.isEmpty()
                || replaceChar == null || replaceChar == '\0';
    }
}