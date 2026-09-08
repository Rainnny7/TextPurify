package me.braydon.profanity.exception.impl;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is raised when a service is unavailable.
 *
 * @author Braydon
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public final class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(@NonNull String message) {
        super(message);
    }
}
