package me.braydon.profanity.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Helpers for matching advertisement URLs against whitelisted domains.
 *
 * @author Braydon
 */
@UtilityClass
public final class LinkWhitelistUtils {
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"
    );

    /**
     * Whether the matched URL or host is covered by a whitelisted link entry.
     *
     * @param matched the matched URL, host, or IP
     * @param whitelistedLinks the configured whitelist entries
     * @return true if the match should be ignored
     */
    public static boolean isWhitelisted(String matched, List<String> whitelistedLinks) {
        if (matched == null || matched.isBlank() || whitelistedLinks == null || whitelistedLinks.isEmpty()) {
            return false;
        }
        String normalizedHost = normalizeHost(matched);
        if (normalizedHost.isEmpty()) {
            return false;
        }
        for (String link : whitelistedLinks) {
            if (link == null || link.isBlank()) {
                continue;
            }
            if (matchesWhitelistEntry(normalizedHost, normalizeHost(link))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normalize a URL or host for whitelist comparison.
     */
    public static String normalizeHost(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("http://")) {
            normalized = normalized.substring("http://".length());
        } else if (normalized.startsWith("https://")) {
            normalized = normalized.substring("https://".length());
        }
        int slash = normalized.indexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(0, slash);
        }
        int colon = normalized.indexOf(':');
        if (colon >= 0) {
            normalized = normalized.substring(0, colon);
        }
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring("www.".length());
        }
        return normalized;
    }

    private static boolean matchesWhitelistEntry(String normalizedHost, String normalizedEntry) {
        if (normalizedHost.isEmpty() || normalizedEntry.isEmpty()) {
            return false;
        }
        if (normalizedHost.equals(normalizedEntry)) {
            return true;
        }
        if (isIpv4(normalizedHost) || isIpv4(normalizedEntry)) {
            return false;
        }
        return normalizedHost.endsWith("." + normalizedEntry);
    }

    private static boolean isIpv4(String value) {
        return IPV4_PATTERN.matcher(value).matches();
    }
}
