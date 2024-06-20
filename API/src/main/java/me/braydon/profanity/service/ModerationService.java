package me.braydon.profanity.service;

import lombok.NonNull;
import me.braydon.profanity.model.response.ContentProcessResponse;
import org.springframework.stereotype.Service;

/**
 * This service is responsible for moderating text
 * content and reporting it to the appropriate parties.
 *
 * @author Braydon
 */
@Service
public final class ModerationService {
    public void handleAlerts(@NonNull ContentProcessResponse response) {
        // Likely safe content, no need to alert anyone
        if (response.getScore() < 0.6D) {
            return;
        }
        // TODO: handle alerting of the content to the appropriate parties
    }
}