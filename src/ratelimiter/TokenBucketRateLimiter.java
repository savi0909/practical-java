package ratelimiter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Per-client, in-memory token buckets for callers sharing one instance in one JVM. */
public final class TokenBucketRateLimiter implements RateLimiter {
    // The instance monitor protects the map and both mutable fields of every bucket.
    private final Map<String, Bucket> buckets = new HashMap<>();
    private final int capacity;
    private final double tokensPerSecond;
    private final LongSupplier nanoTime;

    public TokenBucketRateLimiter(int capacity, double tokensPerSecond) {
        this(capacity, tokensPerSecond, System::nanoTime);
    }

    TokenBucketRateLimiter(int capacity, double tokensPerSecond, LongSupplier nanoTime) {
        if (capacity <= 0 || !Double.isFinite(tokensPerSecond) || tokensPerSecond <= 0) {
            throw new IllegalArgumentException("Capacity and finite refill rate must be positive");
        }
        this.capacity = capacity;
        this.tokensPerSecond = tokensPerSecond;
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    }

    @Override
    public synchronized boolean isAllowed(String userId) {
        Objects.requireNonNull(userId, "userId");
        long now = nanoTime.getAsLong();
        Bucket bucket = buckets.get(userId);
        if (bucket == null) {
            // New clients start full, allowing an immediate burst up to capacity.
            bucket = new Bucket(capacity, now);
            buckets.put(userId, bucket);
        }

        // nanoTime measures elapsed time, so wall-clock corrections do not refill us.
        // Subtraction also handles nanoTime wraparound for gaps below 2^63 ns (~292 years).
        long elapsed = now - bucket.lastRefillNanos;
        if (elapsed > 0) {
            // Keep fractions: two half-token intervals must eventually yield one token.
            double added = (elapsed / 1_000_000_000.0) * tokensPerSecond;
            bucket.tokens = Math.min(capacity, bucket.tokens + added);
            // Advance even when full: unused refill beyond capacity must be discarded.
            bucket.lastRefillNanos = now;
        }
        if (bucket.tokens < 1.0) {
            return false;
        }
        // Refill, check, and consume share one lock; no two calls take the same token.
        // This simple baseline serializes different clients too.
        bucket.tokens -= 1.0;
        return true;
    }

    private static final class Bucket {
        private double tokens;
        private long lastRefillNanos;

        private Bucket(int capacity, long now) {
            tokens = capacity;
            lastRefillNanos = now;
        }
    }
}
