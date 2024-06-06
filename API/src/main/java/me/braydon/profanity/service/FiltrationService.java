package me.braydon.profanity.service;

import lombok.NonNull;
import me.braydon.profanity.model.input.ContentProcessInput;
import me.braydon.profanity.model.response.ContentProcessResponse;
import me.braydon.profanity.repository.ProfanityListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * This service is responsible
 * for filtering text content.
 *
 * @author Braydon
 */
@Service
public final class FiltrationService {
    @NonNull private final ProfanityListRepository profanityListRepository;

    @Autowired
    public FiltrationService(@NonNull ProfanityListRepository profanityListRepository) {
        this.profanityListRepository = profanityListRepository;
    }

    @NonNull
    public ContentProcessResponse process(@NonNull ContentProcessInput input) {
        return new ContentProcessResponse(input.getContent(), new ArrayList<>(), new ArrayList<>(), 0D);
    }
}