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
        // TODO: handle alerting of the content to the appropriate parties
    }
}