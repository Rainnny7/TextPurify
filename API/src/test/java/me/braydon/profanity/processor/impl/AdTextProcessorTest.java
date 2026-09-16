package me.braydon.profanity.processor.impl;

import me.braydon.profanity.common.ContentTag;
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

class AdTextProcessorTest {
    private AdTextProcessor processor;
    private ProfanityList profanityList;

    @BeforeEach
    void setUp() {
        processor = new AdTextProcessor();
        profanityList = new ProfanityList(
                "test",
                List.of("wildprison.net", "store.wildnetwork.net"),
                new HashMap<>(),
                new HashMap<>()
        );
    }

    @Test
    void censorsUnlistedDomains() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        StringBuilder replacement = new StringBuilder("join evil-site.com now");

        processor.process(profanityList, "join evil-site.com now", replacement, '*', matched, tags, null);

        assertTrue(matched.contains("evil-site.com"));
        assertTrue(tags.contains(ContentTag.ADVERTISEMENT));
        assertEquals("join ************* now", replacement.toString());
    }

    @Test
    void skipsExactWhitelistedDomain() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        String content = "use store.wildnetwork.net for deals";
        StringBuilder replacement = new StringBuilder(content);

        processor.process(profanityList, content, replacement, '*', matched, tags, null);

        assertTrue(matched.isEmpty());
        assertFalse(tags.contains(ContentTag.ADVERTISEMENT));
        assertEquals(content, replacement.toString());
    }

    @Test
    void skipsSubdomainsWhenParentDomainIsWhitelisted() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        String content = "use store.wildprison.net for deals";
        StringBuilder replacement = new StringBuilder(content);

        processor.process(profanityList, content, replacement, '*', matched, tags, null);

        assertTrue(matched.isEmpty());
        assertFalse(tags.contains(ContentTag.ADVERTISEMENT));
        assertEquals(content, replacement.toString());
    }

    @Test
    void censorsMixedWhitelistedAndUnlistedDomains() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        String content = "visit store.wildprison.net or evil-site.com";
        StringBuilder replacement = new StringBuilder(content);

        processor.process(profanityList, content, replacement, '*', matched, tags, null);

        assertEquals(List.of("evil-site.com"), matched);
        assertTrue(tags.contains(ContentTag.ADVERTISEMENT));
        assertEquals("visit store.wildprison.net or *************", replacement.toString());
    }

    @Test
    void skipsWhitelistedUrlWithProtocolAndPath() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        String content = "visit https://www.store.wildnetwork.net/sale";
        StringBuilder replacement = new StringBuilder(content);

        processor.process(profanityList, content, replacement, '*', matched, tags, null);

        assertTrue(matched.isEmpty());
        assertEquals(content, replacement.toString());
    }

    @Test
    void respectsIgnoredTags() {
        List<String> matched = new ArrayList<>();
        Set<ContentTag> tags = EnumSet.noneOf(ContentTag.class);
        String content = "visit evil-site.com";
        StringBuilder replacement = new StringBuilder(content);

        processor.process(profanityList, content, replacement, '*', matched, tags, List.of(ContentTag.ADVERTISEMENT));

        assertTrue(matched.isEmpty());
        assertEquals(content, replacement.toString());
    }
}
