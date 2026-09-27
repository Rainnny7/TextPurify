package me.braydon.profanity.processor.impl;

import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.model.ProfanityList;
import me.braydon.profanity.processor.TextProcessor;
import me.braydon.profanity.util.LinkWhitelistUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    /**
     * Hostnames with consecutive dots (ellipses like {@code worked...ty}) must not match.
     * Labels are alphanumeric, with optional hyphens in the middle.
     */
    private static final Pattern URL_REGEX = Pattern.compile(
            "(?i)\\b((?:https?://)?(?:www\\.)?(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,10}(?:/\\S*)?)\\b"
    );
    private static final Pattern IPV4_REGEX = Pattern.compile("(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))");

    /**
     * Country-code TLDs that are also common English words. Bare {@code did.it} / {@code go.to}
     * fragments in chat are almost never ads; require a protocol, {@code www}, path, or subdomain.
     */
    private static final Set<String> AMBIGUOUS_TLDS = Set.of(
            "ad", "am", "an", "as", "at", "be", "by", "do", "he", "id", "if",
            "in", "is", "it", "me", "my", "no", "on", "or", "re", "so", "to",
            "us", "we"
    );

    public AdTextProcessor() {
        super(ContentTag.ADVERTISEMENT);
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
        if (ignoredTags != null && ignoredTags.contains(ContentTag.ADVERTISEMENT)) {
            return replacement;
        }

        AtomicInteger offset = new AtomicInteger();
        Consumer<Matcher> handleReplacements = matcher -> {
            while (matcher.find()) {
                String matchedGroup = matcher.group();
                if (!isLikelyAdvertisementUrl(matchedGroup)) {
                    continue;
                }
                if (LinkWhitelistUtils.isWhitelisted(matchedGroup, profanityList.getWhitelistedLinks())) {
                    continue;
                }
                matched.add(matchedGroup);
                matchedTags.add(ContentTag.ADVERTISEMENT);

                int start = offset.get() + matcher.start();
                int end = offset.get() + matcher.end();
                String matchedWord = matcher.group();
                replacement.replace(start, end, Character.toString(replaceChar).repeat(matchedWord.length()));
                offset.set(offset.get() + (matchedWord.length() - (end - start)));
            }
        };
        handleReplacements.accept(URL_REGEX.matcher(content));
        handleReplacements.accept(IPV4_REGEX.matcher(content));
        return replacement;
    }

    /**
     * Whether a regex URL match is likely an advertisement rather than casual punctuation.
     */
    private static boolean isLikelyAdvertisementUrl(@NonNull String matched) {
        String lower = matched.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www.")) {
            return true;
        }
        if (matched.indexOf('/') >= 0) {
            return true;
        }
        String host = LinkWhitelistUtils.normalizeHost(matched);
        if (host.isEmpty() || host.contains("..")) {
            return false;
        }
        int lastDot = host.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == host.length() - 1) {
            return false;
        }
        int dots = 0;
        for (int i = 0; i < host.length(); i++) {
            if (host.charAt(i) == '.') {
                dots++;
            }
        }
        if (dots >= 2) {
            return true;
        }
        return !AMBIGUOUS_TLDS.contains(host.substring(lastDot + 1));
    }
}
