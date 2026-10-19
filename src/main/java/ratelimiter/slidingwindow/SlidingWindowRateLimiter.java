/*
 * Exact sliding-window log: admit at most maxRequests in (now - windowMillis, now].
 * Store only accepted timestamps; an entry expires at exactly windowMillis age.
 *
 * Interview critique:
 * - Cost: expected amortized O(1) per request; O(L) worst case when expiring a full
 *   log, O(U + sum of retained quotas) space. Each timestamp is added/removed once.
 *   ArrayDeque backing arrays retain their peak capacity until the state is replaced.
 * - Concurrency: compute serializes expiry/check/append per user; the mutable deque
 *   never escapes that callback. Config/time are read inside compute. Same-user calls
 *   and colliding map bins can contend; unrelated bins can progress concurrently.
 *   Snapshot identity is a revision: any configure call resets that user's log lazily.
 *   An overlapping update can leave an in-flight call on the old snapshot; the next
 *   call observing the new snapshot resets. Frequent updates intentionally grant quota.
 * - Edges: duplicate calls/tied timestamps each consume quota; rejected calls do not.
 *   Null/blank IDs and invalid settings fail; expiry handles negative epoch times and
 *   guards subtraction underflow. Requires a nondecreasing clock to keep logs ordered;
 *   use a monotonic elapsed-time source in production if wall-clock rollback matters.
 * - Scale: JVM-local logs retain inactive users and cost one boxed timestamp per
 *   accepted request; use eviction and Redis sorted sets + atomic script + TTL across JVMs.
 * - Alternative: a sliding-window counter uses O(1) state/user but approximates the
 *   rolling count; token bucket offers O(1) state with different burst semantics.
 */
package ratelimiter.slidingwindow;

import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimitConfigProvider;
import ratelimiter.common.RateLimiter;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class SlidingWindowRateLimiter implements RateLimiter {
    private final RateLimitConfigProvider configProvider;
    private final Clock clock;
    // User ID -> configuration revision and that user's accepted request timestamps.
    private final ConcurrentHashMap<String, RequestLog> logs = new ConcurrentHashMap<>();

    // acceptedAt is ordered oldest (front) to newest (back); its size is the used quota.
    // The deque is mutable, but every access is confined to compute for its user.
    private record RequestLog(RateLimitConfig config, ArrayDeque<Long> acceptedAt) {}

    public SlidingWindowRateLimiter(RateLimitConfigProvider configProvider, Clock clock) {
        this.configProvider = Objects.requireNonNull(configProvider, "configProvider");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public SlidingWindowRateLimiter(int maxRequests, long windowMillis) {
        this(maxRequests, windowMillis, Clock.systemUTC());
    }

    public SlidingWindowRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        this(new InMemoryRateLimitConfigProvider(new RateLimitConfig(maxRequests, windowMillis)), clock);
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");

        // A lambda cannot reassign a captured local variable, so use a per-call holder.
        // compute runs synchronously; this holder is never shared between requests.
        boolean[] allowed = {false};

        // Expire, check, and append atomically for this user to prevent over-admission.
        logs.compute(userId, (key, old) -> {
            RateLimitConfig config = Objects.requireNonNull(configProvider.getConfig(key), "config");
            long now = clock.millis();

            // First request or new config revision: start with a fresh quota.
            // Compare identity (!=): even equal settings can represent a new revision.
            RequestLog log = old == null || old.config() != config
                    ? new RequestLog(config, new ArrayDeque<>()) : old;

            // Remove requests at or before the rolling window's exclusive left boundary.
            // Example: now=1500, duration=1000 -> remove timestamps <=500; keep (500,1500].
            // If the cutoff is below Long.MIN_VALUE, no representable timestamp has expired.
            if (now >= Long.MIN_VALUE + config.windowMillis()) {
                long cutoff = now - config.windowMillis();
                while (!log.acceptedAt().isEmpty() && log.acceptedAt().peekFirst() <= cutoff) {
                    log.acceptedAt().removeFirst();
                }
            }

            // Only accepted calls consume quota; rejections never enter the deque.
            if (log.acceptedAt().size() < config.maxRequests()) {
                log.acceptedAt().addLast(now);
                allowed[0] = true;
            }
            // compute stores this user's log; the holder carries the admission result.
            return log;
        });
        return allowed[0];
    }
}
