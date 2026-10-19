package ratelimiter.slidingwindow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimiter;

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
class SlidingWindowRateLimiterTest {
    private final MutableClock clock = new MutableClock();
    private final InMemoryRateLimitConfigProvider provider =
            new InMemoryRateLimitConfigProvider(new RateLimitConfig(2, 1_000));
    private final RateLimiter limiter = new SlidingWindowRateLimiter(provider, clock);

    @Test
    void doesNotGrantFreshQuotaAtAnEpochBoundary() {
        clock.millis = 999;
        assertTrue(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("alice"));
        clock.millis = 1_000;
        assertFalse(limiter.allowRequest("alice"));
    }

    @Test
    void expiresRequestsIndividuallyAtExactAgeAndDoesNotCountRejections() {
        clock.millis = 100;
        assertTrue(limiter.allowRequest("alice"));
        clock.millis = 600;
        assertTrue(limiter.allowRequest("alice"));
        clock.millis = 1_099;
        assertFalse(limiter.allowRequest("alice"));
        clock.millis = 1_100;
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
        clock.millis = 1_600;
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
    }

    @Test
    void usersHaveIndependentQuotasAndOverrides() {
        provider.configure("alice", new RateLimitConfig(1, 100));
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("bob"));
        assertTrue(limiter.allowRequest("bob"));
        assertFalse(limiter.allowRequest("bob"));
        clock.millis = 100;
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("bob"));
    }

    @Test
    void configurationUpdatesResetOnlyTheirUserIncludingEqualAndReusedValues() {
        var config = new RateLimitConfig(1, 1_000);
        provider.configure("alice", config);
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("bob"));
        assertTrue(limiter.allowRequest("bob"));
        provider.configure("alice", config);
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
        provider.configure("alice", new RateLimitConfig(5, 2_000));
        provider.configure("alice", config);
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("bob"));
    }

    @Test
    void longIdlePeriodExpiresAllRequests() {
        assertTrue(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("alice"));
        clock.millis = 100_000;
        assertTrue(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
    }

    @Test
    void validatesUserIdsAndCollaborators() {
        assertThrows(NullPointerException.class, () -> limiter.allowRequest(null));
        assertThrows(IllegalArgumentException.class, () -> limiter.allowRequest("  "));
        assertThrows(IllegalArgumentException.class, () -> limiter.allowRequest(""));
        assertThrows(NullPointerException.class, () -> new SlidingWindowRateLimiter(null, clock));
        assertThrows(NullPointerException.class, () -> new SlidingWindowRateLimiter(provider, null));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowRateLimiter(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowRateLimiter(1, 0));
        RateLimiter fixedClock = new SlidingWindowRateLimiter(1, 1_000,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        assertTrue(fixedClock.allowRequest("alice"));
        assertFalse(fixedClock.allowRequest("alice"));
    }

    @Test
    void supportsNegativeEpochTimesAndExtremeElapsedTimesWithoutOverflow() {
        clock.millis = Long.MIN_VALUE;
        assertTrue(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("alice"));
        clock.millis = Long.MIN_VALUE + 999;
        assertFalse(limiter.allowRequest("alice"));
        clock.millis = Long.MAX_VALUE;
        assertTrue(limiter.allowRequest("alice"));
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("alice"));
    }

    @Test
    void concurrentRequestsAdmitExactlyTheQuotaPerUser() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = new ArrayList<Future<Boolean>>();
            var ready = new CountDownLatch(200);
            var start = new CountDownLatch(1);
            for (int i = 0; i < 200; i++) {
                String user = i % 2 == 0 ? "alice" : "bob";
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Start timed out");
                    return limiter.allowRequest(user);
                }));
            }
            try {
                assertTrue(ready.await(5, TimeUnit.SECONDS));
            } finally {
                start.countDown();
            }
            int aliceAllowed = 0;
            int bobAllowed = 0;
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).get(5, TimeUnit.SECONDS)) {
                    if (i % 2 == 0) aliceAllowed++; else bobAllowed++;
                }
            }
            assertEquals(2, aliceAllowed);
            assertEquals(2, bobAllowed);
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
