/*
 * Basic sliding-window counter: currentCount + previousCount * remainingFraction.
 * Admit only when this estimate INCLUDING the incoming request is <= maxRequests.
 *
 * Interview critique:
 * - Cost: expected O(1) time/request and O(U) retained users, assuming O(1) config lookup.
 *   Immutable state is replaced per call. Exact weighted comparison uses long products
 *   normally, with a bounded-size BigInteger fallback for extreme durations/limits.
 * - Concurrency: compute atomically rolls, estimates, and admits per user. Config/time
 *   are read inside it; immutable counters are safely published. Same-user/bin contention
 *   remains. A config update overlapping the read can affect only subsequent admissions.
 * - Edges: quota and duration must be positive; null/blank IDs fail; tied/duplicate calls
 *   consume separate quota; rejections do not increment. Adjacent windows carry current
 *   into previous; skipped windows discard both. floorDiv/floorMod support negative time.
 *   Exact arithmetic avoids fractional threshold rounding and count/product overflow.
 * - Accuracy: assumes uniform traffic in the previous fixed window. Boundary bursts can
 *   cause either false admissions or false rejections; this is NOT an exact rolling quota.
 *   Nondecreasing time is required. Reconfiguration (even equal values) resets quota.
 * - Scale: entries never expire; add coordinated eviction and a shared atomic store with
 *   TTL for multiple JVMs. A bounded counter is cheaper than keeping every timestamp.
 * - Alternative: finer buckets localize uncertainty to the oldest bucket, costing O(B)
 *   counters/user; the exact sliding log costs O(L) timestamps/user but removes estimation.
 */
package ratelimiter.slidingwindowcounter;

import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimitConfigProvider;
import ratelimiter.common.RateLimiter;

import java.math.BigInteger;
import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class SlidingWindowCounterRateLimiter implements RateLimiter {
    private final RateLimitConfigProvider configProvider;
    private final Clock clock;
    private final ConcurrentHashMap<String, CounterWindow> windows = new ConcurrentHashMap<>();

    private record CounterWindow(RateLimitConfig config, long windowId,
                                 int previousCount, int currentCount) {}

    public SlidingWindowCounterRateLimiter(RateLimitConfigProvider configProvider, Clock clock) {
        this.configProvider = Objects.requireNonNull(configProvider, "configProvider");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public SlidingWindowCounterRateLimiter(int maxRequests, long windowMillis) {
        this(maxRequests, windowMillis, Clock.systemUTC());
    }

    public SlidingWindowCounterRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        this(new InMemoryRateLimitConfigProvider(new RateLimitConfig(maxRequests, windowMillis)), clock);
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");

        boolean[] allowed = {false};
        windows.compute(userId, (key, old) -> {
            RateLimitConfig config = Objects.requireNonNull(configProvider.getConfig(key), "config");
            long now = clock.millis();
            long windowId = Math.floorDiv(now, config.windowMillis());
            int previousCount = 0;
            int currentCount = 0;

            // Preserve counters only for the same configuration revision.
            if (old != null && old.config() == config) {
                if (old.windowId() == windowId) {
                    previousCount = old.previousCount();
                    currentCount = old.currentCount();
                } else if (old.windowId() != Long.MAX_VALUE && old.windowId() + 1 == windowId) {
                    // Move forward exactly one interval: the old current is now previous.
                    previousCount = old.currentCount();
                }
                // Otherwise at least one whole empty interval separates us: both start at 0.
            }

            long elapsed = Math.floorMod(now, config.windowMillis());
            long remaining = config.windowMillis() - elapsed;
            // Estimate = current + previous * remaining/windowMillis.
            // Include the candidate request; merely checking estimate < limit can overshoot.
            if (fitsIncludingRequest(previousCount, currentCount, remaining, config)) {
                currentCount++;
                allowed[0] = true;
            }
            // Save rollover even when rejected, so the next interval carries the right count.
            return new CounterWindow(config, windowId, previousCount, currentCount);
        });
        return allowed[0];
    }

    private static boolean fitsIncludingRequest(int previous, int current, long remaining,
                                                RateLimitConfig config) {
        long capacityAfterRequest = (long) config.maxRequests() - current - 1;
        if (capacityAfterRequest < 0) return false;
        // Equivalent to previous * remaining/windowMillis <= limit - current - 1.
        // Cross-multiply exactly rather than lose fractional precision through division.
        try {
            return Math.multiplyExact((long) previous, remaining)
                    <= Math.multiplyExact(capacityAfterRequest, config.windowMillis());
        } catch (ArithmeticException overflow) {
            return BigInteger.valueOf(previous).multiply(BigInteger.valueOf(remaining))
                    .compareTo(BigInteger.valueOf(capacityAfterRequest)
                            .multiply(BigInteger.valueOf(config.windowMillis()))) <= 0;
        }
    }
}
