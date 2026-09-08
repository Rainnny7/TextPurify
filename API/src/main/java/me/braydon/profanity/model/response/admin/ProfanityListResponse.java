package me.braydon.profanity.model.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;
import me.braydon.profanity.model.ProfanityList;

import java.util.List;
import java.util.Map;

/**
 * Response containing the full profanity list.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
public final class ProfanityListResponse {
    @NonNull private final String id;
    @NonNull private final List<String> whitelistedLinks;
    @NonNull private final Map<ContentTag, Map<Language, List<String>>> profaneWords;
    @NonNull private final Map<ContentTag, Map<Language, List<String>>> profanePhrases;

    @NonNull
    public static ProfanityListResponse from(@NonNull ProfanityList list) {
        return new ProfanityListResponse(
                list.getId(),
                list.getWhitelistedLinks(),
                list.getProfaneWords(),
                list.getProfanePhrases()
        );
    }
}
