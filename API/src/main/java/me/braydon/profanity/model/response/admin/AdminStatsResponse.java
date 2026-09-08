package me.braydon.profanity.model.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;

import java.util.Map;

/**
 * Response containing profanity list statistics.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
public final class AdminStatsResponse {
    @NonNull private final Map<ContentTag, Map<Language, TagStats>> stats;
    private final int whitelistedLinkCount;

    @AllArgsConstructor @Getter
    public static final class TagStats {
        private final int wordCount;
        private final int phraseCount;
    }
}
