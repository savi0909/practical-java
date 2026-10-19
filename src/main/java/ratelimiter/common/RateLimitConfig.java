package ratelimiter.common;

/** Immutable quota settings; request counters belong to the algorithm. */
public record RateLimitConfig(int maxRequests, long windowMillis) {
    public RateLimitConfig {
        if (maxRequests <= 0 || windowMillis <= 0) {
            throw new IllegalArgumentException("Limit and window duration must be positive");
        }
    }
}
