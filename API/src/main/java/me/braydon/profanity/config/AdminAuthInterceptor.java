package me.braydon.profanity.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import me.braydon.profanity.exception.impl.ServiceUnavailableException;
import me.braydon.profanity.exception.impl.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Interceptor to authenticate admin API requests.
 *
 * @author Braydon
 */
@Component
public final class AdminAuthInterceptor implements HandlerInterceptor {
    public static final String ADMIN_API_KEY_HEADER = "X-Admin-Api-Key";

    @NonNull private final String adminApiKey;

    public AdminAuthInterceptor(@Value("${admin.api-key:}") @NonNull String adminApiKey) {
        this.adminApiKey = adminApiKey;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (adminApiKey.isBlank()) {
            throw new ServiceUnavailableException("Admin API is disabled.");
        }

        String providedKey = request.getHeader(ADMIN_API_KEY_HEADER);
        if (providedKey == null || !keysMatch(adminApiKey, providedKey)) {
            throw new UnauthorizedException("Invalid or missing admin API key.");
        }

        return true;
    }

    private static boolean keysMatch(@NonNull String expected, @NonNull String provided) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }
}
