package me.braydon.profanity.service;

import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;
import me.braydon.profanity.exception.impl.BadRequestException;
import me.braydon.profanity.model.ProfanityList;
import me.braydon.profanity.model.input.admin.AddWordsInput;
import me.braydon.profanity.model.input.admin.ProfanityListInput;
import me.braydon.profanity.model.input.admin.RemoveWordInput;
import me.braydon.profanity.model.response.admin.AdminStatsResponse;
import me.braydon.profanity.model.response.admin.ProfanityListResponse;
import me.braydon.profanity.repository.ProfanityListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing the profanity list via the admin API.
 *
 * @author Braydon
 */
@Service
public final class ProfanityListService {
    @NonNull private final ProfanityListRepository profanityListRepository;
    @NonNull private final FiltrationService filtrationService;

    @Autowired
    public ProfanityListService(@NonNull ProfanityListRepository profanityListRepository,
                                @NonNull FiltrationService filtrationService) {
        this.profanityListRepository = profanityListRepository;
        this.filtrationService = filtrationService;
    }

    @NonNull
    public ProfanityListResponse getList() {
        return ProfanityListResponse.from(requireList());
    }

    @NonNull
    public ProfanityListResponse replaceList(@NonNull ProfanityListInput input) {
        ProfanityList list = new ProfanityList(
                "primary",
                new ArrayList<>(input.getWhitelistedLinks() != null ? input.getWhitelistedLinks() : List.of()),
                deepCopyMap(input.getProfaneWords()),
                deepCopyMap(input.getProfanePhrases())
        );
        return saveAndReload(list);
    }

    @NonNull
    public ProfanityListResponse reloadFromDatabase() {
        ProfanityList list = filtrationService.reloadFromDatabase();
        if (list == null) {
            throw new BadRequestException("No profanity list found in the database.");
        }
        return ProfanityListResponse.from(list);
    }

    @NonNull
    public ProfanityListResponse reseedFromGitHub() {
        return saveAndReload(filtrationService.seedFromGitHub());
    }

    @NonNull
    public List<String> getWords(@NonNull ContentTag tag, @NonNull Language language, AddWordsInput.EntryType type) {
        ProfanityList list = requireList();
        if (type == AddWordsInput.EntryType.WORD) {
            return copyTerms(list.getProfaneWords(), tag, language);
        }
        if (type == AddWordsInput.EntryType.PHRASE) {
            return copyTerms(list.getProfanePhrases(), tag, language);
        }
        List<String> terms = new ArrayList<>();
        terms.addAll(copyTerms(list.getProfaneWords(), tag, language));
        terms.addAll(copyTerms(list.getProfanePhrases(), tag, language));
        return terms;
    }

    @NonNull
    public ProfanityListResponse addWords(@NonNull AddWordsInput input) {
        ProfanityList current = requireList();
        Map<ContentTag, Map<Language, List<String>>> profaneWords = deepCopyMap(current.getProfaneWords());
        Map<ContentTag, Map<Language, List<String>>> profanePhrases = deepCopyMap(current.getProfanePhrases());

        for (String term : input.getTerms()) {
            String normalized = normalizeTerm(term);
            if (normalized.isEmpty()) {
                continue;
            }
            boolean isPhrase = input.getType() == AddWordsInput.EntryType.PHRASE
                    || (input.getType() == null && normalized.contains(" "));
            Map<ContentTag, Map<Language, List<String>>> target = isPhrase ? profanePhrases : profaneWords;
            List<String> terms = target.computeIfAbsent(input.getTag(), $ -> new HashMap<>())
                    .computeIfAbsent(input.getLanguage(), $ -> new ArrayList<>());
            if (!terms.contains(normalized)) {
                terms.add(normalized);
            }
        }

        return saveAndReload(new ProfanityList(
                current.getId(),
                new ArrayList<>(current.getWhitelistedLinks()),
                profaneWords,
                profanePhrases
        ));
    }

    @NonNull
    public ProfanityListResponse removeWord(@NonNull RemoveWordInput input) {
        ProfanityList current = requireList();
        String normalized = normalizeTerm(input.getTerm());
        Map<ContentTag, Map<Language, List<String>>> profaneWords = deepCopyMap(current.getProfaneWords());
        Map<ContentTag, Map<Language, List<String>>> profanePhrases = deepCopyMap(current.getProfanePhrases());

        boolean removed = false;
        if (input.getType() == AddWordsInput.EntryType.WORD) {
            removed = removeTerm(profaneWords, input.getTag(), input.getLanguage(), normalized);
        } else if (input.getType() == AddWordsInput.EntryType.PHRASE) {
            removed = removeTerm(profanePhrases, input.getTag(), input.getLanguage(), normalized);
        } else {
            removed = removeTerm(profaneWords, input.getTag(), input.getLanguage(), normalized)
                    || removeTerm(profanePhrases, input.getTag(), input.getLanguage(), normalized);
        }

        if (!removed) {
            throw new BadRequestException("Term not found in the profanity list.");
        }

        return saveAndReload(new ProfanityList(
                current.getId(),
                new ArrayList<>(current.getWhitelistedLinks()),
                profaneWords,
                profanePhrases
        ));
    }

