package me.braydon.profanity.model.input.admin;

import lombok.Getter;

/**
 * Input for managing a whitelisted link.
 *
 * @author Braydon
 */
@Getter
public final class WhitelistedLinkInput {
    private String link;

    public boolean isMalformed() {
        return link == null || link.isBlank();
    }
}
