package me.braydon.profanity.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.braydon.profanity.common.Language;

import java.util.List;
import java.util.Map;

/**
 * A list of profane words and
 * phrases for each language.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
public final class ProfanityList {
    /**
     * The links that are whitelisted from the filter.
     */
    @NonNull private final List<String> whitelistedLinks;

    /**
     * Profane words for each language.
     */
    @NonNull private final Map<Language, List<String>> profaneWords;

    /**
     * Profane phrases for each language.
     */
    @NonNull private final Map<Language, List<String>> profanePhrases;
}