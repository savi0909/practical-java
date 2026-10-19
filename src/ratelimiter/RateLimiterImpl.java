package ratelimiter;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/** One user's fixed-window state and configuration; reused for all of their calls. */
public final class RateLimiterImpl {
    private final int maxRequests;
    private final long windowMillis;
    private final LongSupplier currentTimeMillis;

    // Each user owns a separate lock; the default nonfair policy gives no ordering guarantee.
    private final ReentrantLock lock = new ReentrantLock();

    // The lock protects both fields throughout reset, check, and increment.
    private long currentWindowId;
    private int acceptedCount;

    public RateLimiterImpl(int maxRequests, long windowMillis) {
        this(maxRequests, windowMillis, System::currentTimeMillis);
    }

    // Inject a fast, thread-safe clock for deterministic tests without sleeping.
    RateLimiterImpl(int maxRequests, long windowMillis, LongSupplier currentTimeMillis) {
        if (maxRequests <= 0 || windowMillis <= 0) {
            throw new IllegalArgumentException("Limit and window duration must be positive");
        }
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.currentTimeMillis = Objects.requireNonNull(currentTimeMillis, "currentTimeMillis");
    }

    public boolean isAllowed() {
        lock.lock();
        try {
            // Read time after acquiring THIS user's lock. For a 1,000 ms window,
            // [0, 1,000) is window 0; exactly 1,000 starts window 1.
            // Epoch alignment uses wall-clock time; clock jumps can renew quotas early.
            long windowId = Math.floorDiv(currentTimeMillis.getAsLong(), windowMillis);
            if (windowId != currentWindowId) {
                currentWindowId = windowId;
                acceptedCount = 0;
            }
            if (acceptedCount >= maxRequests) {
                return false; // Rejection consumes no quota.
            }
            // Reset, check, and increment share one lock: two calls cannot take the
            // same last slot. The preceding check also prevents int overflow.
            acceptedCount++;
            return true;
        } finally {
            // Always release, including on rejection or if the clock throws.
            lock.unlock();
        }
    }
}
