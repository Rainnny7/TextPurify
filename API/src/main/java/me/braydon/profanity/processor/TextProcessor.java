package me.braydon.profanity.processor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.model.ProfanityList;

import java.util.List;
import java.util.Set;

/**
 * @author Braydon
 */
@AllArgsConstructor @Getter
public abstract class TextProcessor {
    /**
     * The tag that should be applied to content
     * if they are processed by this processor.
     */
    @NonNull private final ContentTag tag;

    /**
     * Processor the given content.
     *
     * @param profanityList the profanity list to use
     * @param content the content to process
     * @param replacement the replacement content to modify
     * @param replaceChar the replace char to use
     * @param matched the matched content to add to
     * @param matchedTags the tags obtained from matches in this processor
     * @param ignoredTags optional tags to skip filtering for
     * @return the replaced content
     */
    @NonNull public abstract StringBuilder process(@NonNull ProfanityList profanityList, @NonNull String content,
                                            @NonNull StringBuilder replacement, int replaceChar,
                                            @NonNull List<String> matched, @NonNull Set<ContentTag> matchedTags,
                                            List<ContentTag> ignoredTags);
}
