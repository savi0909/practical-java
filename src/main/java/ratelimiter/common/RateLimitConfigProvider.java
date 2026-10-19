package ratelimiter.common;

/** Supplies thread-safe, immutable configuration snapshots independently of the algorithm. */
public interface RateLimitConfigProvider {
    /**
     * Returns a non-null snapshot for a non-null, nonblank user ID.
     * Return the same instance until reconfigured, then publish a fresh instance,
     * even if the values are unchanged. Instance identity acts as a revision token.
     * Implementations must support concurrent reads and updates.
     */
    RateLimitConfig getConfig(String userId);
}
