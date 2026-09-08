package me.braydon.profanity.controller;

import lombok.NonNull;
import me.braydon.profanity.common.ContentTag;
import me.braydon.profanity.common.Language;
import me.braydon.profanity.exception.impl.BadRequestException;
import me.braydon.profanity.model.input.admin.AddWordsInput;
import me.braydon.profanity.model.input.admin.ProfanityListInput;
import me.braydon.profanity.model.input.admin.RemoveWordInput;
import me.braydon.profanity.model.input.admin.WhitelistedLinkInput;
import me.braydon.profanity.model.response.admin.AdminActionResponse;
import me.braydon.profanity.model.response.admin.AdminStatsResponse;
import me.braydon.profanity.model.response.admin.ProfanityListResponse;
import me.braydon.profanity.service.ProfanityListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for managing the profanity filter list.
 *
 * @author Braydon
 */
@RestController
@RequestMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public final class AdminController {
    @NonNull private final ProfanityListService profanityListService;

    @Autowired
    public AdminController(@NonNull ProfanityListService profanityListService) {
        this.profanityListService = profanityListService;
    }

    @GetMapping("/list") @NonNull
    public ResponseEntity<ProfanityListResponse> getList() {
        return ResponseEntity.ok(profanityListService.getList());
    }

    @PutMapping("/list") @NonNull
    public ResponseEntity<ProfanityListResponse> replaceList(@RequestBody ProfanityListInput input) {
        if (input == null || input.isMalformed()) {
            throw new BadRequestException("Missing or malformed input.");
        }
        return ResponseEntity.ok(profanityListService.replaceList(input));
    }

    @PostMapping("/list/reload") @NonNull
    public ResponseEntity<ProfanityListResponse> reloadList() {
        return ResponseEntity.ok(profanityListService.reloadFromDatabase());
    }

    @PostMapping("/list/reseed") @NonNull
    public ResponseEntity<ProfanityListResponse> reseedList() {
        return ResponseEntity.ok(profanityListService.reseedFromGitHub());
    }

    @GetMapping("/words") @NonNull
    public ResponseEntity<List<String>> getWords(@RequestParam @NonNull ContentTag tag,
                                                 @RequestParam @NonNull Language lang,
                                                 @RequestParam(required = false) AddWordsInput.EntryType type) {
        return ResponseEntity.ok(profanityListService.getWords(tag, lang, type));
    }

    @PostMapping("/words") @NonNull
    public ResponseEntity<AdminActionResponse> addWords(@RequestBody AddWordsInput input) {
        if (input == null || input.isMalformed()) {
            throw new BadRequestException("Missing or malformed input.");
        }
        profanityListService.addWords(input);
        return ResponseEntity.ok(new AdminActionResponse(true, "Terms added successfully."));
    }

    @DeleteMapping("/words") @NonNull
    public ResponseEntity<AdminActionResponse> removeWord(@RequestBody RemoveWordInput input) {
        if (input == null || input.isMalformed()) {
            throw new BadRequestException("Missing or malformed input.");
        }
        profanityListService.removeWord(input);
        return ResponseEntity.ok(new AdminActionResponse(true, "Term removed successfully."));
    }

    @GetMapping("/whitelisted-links") @NonNull
    public ResponseEntity<List<String>> getWhitelistedLinks() {
        return ResponseEntity.ok(profanityListService.getWhitelistedLinks());
    }

    @PostMapping("/whitelisted-links") @NonNull
    public ResponseEntity<AdminActionResponse> addWhitelistedLink(@RequestBody WhitelistedLinkInput input) {
        if (input == null || input.isMalformed()) {
            throw new BadRequestException("Missing or malformed input.");
        }
        profanityListService.addWhitelistedLink(input.getLink());
        return ResponseEntity.ok(new AdminActionResponse(true, "Link added successfully."));
    }

    @DeleteMapping("/whitelisted-links") @NonNull
    public ResponseEntity<AdminActionResponse> removeWhitelistedLink(@RequestBody WhitelistedLinkInput input) {
        if (input == null || input.isMalformed()) {
            throw new BadRequestException("Missing or malformed input.");
        }
        profanityListService.removeWhitelistedLink(input.getLink());
        return ResponseEntity.ok(new AdminActionResponse(true, "Link removed successfully."));
    }

    @GetMapping("/stats") @NonNull
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(profanityListService.getStats());
    }
}
