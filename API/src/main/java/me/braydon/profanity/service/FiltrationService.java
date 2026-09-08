package me.braydon.profanity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import me.braydon.profanity.TextPurifyAPI;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;
import me.braydon.profanity.model.ProfanityList;
import me.braydon.profanity.model.input.ContentProcessInput;
import me.braydon.profanity.model.response.ContentProcessResponse;
import me.braydon.profanity.processor.TextProcessor;
import me.braydon.profanity.processor.impl.AdTextProcessor;
import me.braydon.profanity.processor.impl.ProfanityTextProcessor;
import me.braydon.profanity.repository.ProfanityListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This service is responsible
 * for filtering text content.
 *
 * @author Braydon
 */
@Service @Log4j2(topic = "Filtration Service")
public final class FiltrationService {
    /**
     * The profanity list repository to use.
     */
    @NonNull private final ProfanityListRepository profanityListRepository;

    /**
     * The profanity list to use.
     */
    private ProfanityList profanityList;

    /**
     * The registered text processors to use.
     */
    @NonNull private final List<TextProcessor> textProcessors = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    public FiltrationService(@NonNull ProfanityListRepository profanityListRepository) {
        this.profanityListRepository = profanityListRepository;

        textProcessors.add(new ProfanityTextProcessor());
        textProcessors.add(new AdTextProcessor());
    }

    /**
     * Populate the database with
     * default lists if empty.
     */
    @PostConstruct @SneakyThrows
    public void populateDefaults() {
        long before = System.currentTimeMillis();

        if ((profanityList = profanityListRepository.getProfanityList()) != null) {
            log.info("Loaded lists in {}ms", System.currentTimeMillis() - before);
            return;
        }
        log.info("Downloading pre-made lists...");
        before = System.currentTimeMillis();

        Map<ContentTag, Map<Language, List<String>>> profaneWords = new HashMap<>();
        Map<ContentTag, Map<Language, List<String>>> profanePhrases = new HashMap<>();
        for (Language lang : Language.values()) {
            String contentUrl = "https://raw.githubusercontent.com/Rainnny7/TextPurify/master/lists/" + lang.name().toLowerCase() + ".json";
            JsonObject content = TextPurifyAPI.GSON.fromJson(new Scanner(new URL(contentUrl).openStream(),
                    StandardCharsets.UTF_8
            ).useDelimiter("\\A").next(), JsonObject.class);
            for (String key : content.keySet()) {
                ContentTag tag = ContentTag.valueOf(key.toUpperCase());
                JsonArray items = content.getAsJsonArray(key);
                for (JsonElement item : items) {
                    String element = item.getAsString();
                    Map<ContentTag, Map<Language, List<String>>> target = element.contains(" ")
                            ? profanePhrases : profaneWords;
                    target.computeIfAbsent(tag, $ -> new HashMap<>())
                            .computeIfAbsent(lang, $ -> new ArrayList<>())
                            .add(element);
                }
            }
        }
        profanityList = profanityListRepository.save(new ProfanityList("primary", new ArrayList<>(), profaneWords, profanePhrases));
        log.info("Downloaded lists in {}ms", System.currentTimeMillis() - before);
    }

    /**
     * Filter the content in the given input.
     *
     * @param input the input to filter
     * @return the response from filtering the content
     */
    @NonNull
    public ContentProcessResponse process(@NonNull ContentProcessInput input) {
        String raw = input.getContent().trim();
        String content = raw.toLowerCase();

        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = new LinkedHashSet<>();
        StringBuilder replacement = new StringBuilder(raw);

        if (profanityList != null) {
            for (TextProcessor textProcessor : textProcessors) {
                Set<ContentTag> processorTags = new LinkedHashSet<>();
                replacement = textProcessor.process(profanityList, content, replacement, input.getReplaceChar(),
                        matched, processorTags, input.getIgnoredTags());
                tags.addAll(processorTags);
            }
        }

        double score = calculateScore(matched, tags);

        return new ContentProcessResponse(!matched.isEmpty(), input.getContent(), replacement.toString(), matched, new ArrayList<>(tags), score);
    }

    private double calculateScore(@NonNull List<String> matched, @NonNull Set<ContentTag> tags) {
        double score = 0D;
        for (String match : matched) {
            score += 2D / (double) match.length();
        }
        if (tags.contains(ContentTag.HATE_SPEECH) || tags.contains(ContentTag.SELF_HARM)) {
            score *= 1.5D;
        }
        return Math.min(score, 1D);
    }
}
