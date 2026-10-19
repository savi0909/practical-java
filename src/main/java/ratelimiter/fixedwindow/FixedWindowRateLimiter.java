/*
 * Allow each user's configured quota in each epoch-aligned windowMillis interval.
 * For 3 requests / 10 seconds: [0, 10000), [10000, 20000), ... in epoch millis.
 * Reset is lazy: the first request in a new window replaces the previous counter.
 *
 * Interview critique:
 * - Cost: expected O(1) per request, O(U) retained users; no inactive-user eviction.
 * - Concurrency: compute makes the check, increment, and admission decision atomic
 *   per user and publishes immutable state safely. No AtomicInteger needed.
 *   Same-user requests serialize; hash collisions can also cause contention.
 *   Read config and time inside compute so waiting callers cannot install stale
 *   state over a newer admission. Snapshot identity is the configuration revision;
 *   every configure call resets quota lazily, including equal settings and A/B/A.
 *   An overlapping configure may publish after a request reads its snapshot; that
 *   request uses the old revision, and the next request observes the new revision.
 *   Frequent reconfiguration grants fresh quota intentionally; do not use it per call.
 * - Edges: reject invalid limits, null clock, null/blank IDs; repeated calls consume
 *   quota. Counts stop at the limit (no overflow). floorDiv handles pre-epoch times.
 *   Requires a nondecreasing wall clock for strict quotas: a clock correction can
 *   revisit an earlier window and grant fresh quota. Rejected calls consume none.
 *   Empty/blank IDs are invalid; duplicate calls are distinct requests; no tie ordering.
 * - Scale: JVM-local state grows with all seen users; use expiring entries and a
 *   shared atomic store (e.g. Redis script with TTL) for multi-instance enforcement.
 * - Tradeoff: 3 calls at 9.9s plus 3 at 10.0s are legal. Sliding-window log avoids
 *   this boundary burst but stores timestamps; token bucket supports controlled bursts.
 * - Extension: implement RateLimiter with the same config provider/Clock; sliding log
 *   keeps a timestamp deque, sliding counter keeps adjacent counts, token bucket keeps
 *   tokens + last refill time. Token bucket needs an explicit burst-capacity policy.
 */
package ratelimiter.fixedwindow;

import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimitConfigProvider;
import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class FixedWindowRateLimiter implements RateLimiter {
    private final RateLimitConfigProvider configProvider;
    private final Clock clock;
    private final ConcurrentHashMap<String, ConfiguredWindow> windows = new ConcurrentHashMap<>();

    // Keep the revision and its WindowState in one atomic map entry.
    private record ConfiguredWindow(RateLimitConfig config, WindowState state) {}

    /** Creates a limiter with user-specific configuration and an injectable clock. */
    public FixedWindowRateLimiter(RateLimitConfigProvider configProvider, Clock clock) {
        this.configProvider = Objects.requireNonNull(configProvider, "configProvider");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public FixedWindowRateLimiter(int maxRequests, long windowMillis) {
        this(maxRequests, windowMillis, Clock.systemUTC());
    }

    public FixedWindowRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        this(new InMemoryRateLimitConfigProvider(new RateLimitConfig(maxRequests, windowMillis)), clock);
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        // This holder belongs only to this call; compute invokes its callback synchronously.
        boolean[] allowed = {false};
        windows.compute(userId, (key, old) -> {
            RateLimitConfig config = Objects.requireNonNull(configProvider.getConfig(key), "config");
            long windowId = Math.floorDiv(clock.millis(), config.windowMillis());
            if (old == null || old.config() != config || old.state().windowId() != windowId) {
                allowed[0] = true;
                return new ConfiguredWindow(config, new WindowState(windowId, 1));
            }
            if (old.state().requestCount() < config.maxRequests()) {
                allowed[0] = true;
                return new ConfiguredWindow(config,
                        new WindowState(windowId, old.state().requestCount() + 1));
            }
            return old;
        });
        return allowed[0];
    }
}
