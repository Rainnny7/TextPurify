package me.braydon.profanity.model.input.admin;

import lombok.Getter;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;

import java.util.List;
import java.util.Map;

/**
 * Input for replacing the entire profanity list.
 *
 * @author Braydon
 */
@Getter
public final class ProfanityListInput {
    private List<String> whitelistedLinks;
    private Map<ContentTag, Map<Language, List<String>>> profaneWords;
    private Map<ContentTag, Map<Language, List<String>>> profanePhrases;

    public boolean isMalformed() {
        return profaneWords == null || profanePhrases == null;
    }
}
