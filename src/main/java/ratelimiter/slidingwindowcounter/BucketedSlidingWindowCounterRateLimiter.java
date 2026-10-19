/*
 * Enhanced sliding-window counter: sum small fixed buckets in a circular array.
 * Default bucket size is 5 seconds: a 60-second limit uses 12 previous slots + active.
 * Count the ENTIRE oldest overlapping bucket, rather than assume uniform traffic.
 *
 * Interview critique:
 * - Cost: O(B) time/request to expire/sum B slots, O(U * B) space (sum of per-user B
 *   with overrides). At 60s/5s, B=13. No per-request timestamp objects or growing queue.
 *   A running total with incremental expiry can reduce amortized work but adds bookkeeping.
 * - Concurrency: compute serializes array expiry/check/increment per user. Arrays never
 *   escape; config/time are read inside compute. Same-user/bin contention remains. Updates
 *   overlapping the config read may leave that request on the old revision; next read resets.
 * - Accuracy: conservative, assuming nondecreasing time and no revision reset: no false
 *   admissions relative to exact rolling quota, but old requests can block up to one extra
 *   bucket duration. Error in count depends on bursts; 5s buckets do NOT imply 5% error.
 * - Edges: tied/duplicate calls consume quota; rejected calls do not increment. floorDiv/
 *   floorMod support pre-epoch times, IDs tag reused slots, unsigned age handles huge jumps.
 *   Positive duration must be a multiple of bucket size; require an int-sized slot count.
 *   Long sum and guarded int increments avoid overflow. Config revisions reset the arrays.
 * - Scale: retains idle users and allocates arrays proportional to window/bucket duration;
 *   bound configured sizes, add coordinated eviction, and use shared atomic storage + TTL
 *   across JVMs. Each instance currently enforces only its own traffic.
 * - Alternative: exact sliding log costs one timestamp/accepted request; weighting the
 *   oldest bucket reduces conservative rejection but reintroduces burst undercounting.
 */
package ratelimiter.slidingwindowcounter;

import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimitConfigProvider;
import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class BucketedSlidingWindowCounterRateLimiter implements RateLimiter {
    public static final long DEFAULT_BUCKET_MILLIS = 5_000;

    private final RateLimitConfigProvider configProvider;
    private final Clock clock;
    private final long bucketMillis;
    private final ConcurrentHashMap<String, BucketWindow> windows = new ConcurrentHashMap<>();

    // Parallel arrays: counts[i] belongs to bucketIds[i]. A zero count means an empty slot.
    // Mutable arrays are accessed only inside compute for the owning user.
    private record BucketWindow(RateLimitConfig config, long[] bucketIds, int[] counts) {}

    public BucketedSlidingWindowCounterRateLimiter(RateLimitConfigProvider configProvider, Clock clock) {
        this(configProvider, clock, DEFAULT_BUCKET_MILLIS);
    }

    public BucketedSlidingWindowCounterRateLimiter(RateLimitConfigProvider configProvider, Clock clock,
                                                   long bucketMillis) {
        this.configProvider = Objects.requireNonNull(configProvider, "configProvider");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (bucketMillis <= 0) throw new IllegalArgumentException("Bucket duration must be positive");
        this.bucketMillis = bucketMillis;
    }

    public BucketedSlidingWindowCounterRateLimiter(int maxRequests, long windowMillis) {
        this(maxRequests, windowMillis, Clock.systemUTC());
    }

    public BucketedSlidingWindowCounterRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        this(new InMemoryRateLimitConfigProvider(new RateLimitConfig(maxRequests, windowMillis)), clock);
        slotCount(new RateLimitConfig(maxRequests, windowMillis));
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");

        boolean[] allowed = {false};
        windows.compute(userId, (key, old) -> {
            RateLimitConfig config = Objects.requireNonNull(configProvider.getConfig(key), "config");
            int slots = slotCount(config); // validate before allocating or mutating any state
            long activeBucketId = Math.floorDiv(clock.millis(), bucketMillis);
            BucketWindow window = old == null || old.config() != config
                    ? new BucketWindow(config, new long[slots], new int[slots]) : old;

            // For 60s/5s, retain ages 0..12: current bucket plus the previous 12.
            // Age 12 is partly expired, but keep all its count to avoid underestimating bursts.
            long total = 0;
            for (int i = 0; i < slots; i++) {
                if (window.counts()[i] == 0) continue;
                long age = activeBucketId - window.bucketIds()[i];
                // With nondecreasing time, subtraction represents a nonnegative unsigned age,
                // even when a jump from Long.MIN_VALUE to Long.MAX_VALUE wraps signed long.
                if (Long.compareUnsigned(age, slots - 1L) > 0) {
                    window.counts()[i] = 0;
                } else {
                    total += window.counts()[i];
                }
            }

            if (total < config.maxRequests()) {
                // Modulo wraps around the fixed-size ring. Expiry cleared a previous occupant
                // before this slot can be reused; a still-live occupant has the same bucket ID.
                int index = (int) Math.floorMod(activeBucketId, (long) slots);
                window.bucketIds()[index] = activeBucketId;
                window.counts()[index]++;
                allowed[0] = true;
            }
            return window;
        });
        return allowed[0];
    }

    private int slotCount(RateLimitConfig config) {
        if (config.windowMillis() % bucketMillis != 0) {
            throw new IllegalArgumentException("Window duration must be a multiple of bucket duration");
        }
        long previousBuckets = config.windowMillis() / bucketMillis;
        if (previousBuckets >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Too many buckets for a circular array");
        }
        return (int) previousBuckets + 1; // previous buckets plus active bucket
    }
}
