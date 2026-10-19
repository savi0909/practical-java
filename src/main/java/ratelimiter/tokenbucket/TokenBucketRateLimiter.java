/*
 * Step 1: per-tenant token bucket. Start full; add 2 whole tokens per completed
 * refill period (default 5 seconds), cap at capacity, consume 1 per accepted call.
 *
 * Interview critique:
 * - Expected O(1) admission and O(1) state/tenant; O(U) total retained state.
 * - compute atomically refills/checks/consumes and safely publishes each bucket.
 *   All mutable bucket access stays inside compute; no AtomicInteger or extra lock.
 *   Same-tenant calls serialize; colliding map bins may also contend. No fairness.
 * - Null/blank IDs are invalid; duplicate/equal-time calls are distinct. Capacity
 *   1 works; long arithmetic and clamping before multiplication avoid token overflow.
 *   Preserve partial periods; discard excess refill at capacity. Config snapshot
 *   identity resets to full, including equal-value updates; in-flight old reads
 *   may finish under their observed revision. Rejections consume no tokens.
 * - Clock must be nondecreasing for elapsed-time guarantees. Rollback freezes refill
 *   until catch-up; forward wall-clock jumps grant tokens. Gaps beyond Long.MAX_VALUE
 *   milliseconds throw rather than silently overflowing. A monotonic ticker is a
 *   production follow-up, at the cost of changing the existing Clock abstraction.
 * - A million tenants need heap sizing for keys/map nodes/buckets; entries never
 *   expire here. Add safe idle eviction for churn; use shared atomic state for
 *   multiple JVMs. Lazy refill touches only the requesting tenant, with no scanner.
 * - Alternative: continuous fractional refill smooths admission but changes the
 *   policy (one token at 2.5s instead of two at 5s) and needs careful arithmetic.
 */
package ratelimiter.tokenbucket;

import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimitConfigProvider;
import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class TokenBucketRateLimiter implements RateLimiter {
    private static final long REFILL_PERIOD_MILLIS = 5_000;
    private static final int REFILL_TOKENS = 2;

    private final RateLimitConfigProvider configProvider;
    private final Clock clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final class Bucket {
        private final RateLimitConfig config;
        private int tokens;
        private long lastRefillMillis;

        private Bucket(RateLimitConfig config, long now) {
            this.config = config;
            tokens = config.maxRequests();
            lastRefillMillis = now;
        }
    }

    public TokenBucketRateLimiter(int capacity) {
        this(capacity, Clock.systemUTC());
    }

    public TokenBucketRateLimiter(int capacity, Clock clock) {
        this(new InMemoryRateLimitConfigProvider(
                new RateLimitConfig(capacity, REFILL_PERIOD_MILLIS)), clock);
    }

    /** maxRequests is burst capacity; windowMillis is the period for adding 2 tokens. */
    public TokenBucketRateLimiter(RateLimitConfigProvider configProvider, Clock clock) {
        this.configProvider = Objects.requireNonNull(configProvider, "configProvider");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");

        boolean[] allowed = {false};
        buckets.compute(userId, (key, bucket) -> {
            var config = Objects.requireNonNull(configProvider.getConfig(key), "config");
            long now = clock.millis();
            if (bucket == null || bucket.config != config) {
                bucket = new Bucket(config, now);
            }
            if (now > bucket.lastRefillMillis) {
                long elapsed = Math.subtractExact(now, bucket.lastRefillMillis);
                long periods = elapsed / config.windowMillis();
                if (periods > 0) {
                    long missing = (long) config.maxRequests() - bucket.tokens;
                    long added = Math.min(periods, (missing + REFILL_TOKENS - 1) / REFILL_TOKENS)
                            * REFILL_TOKENS;
                    bucket.tokens = (int) Math.min(config.maxRequests(), bucket.tokens + added);
                    // Advance even at capacity; preserve only the unfinished period.
                    bucket.lastRefillMillis = now - elapsed % config.windowMillis();
                }
            }
            if (bucket.tokens > 0) {
                bucket.tokens--;
                allowed[0] = true;
            }
            return bucket;
        });
        return allowed[0];
    }
}
