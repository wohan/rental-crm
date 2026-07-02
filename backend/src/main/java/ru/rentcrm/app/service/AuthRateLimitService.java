package ru.rentcrm.app.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.exception.ApiException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthRateLimitService {
    private static final int MAX_ATTEMPTS = 8;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public void check(String email, String ip) {
        String key = normalize(email) + "|" + normalize(ip);
        Attempts state = attempts.compute(key, (ignored, current) -> {
            Instant now = Instant.now();
            if (current == null || current.windowStarted.plus(WINDOW).isBefore(now)) {
                return new Attempts(1, now);
            }
            return new Attempts(current.count + 1, current.windowStarted);
        });
        if (state.count > MAX_ATTEMPTS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Слишком много попыток входа. Попробуйте позже.");
        }
    }

    public void reset(String email, String ip) {
        attempts.remove(normalize(email) + "|" + normalize(ip));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private record Attempts(int count, Instant windowStarted) {
    }
}
