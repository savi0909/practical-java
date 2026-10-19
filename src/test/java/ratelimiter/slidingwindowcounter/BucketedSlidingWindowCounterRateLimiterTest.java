package ratelimiter.slidingwindowcounter;

import org.junit.jupiter.api.Test;
import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static ratelimiter.slidingwindowcounter.CounterTestSupport.*;

class BucketedSlidingWindowCounterRateLimiterTest {
    private final MutableClock clock = new MutableClock();
    private final InMemoryRateLimitConfigProvider provider =
            new InMemoryRateLimitConfigProvider(new RateLimitConfig(4, 60_000));
    private final BucketedSlidingWindowCounterRateLimiter limiter =
            new BucketedSlidingWindowCounterRateLimiter(provider, clock);

    @Test
    void sameTimestampCallsConsumeIndependentQuotaPerUser() {
        exhaust(limiter, "alice", 4);
        exhaust(limiter, "bob", 4);
    }

    @Test
    void retainsTheTwelfthPreviousBucketPlusActiveBucket() {
        clock.millis = 4_999;
        for (int i = 0; i < 3; i++) assertTrue(limiter.allowRequest("alice"));
        clock.millis = 60_000;
        assertTrue(limiter.allowRequest("alice")); // bucket 0 and active bucket 12 coexist
        assertFalse(limiter.allowRequest("alice"));
        clock.millis = 65_000;
        exhaust(limiter, "alice", 3); // reuse slot 0 for bucket 13; bucket 12 still counts
    }

    @Test
    void conservativeOldestBucketCanRejectAlreadyExpiredRequests() {
        exhaust(limiter, "alice", 4); // accepted at 0, exactly expires at 60_000
        clock.millis = 60_000;
        assertFalse(limiter.allowRequest("alice")); // full bucket [0,5000) still overlaps
        clock.millis = 65_000;
        exhaust(limiter, "alice", 4);
    }

    @Test
    void expiresIndividualBucketsWithoutClearingStillLiveCounters() {
        clock.millis = 1_000;
        assertTrue(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("alice"));
        clock.millis = 6_000;
        assertTrue(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("alice"));
        clock.millis = 65_000;
        exhaust(limiter, "alice", 2); // bucket 0 expires, bucket 1 remains
        clock.millis = 70_000;
        exhaust(limiter, "alice", 2); // bucket 1 expires, bucket 13 remains
    }

    @Test
    void eachUpdateReplacesArraysAndOnlyResetsItsUser() {
        var config = new RateLimitConfig(1, 60_000);
        provider.configure("alice", config);
        exhaust(limiter, "alice", 1);
        exhaust(limiter, "bob", 4);
        provider.configure("alice", config);
        exhaust(limiter, "alice", 1);
        provider.configure("alice", new RateLimitConfig(3, 120_000));
        provider.configure("alice", config);
        exhaust(limiter, "alice", 1);
        provider.configure("alice", new RateLimitConfig(2, 10_000));
        exhaust(limiter, "alice", 2);
        assertFalse(limiter.allowRequest("bob"));
        clock.millis = 15_000;
        exhaust(limiter, "alice", 2);
        assertFalse(limiter.allowRequest("bob"));
    }

}
