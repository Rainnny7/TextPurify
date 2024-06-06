package me.braydon.profanity.controller;

import lombok.NonNull;
import me.braydon.profanity.exception.impl.BadRequestException;
import me.braydon.profanity.model.input.ContentProcessInput;
import me.braydon.profanity.model.response.ContentProcessResponse;
import me.braydon.profanity.service.FiltrationService;
import me.braydon.profanity.service.ModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Braydon
 */
@RestController
@RequestMapping(value = "/content", produces = MediaType.APPLICATION_JSON_VALUE)
public final class ContentController {
    /**
     * The filtration service to use.
     */
    @NonNull private final FiltrationService filtrationService;

    /**
     * The moderation service to use.
     */
    @NonNull private final ModerationService moderationService;

    @Autowired
    public ContentController(@NonNull FiltrationService filtrationService, @NonNull ModerationService moderationService) {
        this.filtrationService = filtrationService;
        this.moderationService = moderationService;
    }

    @PostMapping(path = "/process") @ResponseBody @NonNull
    public ResponseEntity<ContentProcessResponse> process(ContentProcessInput input) throws BadRequestException {
        if (input == null || (input.isMalformed())) { // Validate the input
            throw new BadRequestException("Missing or malformed input.");
        }
        ContentProcessResponse response = filtrationService.process(input); // Filter the content
        moderationService.handleAlerts(response); // Handle moderation
        return ResponseEntity.ok(response);
    }
}