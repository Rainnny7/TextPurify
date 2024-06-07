package me.braydon.profanity.processor.impl;

import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.model.ProfanityList;
import me.braydon.profanity.processor.TextProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text processor to filter advertisement content.
 *
 * @author Braydon
 */
public final class AdTextProcessor extends TextProcessor {
    private static final Pattern URL_REGEX = Pattern.compile("(?i)\\b((?:https?://)?(?:www\\.)?[a-z0-9.-]+\\.[a-z]{2,10}(?:/\\S*)?)\\b");
    private static final Pattern IPV4_REGEX = Pattern.compile("(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))");

    public AdTextProcessor() {
        super(ContentTag.ADVERTISEMENT);
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
        AtomicInteger offset = new AtomicInteger();
        Consumer<Matcher> handleReplacements = matcher -> {
            while (matcher.find()) {
                String matchedGroup = matcher.group();
                matched.add(matchedGroup);

                // Replace the matched group with the replace char
                int start = offset.get() + matcher.start();
                int end = offset.get() + matcher.end();
                String matchedWord = matcher.group();
                replacement.replace(start, end, Character.toString(replaceChar).repeat(matchedWord.length()));
                offset.set(offset.get() + (matchedWord.length() - (end - start)));
            }
        };
        handleReplacements.accept(URL_REGEX.matcher(content)); // Handle URLs
        handleReplacements.accept(IPV4_REGEX.matcher(content)); // Handle IPs
        return replacement;
    }
}