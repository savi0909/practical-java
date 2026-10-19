package ratelimiter;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/** Share one manager across callers in one JVM; each user gets their own limiter. */
public final class RateLimiterManager implements RateLimiter {
    // Stable identity matters: replacing a limiter would give that user fresh quota.
    // Inactive entries remain stored; cleanup and changing live policies are deferred.
    private final ConcurrentHashMap<String, RateLimiterImpl> limiters = new ConcurrentHashMap<>();
    private final int defaultMaxRequests;
    private final long defaultWindowMillis;
    private final LongSupplier currentTimeMillis;

    public RateLimiterManager() {
        this(10, 60_000);
    }

    public RateLimiterManager(int defaultMaxRequests, long defaultWindowMillis) {
        this(defaultMaxRequests, defaultWindowMillis, System::currentTimeMillis);
    }

    RateLimiterManager(int defaultMaxRequests, long defaultWindowMillis, LongSupplier currentTimeMillis) {
        if (defaultMaxRequests <= 0 || defaultWindowMillis <= 0) {
            throw new IllegalArgumentException("Limit and window duration must be positive");
        }
        this.defaultMaxRequests = defaultMaxRequests;
        this.defaultWindowMillis = defaultWindowMillis;
        this.currentTimeMillis = Objects.requireNonNull(currentTimeMillis, "currentTimeMillis");
    }

    /** Configure a user before their first request; duplicate registration is rejected. */
    public void registerUser(String userId, int maxRequests, long windowMillis) {
        Objects.requireNonNull(userId, "userId");
        RateLimiterImpl limiter = new RateLimiterImpl(maxRequests, windowMillis, currentTimeMillis);
        // Atomic insertion: registration cannot replace a limiter created by a
        // concurrent request or another registration and silently reset its quota.
        if (limiters.putIfAbsent(userId, limiter) != null) {
            throw new IllegalStateException("User already has a limiter: " + userId);
        }
    }

    @Override
    public boolean isAllowed(String userId) {
        Objects.requireNonNull(userId, "userId");
        // Concurrent first requests reuse ONE stored limiter for this user.
        // Keep the map callback short: it only constructs the default limiter.
        RateLimiterImpl limiter = limiters.computeIfAbsent(userId,
                key -> new RateLimiterImpl(defaultMaxRequests, defaultWindowMillis, currentTimeMillis));

        // Admission happens OUTSIDE the map callback. A busy user's lock therefore
        // does not hold a map-bin lock while another user's admission runs.
        return limiter.isAllowed();
    }
}
