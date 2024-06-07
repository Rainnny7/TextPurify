package me.braydon.profanity.processor.impl;

import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;
import me.braydon.profanity.model.ProfanityList;
import me.braydon.profanity.processor.TextProcessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text processor to filter vulgar content.
 *
 * @author Braydon
 */
public final class VulgarityProcessor extends TextProcessor {
    /**
     * Patterns for profane words.
     */
    private static final Map<String, Pattern> wordPatterns = Collections.synchronizedMap(new HashMap<>());

    /**
     * Substitutions for characters in profane words.
     */
    private static final Map<Character, Character> charSubstitutions = Collections.synchronizedMap(new HashMap<>());
    static { // Populate char substitutions
        charSubstitutions.put('3', 'e');
        charSubstitutions.put('1', 'i');
        charSubstitutions.put('!', 'i');
        charSubstitutions.put('@', 'a');
        charSubstitutions.put('7', 't');
        charSubstitutions.put('0', 'o');
        charSubstitutions.put('5', 's');
        charSubstitutions.put('8', 'b');
        charSubstitutions.put('$', 's');
    }

    public VulgarityProcessor() {
        super(ContentTag.VULGARITY);
    }

    /**
     * Processor the given content.
     *
     * @param profanityList the profanity list to use
     * @param content       the content to process
     * @param replacement   the replacement content to modify
     * @param replaceChar   the replace char to use
     * @param matched       the matched content to add to
     * @return the replaced content
     */
    @Override @NonNull
    public StringBuilder process(@NonNull ProfanityList profanityList, @NonNull String content,
                                 @NonNull StringBuilder replacement, int replaceChar, @NonNull List<String> matched) {
        // Populate word patterns if empty
        if (wordPatterns.isEmpty()) {
            populatePatterns(profanityList);
        }
        content = content.replaceAll("\\p{Punct}", ""); // Replace punctuation

        // Process single words in the content
        int offset = 0;
        for (Map.Entry<String, Pattern> entry : wordPatterns.entrySet()) {
            String word = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                matched.add(word);
                int start = offset + matcher.start();
                int end = offset + matcher.end();
                replacement.replace(start, end, Character.toString(replaceChar).repeat(word.length()));
                offset += word.length() - (end - start);
            }
        }

        // TODO: Process phrases in the content

        return replacement;
    }

    /**
     * Populate the word patterns
     * for the given profanity list.
     *
     * @param profanityList the profanity list to use
     */
    private void populatePatterns(@NonNull ProfanityList profanityList) {
        for (Map.Entry<Language, List<String>> entry : profanityList.getProfaneWords().entrySet()) {
            for (String word : entry.getValue()) {
                wordPatterns.put(word, Pattern.compile(buildCombinedRegex(word), Pattern.CASE_INSENSITIVE));
            }
        }
    }

    /**
     * Build a regex pattern for the given word.
     * <p>
     * This pattern will match the exact, and
     * obfuscated versions of the word.
     * </p>
     *
     * @param word the word to build for
     * @return the built regex pattern
     */
    @NonNull
    private String buildCombinedRegex(@NonNull String word) {
        StringBuilder exactWordRegex = new StringBuilder();
        StringBuilder obfuscatedWordRegex = new StringBuilder();

        for (char character : word.toCharArray()) {
            char lowerChar = Character.toLowerCase(character);
            exactWordRegex.append(lowerChar);
            if (charSubstitutions.containsKey(lowerChar)) {
                obfuscatedWordRegex.append('[').append(lowerChar).append(charSubstitutions.get(lowerChar)).append(']');
            } else {
                obfuscatedWordRegex.append(lowerChar);
            }
        }

        // Build the pattern
        return exactWordRegex + ((exactWordRegex.compareTo(obfuscatedWordRegex) == 0) ? "" : "|" + obfuscatedWordRegex);
    }
}