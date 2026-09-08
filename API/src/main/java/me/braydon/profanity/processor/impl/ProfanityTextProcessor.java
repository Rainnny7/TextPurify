package me.braydon.profanity.processor.impl;

import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;
import me.braydon.profanity.model.ProfanityList;
import me.braydon.profanity.processor.TextProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text processor to filter profane content across all profanity categories.
 *
 * @author Braydon
 */
public final class ProfanityTextProcessor extends TextProcessor {
    private static final String PUNCTUATION_PATTERN = "[\\p{Punct}]*";
    private static final String PHRASE_SEPARATOR = "[\\s\\p{Punct}]+";
    private static final Set<ContentTag> PROFANITY_TAGS = EnumSet.of(
            ContentTag.SEXUAL, ContentTag.VULGAR, ContentTag.HATE_SPEECH, ContentTag.SELF_HARM, ContentTag.SHOCK
    );

    /**
     * Substitutions for characters in profane words.
     */
    private static final Map<Character, List<Character>> CHAR_SUBSTITUTIONS = Map.of(
            'e', List.of('3'),
            'i', List.of('1', '!'),
            'a', List.of('4', '@'),
            't', List.of('7'),
            'o', List.of('0'),
            's', List.of('5', '$'),
            'b', List.of('8'),
            'g', List.of('q'),
            'u', List.of('v'),
            '1', List.of('!')
    );

    private final Map<String, List<PatternEntry>> patternCache = new HashMap<>();

    public ProfanityTextProcessor() {
        super(ContentTag.SEXUAL);
    }

    /**
     * Process the given content.
     *
     * @param profanityList the profanity list to use
     * @param content       the content to process
     * @param replacement   the replacement content to modify
     * @param replaceChar   the replace char to use
     * @param matched       the matched content to add to
     * @param matchedTags   the tags obtained from matches
     * @param ignoredTags   optional tags to skip filtering for
     * @return the replaced content
     */
    @Override @NonNull
    public StringBuilder process(@NonNull ProfanityList profanityList, @NonNull String content,
                                 @NonNull StringBuilder replacement, int replaceChar,
                                 @NonNull List<String> matched, @NonNull Set<ContentTag> matchedTags,
                                 List<ContentTag> ignoredTags) {
        List<PatternEntry> patterns = getPatterns(profanityList, ignoredTags);
        boolean[] occupied = new boolean[content.length()];
        List<MatchResult> results = new ArrayList<>();

        for (PatternEntry entry : patterns) {
            Matcher matcher = entry.pattern.matcher(content);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (isOccupied(occupied, start, end)) {
                    continue;
                }
                markOccupied(occupied, start, end);
                results.add(new MatchResult(start, end, entry.term, entry.tag));
            }
        }

        results.sort(Comparator.comparingInt(MatchResult::start).reversed());
        for (MatchResult result : results) {
            matched.add(result.term);
            matchedTags.add(result.tag);
            String matchedText = replacement.substring(result.start, result.end);
            replacement.replace(result.start, result.end,
                    Character.toString(replaceChar).repeat(matchedText.length()));
        }

        return replacement;
    }

    @Override
    public void invalidateCache() {
        patternCache.clear();
    }

    @NonNull
    private List<PatternEntry> getPatterns(@NonNull ProfanityList profanityList, List<ContentTag> ignoredTags) {
        String cacheKey = profanityList.getId() + ":" + (ignoredTags == null ? "" : ignoredTags.toString());
        return patternCache.computeIfAbsent(cacheKey, $ -> buildPatterns(profanityList, ignoredTags));
    }

    @NonNull
    private List<PatternEntry> buildPatterns(@NonNull ProfanityList profanityList, List<ContentTag> ignoredTags) {
        List<PatternEntry> entries = new ArrayList<>();

        for (ContentTag tag : PROFANITY_TAGS) {
            if (isTagIgnored(tag, ignoredTags)) {
                continue;
            }
            Map<Language, List<String>> words = profanityList.getProfaneWords().getOrDefault(tag, Collections.emptyMap());
            Map<Language, List<String>> phrases = profanityList.getProfanePhrases().getOrDefault(tag, Collections.emptyMap());

            for (List<String> terms : words.values()) {
                for (String term : terms) {
                    entries.add(new PatternEntry(term, tag,
                            Pattern.compile("\\b" + buildObfuscatedRegex(term) + "\\b", Pattern.CASE_INSENSITIVE)));
                }
            }
            for (List<String> terms : phrases.values()) {
                for (String term : terms) {
                    entries.add(new PatternEntry(term, tag,
                            Pattern.compile(buildPhraseRegex(term), Pattern.CASE_INSENSITIVE)));
                }
            }
        }

        entries.sort(Comparator.comparingInt((PatternEntry entry) -> entry.term.length()).reversed());
        return entries;
    }

    private boolean isTagIgnored(@NonNull ContentTag tag, List<ContentTag> ignoredTags) {
        return ignoredTags != null && ignoredTags.contains(tag);
    }

    private static boolean isOccupied(boolean[] occupied, int start, int end) {
        for (int i = start; i < end; i++) {
            if (occupied[i]) {
                return true;
            }
        }
        return false;
    }

    private static void markOccupied(boolean[] occupied, int start, int end) {
        for (int i = start; i < end; i++) {
            occupied[i] = true;
        }
    }

    /**
     * Build a regex pattern for the given word with obfuscation support.
     *
     * @param word the word to build for
     * @return the built regex pattern
     */
    @NonNull
    private String buildObfuscatedRegex(@NonNull String word) {
        StringBuilder exactWordRegex = new StringBuilder();
        StringBuilder obfuscatedWordRegex = new StringBuilder();

        for (char character : word.toCharArray()) {
            char lowerChar = Character.toLowerCase(character);
            exactWordRegex.append(Pattern.quote(String.valueOf(lowerChar)));
            if (CHAR_SUBSTITUTIONS.containsKey(lowerChar)) {
                StringBuilder chars = new StringBuilder(Character.toString(lowerChar));
                for (Character substitution : CHAR_SUBSTITUTIONS.get(lowerChar)) {
                    chars.append(substitution);
                }
                obfuscatedWordRegex.append(PUNCTUATION_PATTERN + "[").append(chars).append("]+" + PUNCTUATION_PATTERN);
            } else {
                obfuscatedWordRegex.append(PUNCTUATION_PATTERN).append(Pattern.quote(String.valueOf(lowerChar))).append(PUNCTUATION_PATTERN);
            }
        }

        String exact = exactWordRegex.toString();
        String obfuscated = obfuscatedWordRegex.toString();
        return exact.equals(obfuscated) ? exact : "(?:" + exact + "|" + obfuscated + ")";
    }

    /**
     * Build a regex pattern for the given phrase with flexible separators.
     *
     * @param phrase the phrase to build for
     * @return the built regex pattern
     */
    @NonNull
    private String buildPhraseRegex(@NonNull String phrase) {
        String[] tokens = phrase.trim().split("\\s+");
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                regex.append(PHRASE_SEPARATOR);
            }
            regex.append("(?:").append(buildObfuscatedRegex(tokens[i])).append(")");
        }
        return regex.toString();
    }

    private record PatternEntry(@NonNull String term, @NonNull ContentTag tag, @NonNull Pattern pattern) { }

    private record MatchResult(int start, int end, @NonNull String term, @NonNull ContentTag tag) { }
}
