package me.braydon.profanity.model.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Response for admin mutation actions.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
public final class AdminActionResponse {
    private final boolean success;
    @NonNull private final String message;
}
