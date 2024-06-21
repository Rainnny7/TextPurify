package me.braydon.profanity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import me.braydon.profanity.processor.impl.VulgarityTextProcessor;
import me.braydon.profanity.repository.ProfanityListRepository;
import org.apache.commons.text.StringEscapeUtils;
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

        // Register text processors
        textProcessors.add(new VulgarityTextProcessor());
        textProcessors.add(new AdTextProcessor());
    }

    /**
     * Populate the database with
     * default lists if empty.
     */
    @PostConstruct @SneakyThrows
    public void populateDefaults() {
        long before = System.currentTimeMillis();

        // List is already present
        if ((profanityList = profanityListRepository.getProfanityList()) != null) {
            log.info("Loaded lists in {}ms", System.currentTimeMillis() - before);
            return;
        }
        // Download the pre-made lists
        // for each language and save it.
        log.info("Downloading pre-made lists...");
        before = System.currentTimeMillis();

        Map<Language, List<String>> profaneWords = new HashMap<>();
        Map<Language, List<String>> profanePhrases = new HashMap<>();
        for (Language lang : Language.values()) {
            String contentUrl = "https://raw.githubusercontent.com/Rainnny7/TextPurify/master/lists/" + lang.name().toLowerCase() + ".json";
            JsonArray content = TextPurifyAPI.GSON.fromJson(new Scanner(new URL(contentUrl).openStream(),
                    StandardCharsets.UTF_8
            ).useDelimiter("\\A").next(), JsonArray.class);
            for (JsonElement item : content) {
                String element = item.getAsString();
                (element.contains(" ") ? profanePhrases : profaneWords).computeIfAbsent(lang, $ -> new ArrayList<>()).add(element);
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
        String content = StringEscapeUtils.escapeJava(input.getContent()).toLowerCase().trim(); // The content to filter

        List<String> matched = new ArrayList<>(); // The content that was matched
        List<ContentTag> tags = new ArrayList<>(); // Tags obtained from the processed content
        StringBuilder replacement = new StringBuilder(input.getContent());

        // Handle filtering if a profanity list is present
        if (profanityList != null) {
            // Invoke each text processor on the content
            for (TextProcessor textProcessor : textProcessors) {
                ContentTag tag = textProcessor.getTag();

                // Skip this processor as it's tag is ignored.
                if (input.isTagIgnored(tag)) {
                    continue;
                }
                int before = matched.size();
                replacement = textProcessor.process(profanityList, content, replacement, input.getReplaceChar(), matched);
                if (matched.size() > before) {
                    tags.add(tag);
                }
            }
        }

        // Calculate the score based on
        // the matched profane content, that cannot be bypassed by changing the content length
        double score = 0D;
        for (String match : matched) {
            score+= 2D / (double) match.length();
        }
        score = Math.min(score, 1D);

        return new ContentProcessResponse(!matched.isEmpty(), input.getContent(), replacement.toString(), matched, tags, score);
    }
}