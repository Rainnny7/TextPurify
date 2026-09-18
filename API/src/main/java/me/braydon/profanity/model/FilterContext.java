package me.braydon.profanity.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic key-value metadata attached to a filter request.
 *
 * @author Braydon
 */
public final class FilterContext {
    private static final int MAX_KEYS = 10;
    private static final int MAX_KEY_LENGTH = 32;
    private static final int MAX_VALUE_LENGTH = 256;

    private static final FilterContext EMPTY = new FilterContext(Map.of());

    @Getter(onMethod_ = @JsonValue)
    @NonNull private final Map<String, String> values;

    private FilterContext(@NonNull Map<String, String> values) {
        this.values = values;
    }

    /**
     * Return an empty context.
     */
    @NonNull
    public static FilterContext empty() {
        return EMPTY;
    }

    /**
     * Build a context from the provided values.
     */
    @NonNull
    public static FilterContext of(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }
        return new FilterContext(Map.copyOf(values));
    }

    /**
     * Sanitize caller-provided context before processing.
     */
    @NonNull
    public static FilterContext sanitize(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }
        Map<String, String> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (sanitized.size() >= MAX_KEYS) {
                break;
            }
            String key = trimToLength(entry.getKey(), MAX_KEY_LENGTH);
            String value = trimToLength(entry.getValue(), MAX_VALUE_LENGTH);
            if (key.isEmpty() || value.isEmpty()) {
                continue;
            }
            sanitized.put(key, value);
        }
        if (sanitized.isEmpty()) {
            return empty();
        }
        return new FilterContext(Collections.unmodifiableMap(sanitized));
    }

    /**
     * Whether this context has any entries.
     */
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    private static String trimToLength(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
