package ratelimiter.common;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Per-user overrides with a fixed default for users without an override. */
public final class InMemoryRateLimitConfigProvider implements RateLimitConfigProvider {
    private final RateLimitConfig defaultConfig;
    private final ConcurrentHashMap<String, RateLimitConfig> configs = new ConcurrentHashMap<>();

    public InMemoryRateLimitConfigProvider(RateLimitConfig defaultConfig) {
        this.defaultConfig = Objects.requireNonNull(defaultConfig, "defaultConfig");
    }

    /** Atomically publishes a fresh revision; the next request observing it resets its quota. */
    public void configure(String userId, RateLimitConfig config) {
        validateUserId(userId);
        Objects.requireNonNull(config, "config");
        // Copy even when the caller passes a previously used record (A -> B -> A).
        configs.put(userId, new RateLimitConfig(config.maxRequests(), config.windowMillis()));
    }

    @Override
    public RateLimitConfig getConfig(String userId) {
        validateUserId(userId);
        return configs.getOrDefault(userId, defaultConfig);
    }

    static void validateUserId(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
    }
}