    @NonNull
    public List<String> getWhitelistedLinks() {
        return new ArrayList<>(requireList().getWhitelistedLinks());
    }

    @NonNull
    public ProfanityListResponse addWhitelistedLink(@NonNull String link) {
        ProfanityList current = requireList();
        String normalized = link.trim();
        List<String> links = new ArrayList<>(current.getWhitelistedLinks());
        if (!links.contains(normalized)) {
            links.add(normalized);
        }
        return saveAndReload(new ProfanityList(
                current.getId(),
                links,
                deepCopyMap(current.getProfaneWords()),
                deepCopyMap(current.getProfanePhrases())
        ));
    }

    @NonNull
    public ProfanityListResponse removeWhitelistedLink(@NonNull String link) {
        ProfanityList current = requireList();
        String normalized = link.trim();
        List<String> links = new ArrayList<>(current.getWhitelistedLinks());
        if (!links.remove(normalized)) {
            throw new BadRequestException("Link not found in the whitelist.");
        }
        return saveAndReload(new ProfanityList(
                current.getId(),
                links,
                deepCopyMap(current.getProfaneWords()),
                deepCopyMap(current.getProfanePhrases())
        ));
    }

    @NonNull
    public AdminStatsResponse getStats() {
        ProfanityList list = requireList();
        Map<ContentTag, Map<Language, AdminStatsResponse.TagStats>> stats = new HashMap<>();

        for (ContentTag tag : ContentTag.values()) {
            Map<Language, AdminStatsResponse.TagStats> languageStats = new HashMap<>();
            for (Language language : Language.values()) {
                int wordCount = countTerms(list.getProfaneWords(), tag, language);
                int phraseCount = countTerms(list.getProfanePhrases(), tag, language);
                if (wordCount > 0 || phraseCount > 0) {
                    languageStats.put(language, new AdminStatsResponse.TagStats(wordCount, phraseCount));
                }
            }
            if (!languageStats.isEmpty()) {
                stats.put(tag, languageStats);
            }
        }

        return new AdminStatsResponse(stats, list.getWhitelistedLinks().size());
    }

    @NonNull
    private ProfanityList requireList() {
        ProfanityList list = filtrationService.getProfanityList();
        if (list == null) {
            list = profanityListRepository.getProfanityList();
            if (list != null) {
                filtrationService.reloadList(list);
            }
        }
        if (list == null) {
            throw new BadRequestException("No profanity list is loaded.");
        }
        return list;
    }

    @NonNull
    private ProfanityListResponse saveAndReload(@NonNull ProfanityList list) {
        ProfanityList saved = profanityListRepository.save(list);
        filtrationService.reloadList(saved);
        return ProfanityListResponse.from(saved);
    }

    @NonNull
    private static String normalizeTerm(@NonNull String term) {
        return term.trim().toLowerCase();
    }

    @NonNull
    private static List<String> copyTerms(@NonNull Map<ContentTag, Map<Language, List<String>>> source,
                                          @NonNull ContentTag tag, @NonNull Language language) {
        return new ArrayList<>(source.getOrDefault(tag, Map.of()).getOrDefault(language, List.of()));
    }

    private static int countTerms(@NonNull Map<ContentTag, Map<Language, List<String>>> source,
                                  @NonNull ContentTag tag, @NonNull Language language) {
        return source.getOrDefault(tag, Map.of()).getOrDefault(language, List.of()).size();
    }

    private static boolean removeTerm(@NonNull Map<ContentTag, Map<Language, List<String>>> source,
                                      @NonNull ContentTag tag, @NonNull Language language, @NonNull String term) {
        List<String> terms = source.getOrDefault(tag, Map.of()).get(language);
        return terms != null && terms.remove(term);
    }

    @NonNull
    private static Map<ContentTag, Map<Language, List<String>>> deepCopyMap(
            @NonNull Map<ContentTag, Map<Language, List<String>>> source) {
        Map<ContentTag, Map<Language, List<String>>> copy = new HashMap<>();
        for (Map.Entry<ContentTag, Map<Language, List<String>>> tagEntry : source.entrySet()) {
            Map<Language, List<String>> languageMap = new HashMap<>();
            for (Map.Entry<Language, List<String>> langEntry : tagEntry.getValue().entrySet()) {
                languageMap.put(langEntry.getKey(), new ArrayList<>(langEntry.getValue()));
            }
            copy.put(tagEntry.getKey(), languageMap);
        }
        return copy;
    }
}
