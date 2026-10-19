package ratelimiter.tokenbucket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class TokenBucketRateLimiterTest {
    private final MutableClock clock = new MutableClock();

    @Test
    void startsFullAndKeepsTenantsIndependent() {
        var limiter = new TokenBucketRateLimiter(3, clock);
        exhaust(limiter, "alice", 3);
        exhaust(limiter, "bob", 3);
    }

    @Test
    void refillsTwoAtExactBoundaryAndPreservesPartialPeriod() {
        clock.millis = 1_000;
        var limiter = new TokenBucketRateLimiter(5, clock);
        exhaust(limiter, "alice", 5);
        clock.millis = 5_999;
        assertFalse(limiter.allowRequest("alice"));
        clock.millis = 6_000;
        exhaust(limiter, "alice", 2);
        clock.millis = 13_500;
        exhaust(limiter, "alice", 2);
        clock.millis = 16_000; // Retained the 2.5 seconds left over at 13.5s.
        exhaust(limiter, "alice", 2);
    }

    @Test
    void skippedPeriodsAccumulateOnlyUpToCapacity() {
        var limiter = new TokenBucketRateLimiter(5, clock);
        exhaust(limiter, "alice", 5);
        clock.millis = 10_000;
        exhaust(limiter, "alice", 4);
        clock.millis = Long.MAX_VALUE;
        exhaust(limiter, "alice", 5); // Huge gaps must not overflow token arithmetic.
    }

    @Test
    void discardsExcessRefillAndSupportsCapacityOne() {
        var limiter = new TokenBucketRateLimiter(1, clock);
        exhaust(limiter, "alice", 1);
        clock.millis = 100_000;
        exhaust(limiter, "alice", 1);
        clock.millis = 104_999;
        assertFalse(limiter.allowRequest("alice"));
        clock.millis = 105_000;
        exhaust(limiter, "alice", 1);
    }

    @Test
    void advancesRefillTimeWhileBucketStillHasTokens() {
        var limiter = new TokenBucketRateLimiter(3, clock);
        assertTrue(limiter.allowRequest("alice")); // Two tokens remain.
        clock.millis = 100_000;
        exhaust(limiter, "alice", 3);
        assertFalse(limiter.allowRequest("alice")); // No banked overflow from the idle gap.
    }

    @Test
    void equalValueConfigurationUpdateResetsOnlyItsTenant() {
        var provider = new InMemoryRateLimitConfigProvider(new RateLimitConfig(3, 5_000));
        var limiter = new TokenBucketRateLimiter(provider, clock);
        exhaust(limiter, "alice", 3);
        exhaust(limiter, "bob", 3);
        provider.configure("alice", new RateLimitConfig(3, 5_000));
        exhaust(limiter, "alice", 3);
        assertFalse(limiter.allowRequest("bob"));
        provider.configure("alice", new RateLimitConfig(1, 5_000));
        exhaust(limiter, "alice", 1);
    }

    @Test
    void concurrentRequestsCannotOverspendInitialOrRefilledTokens() throws Exception {
        var limiter = new TokenBucketRateLimiter(7, clock);
        assertEquals(7, concurrentAdmissions(limiter));
        clock.millis = 5_000;
        assertEquals(2, concurrentAdmissions(limiter));
    }

    @Test
    void validatesCapacityCollaboratorsAndIds() {
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketRateLimiter(0));
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketRateLimiter(-1));
        assertThrows(NullPointerException.class, () -> new TokenBucketRateLimiter(3, null));
        assertThrows(NullPointerException.class, () -> new TokenBucketRateLimiter(null, clock));
        var limiter = new TokenBucketRateLimiter(3, clock);
        assertThrows(NullPointerException.class, () -> limiter.allowRequest(null));
        assertThrows(IllegalArgumentException.class, () -> limiter.allowRequest(" \t"));
    }

    private static void exhaust(TokenBucketRateLimiter limiter, String user, int count) {
        for (int i = 0; i < count; i++) assertTrue(limiter.allowRequest(user));
        assertFalse(limiter.allowRequest(user));
        assertFalse(limiter.allowRequest(user));
    }

    private static int concurrentAdmissions(TokenBucketRateLimiter limiter) throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 100; i++) {
                results.add(executor.submit(() -> {
                    if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Start timed out");
                    return limiter.allowRequest("alice");
                }));
            }
            start.countDown();
            int accepted = 0;
            for (var result : results) if (result.get(5, TimeUnit.SECONDS)) accepted++;
            return accepted;
        }
    }

    private static final class MutableClock extends Clock {
        private volatile long millis;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) throw new UnsupportedOperationException();
            return this;
        }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis); }
        @Override public long millis() { return millis; }
    }
}
