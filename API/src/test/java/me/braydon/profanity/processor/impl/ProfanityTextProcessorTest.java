package me.braydon.profanity.processor.impl;

import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;
import me.braydon.profanity.model.ProfanityList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfanityTextProcessorTest {
    private ProfanityTextProcessor processor;
    private ProfanityList profanityList;

    @BeforeEach
    void setUp() {
        processor = new ProfanityTextProcessor();

        Map<ContentTag, Map<Language, List<String>>> words = new HashMap<>();
        Map<ContentTag, Map<Language, List<String>>> phrases = new HashMap<>();

        words.put(ContentTag.HATE_SPEECH, Map.of(Language.ENGLISH, List.of("nigger", "klan")));
        words.put(ContentTag.SEXUAL, Map.of(Language.ENGLISH, List.of("porn")));
        phrases.put(ContentTag.SELF_HARM, Map.of(Language.ENGLISH, List.of("kill yourself")));
        phrases.put(ContentTag.HATE_SPEECH, Map.of(Language.ENGLISH, List.of("white power")));

        profanityList = new ProfanityList("test", new ArrayList<>(), words, phrases);
    }

    @Test
    void matchesSingleWordWithWordBoundary() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        StringBuilder replacement = new StringBuilder("this is porn content");

        processor.process(profanityList, "this is porn content", replacement, '*', matched, tags, null);

        assertTrue(matched.contains("porn"));
        assertTrue(tags.contains(ContentTag.SEXUAL));
        assertEquals("this is **** content", replacement.toString());
    }

    @Test
    void doesNotMatchSubstringWithoutWordBoundary() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        StringBuilder replacement = new StringBuilder("pronunciation guide");

        processor.process(profanityList, "pronunciation guide", replacement, '*', matched, tags, null);

        assertTrue(matched.isEmpty());
        assertEquals("pronunciation guide", replacement.toString());
    }

    @Test
    void matchesMultiWordPhrase() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        StringBuilder replacement = new StringBuilder("you should kill yourself now");

        processor.process(profanityList, "you should kill yourself now", replacement, '*', matched, tags, null);

        assertTrue(matched.contains("kill yourself"));
        assertTrue(tags.contains(ContentTag.SELF_HARM));
        assertEquals("you should ************* now", replacement.toString());
    }

    @Test
    void matchesPhraseWithFlexibleSpacing() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        StringBuilder replacement = new StringBuilder("white   power");

        processor.process(profanityList, "white   power", replacement, '*', matched, tags, null);

        assertTrue(matched.contains("white power"));
        assertTrue(tags.contains(ContentTag.HATE_SPEECH));
    }

    @Test
    void respectsIgnoredTags() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        StringBuilder replacement = new StringBuilder("kill yourself");

        processor.process(profanityList, "kill yourself", replacement, '*', matched, tags, List.of(ContentTag.SELF_HARM));

        assertTrue(matched.isEmpty());
        assertFalse(tags.contains(ContentTag.SELF_HARM));
        assertEquals("kill yourself", replacement.toString());
    }

    @Test
    void assignsCorrectCategoryTag() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        StringBuilder replacement = new StringBuilder("nigger");

        processor.process(profanityList, "nigger", replacement, '*', matched, tags, null);

        assertTrue(tags.contains(ContentTag.HATE_SPEECH));
        assertFalse(tags.contains(ContentTag.SEXUAL));
    }
}
