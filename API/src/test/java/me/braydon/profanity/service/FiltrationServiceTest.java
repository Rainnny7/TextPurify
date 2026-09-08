package me.braydon.profanity.service;

import me.braydon.profanity.common.ContentTag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiltrationServiceTest {
    @Test
    void scoreIsZeroWhenNoMatches() {
        assertEquals(0D, FiltrationService.calculateScore("hello world", List.of(), Set.of()));
    }

    @Test
    void scoreReflectsMatchedCharacterCoverage() {
        String content = "Check out http://spam.com for deals";
        List<String> matched = List.of("http://spam.com");
        Set<ContentTag> tags = Set.of(ContentTag.ADVERTISEMENT);

        assertEquals(0.429D, FiltrationService.calculateScore(content, matched, tags));
    }

    @Test
    void scoreIsCappedAtOne() {
        assertEquals(1D, FiltrationService.calculateScore("bad", List.of("bad"), Set.of(ContentTag.VULGAR)));
    }

    @Test
    void hateSpeechAndSelfHarmBoostScore() {
        String content = "you are bad";
        List<String> matched = List.of("bad");
        Set<ContentTag> tags = new LinkedHashSet<>();
        tags.add(ContentTag.HATE_SPEECH);

        assertEquals(0.409D, FiltrationService.calculateScore(content, matched, tags));
    }

    @Test
    void boostedScoreIsStillCappedAtOne() {
        assertEquals(1D, FiltrationService.calculateScore(
                "slur",
                List.of("slur"),
                Set.of(ContentTag.HATE_SPEECH)
        ));
    }
}
