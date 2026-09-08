package me.braydon.profanity.model.input.admin;

import lombok.Getter;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;

/**
 * Input for removing a word or phrase from the profanity list.
 *
 * @author Braydon
 */
@Getter
public final class RemoveWordInput {
    private ContentTag tag;
    private Language language;
    private String term;
    private AddWordsInput.EntryType type;

    public boolean isMalformed() {
        return tag == null || language == null || term == null || term.isBlank();
    }
}
