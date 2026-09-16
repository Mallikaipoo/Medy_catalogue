package in.techgeneza.medycatalog.modules.auth.application;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public void check(String key) {
        Instant now = Instant.now();
        Window window = windows.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiresAt.isBefore(now)) {
                return new Window(now.plus(WINDOW));
            }
            return existing;
        });
        if (window.count.incrementAndGet() > MAX_ATTEMPTS) {
            throw in.techgeneza.medycatalog.common.exception.ApiException.tooManyRequests(
                    "Too many attempts. Please wait a few minutes and try again.");
        }
    }

    private static final class Window {
        private final Instant expiresAt;
        private final AtomicInteger count = new AtomicInteger();

        private Window(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
