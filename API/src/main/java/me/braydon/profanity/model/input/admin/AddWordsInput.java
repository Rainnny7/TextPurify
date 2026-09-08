package me.braydon.profanity.model.input.admin;

import lombok.Getter;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;

import java.util.List;

/**
 * Input for adding words or phrases to the profanity list.
 *
 * @author Braydon
 */
@Getter
public final class AddWordsInput {
    private ContentTag tag;
    private Language language;
    private List<String> terms;
    private EntryType type;

    public enum EntryType {
        WORD, PHRASE
    }

    public boolean isMalformed() {
        return tag == null || language == null || terms == null || terms.isEmpty();
    }
}
