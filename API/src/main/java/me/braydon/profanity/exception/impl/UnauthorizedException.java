package me.braydon.profanity.exception.impl;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is raised when a request is not authorized.
 *
 * @author Braydon
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public final class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(@NonNull String message) {
        super(message);
    }
}
