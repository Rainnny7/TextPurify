package me.braydon.profanity.model;

import me.braydon.profanity.model.input.ContentProcessInput;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterContextTest {
    @Test
    void emptyReturnsSharedInstance() {
        assertTrue(FilterContext.empty().isEmpty());
        assertTrue(FilterContext.sanitize(null).isEmpty());
        assertTrue(FilterContext.sanitize(Map.of()).isEmpty());
    }

    @Test
    void sanitizeTrimsAndDropsBlankEntries() {
        FilterContext context = FilterContext.sanitize(Map.of(
                " player ", " Steve ",
                "server", "",
                "", "value",
                "cause", "Global Chat"
        ));
        assertEquals(2, context.getValues().size());
        assertEquals("Steve", context.getValues().get("player"));
        assertEquals("Global Chat", context.getValues().get("cause"));
    }

    @Test
    void sanitizeCapsKeyCountAndLengths() {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            values.put("key-" + i, "value-" + i);
        }
        values.put("long-key-" + "x".repeat(40), "long-value-" + "y".repeat(300));
        FilterContext context = FilterContext.sanitize(values);
        assertEquals(10, context.getValues().size());
        String longKey = context.getValues().keySet().stream()
                .filter(key -> key.startsWith("long-key-"))
                .findFirst()
                .orElseThrow();
        assertEquals(32, longKey.length());
        assertEquals(256, context.getValues().get(longKey).length());
    }

    @Test
    void missingContextIsOptional() {
        ContentProcessInput input = new ContentProcessInput();
        input.setContent("hello");
        assertTrue(input.getFilterContext().isEmpty());
        assertTrue(input.getContext() == null);
    }

    @Test
    void inputRoundTripsSanitizedContext() {
        ContentProcessInput input = new ContentProcessInput();
        input.setContent("hello");
        input.setContext(Map.of(
                "player", " Steve ",
                "server", "Survival",
                "cause", "Global Chat"
        ));
        FilterContext context = input.getFilterContext();
        assertEquals("Steve", context.getValues().get("player"));
        assertEquals("Survival", context.getValues().get("server"));
        assertEquals("Global Chat", context.getValues().get("cause"));
    }
}
