package ratelimiter.fixedwindow;

import ratelimiter.common.InMemoryRateLimitConfigProvider;
import ratelimiter.common.RateLimitConfig;
import ratelimiter.common.RateLimitConfigProvider;
import ratelimiter.common.RateLimiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class FixedWindowRateLimiterTest {
    private final MutableClock clock = new MutableClock(1_000);
    private final InMemoryRateLimitConfigProvider provider =
            new InMemoryRateLimitConfigProvider(new RateLimitConfig(3, 10_000));
    private final RateLimiter limiter = new FixedWindowRateLimiter(provider, clock);

    @Test
    void acceptsFirstRequest() {
        assertTrue(limiter.allowRequest("alice"));
    }

    @Test
    void acceptsUpToLimitAndKeepsRejectingAfterward() {
        exhaust(limiter, "alice", 3);
    }

    @Test
    void resetsAtExactEpochBoundaryRatherThanAfterTimeSinceFirstRequest() {
        clock.millis = 9_999;
        exhaust(limiter, "alice", 3);
        clock.millis = 10_000;
        exhaust(limiter, "alice", 3);
        clock.millis = 45_000;
        exhaust(limiter, "alice", 3);
    }

    @Test
    void maintainsIndependentCountersForTwoUsers() {
        exhaust(limiter, "alice", 3);
        exhaust(limiter, "bob", 3);
        assertFalse(limiter.allowRequest("alice"));
    }

    @Test
    void appliesUserSpecificLimitsAndDurations() {
        provider.configure("alice", new RateLimitConfig(1, 2_000));
        provider.configure("bob", new RateLimitConfig(5, 60_000));
        exhaust(limiter, "alice", 1);
        exhaust(limiter, "bob", 5);
        clock.millis = 2_000;
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("bob"));
    }

    @Test
    void usesDefaultForUsersWithoutOverride() {
        assertEquals(new RateLimitConfig(3, 10_000), provider.getConfig("unknown"));
        exhaust(limiter, "unknown", 3);
    }

    @Test
    void changedLimitResetsEvenWhenWindowIdIsUnchanged() {
        exhaust(limiter, "alice", 3);
        provider.configure("alice", new RateLimitConfig(1, 10_000));
        exhaust(limiter, "alice", 1);
    }

    @Test
    void changedDurationResetsEvenWhenWindowIdIsUnchanged() {
        exhaust(limiter, "alice", 3); // window ID 0
        provider.configure("alice", new RateLimitConfig(3, 60_000)); // still ID 0
        exhaust(limiter, "alice", 3);
    }

    @Test
    void everyUpdateResetsIncludingEqualValuesAndReusedConfigInstances() {
        RateLimitConfig original = new RateLimitConfig(1, 10_000);
        provider.configure("alice", original);
        exhaust(limiter, "alice", 1);
        provider.configure("alice", original);
        exhaust(limiter, "alice", 1);
        provider.configure("alice", new RateLimitConfig(5, 60_000));
        provider.configure("alice", original); // A -> B -> A without requests in B
        exhaust(limiter, "alice", 1);
    }

    @Test
    void resetsOnlyTheReconfiguredUser() {
        exhaust(limiter, "alice", 3);
        exhaust(limiter, "bob", 3);
        provider.configure("alice", new RateLimitConfig(1, 10_000));
        assertTrue(limiter.allowRequest("alice"));
        assertFalse(limiter.allowRequest("bob"));
    }

    @Test
    void concurrentRequestsAdmitExactlyTheConfiguredQuota() throws Exception {
        assertEquals(3, concurrentAdmissions(limiter, "alice", 200));
        assertFalse(limiter.allowRequest("alice"));
    }

    @Test
    void concurrentUsersDoNotShareCounters() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = new HashMap<String, Future<Integer>>();
            for (String user : new String[]{"alice", "bob", "carol"}) {
                results.put(user, executor.submit(() -> concurrentAdmissions(limiter, user, 100)));
            }
            for (var entry : results.entrySet()) {
                assertEquals(3, entry.getValue().get(5, TimeUnit.SECONDS), entry.getKey());
            }
        }
    }

    @Test
    void overlappingUpdateIsObservedAfterInFlightOldRevisionCompletes() throws Exception {
        CountDownLatch oldRead = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean pauseOnce = new AtomicBoolean(true);
        RateLimitConfigProvider gatedProvider = user -> {
            RateLimitConfig snapshot = provider.getConfig(user);
            if (pauseOnce.compareAndSet(true, false)) {
                oldRead.countDown();
                await(release);
            }
            return snapshot;
        };
        RateLimiter gated = new FixedWindowRateLimiter(gatedProvider, clock);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Boolean> inFlight = executor.submit(() -> gated.allowRequest("alice"));
            try {
                assertTrue(oldRead.await(5, TimeUnit.SECONDS));
                provider.configure("alice", new RateLimitConfig(1, 10_000));
            } finally {
                release.countDown();
            }
            assertTrue(inFlight.get(5, TimeUnit.SECONDS));
            // The completed old request must not cause the new revision to reuse its counter.
            assertTrue(gated.allowRequest("alice"));
            assertFalse(gated.allowRequest("alice"));
        }
    }

    @Test
    void rejectsInvalidConfigurations() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimitConfig(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitConfig(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitConfig(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitConfig(1, -1));
        assertThrows(NullPointerException.class, () -> new InMemoryRateLimitConfigProvider(null));
        assertThrows(NullPointerException.class, () -> provider.configure("alice", null));
        assertThrows(NullPointerException.class, () -> new FixedWindowRateLimiter(null, clock));
        assertThrows(NullPointerException.class, () -> new FixedWindowRateLimiter(provider, null));
    }

    @Test
    void rejectsNullAndBlankUserIds() {
        assertThrows(NullPointerException.class, () -> limiter.allowRequest(null));
        assertThrows(NullPointerException.class, () -> provider.getConfig(null));
        assertThrows(NullPointerException.class,
                () -> provider.configure(null, new RateLimitConfig(1, 10)));
        assertThrows(IllegalArgumentException.class, () -> limiter.allowRequest(""));
        assertThrows(IllegalArgumentException.class, () -> provider.getConfig("  "));
    }

    private static void exhaust(RateLimiter limiter, String user, int limit) {
        for (int i = 0; i < limit; i++) assertTrue(limiter.allowRequest(user));
        for (int i = 0; i < 5; i++) assertFalse(limiter.allowRequest(user));
    }

    private static int concurrentAdmissions(RateLimiter limiter, String user, int requests)
            throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch ready = new CountDownLatch(requests);
            CountDownLatch start = new CountDownLatch(1);
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < requests; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    await(start);
                    return limiter.allowRequest(user);
                }));
            }
            try {
                assertTrue(ready.await(5, TimeUnit.SECONDS));
            } finally {
                start.countDown();
            }
            int allowed = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) allowed++;
            }
            return allowed;
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) throw new AssertionError("Latch timed out");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError(interrupted);
        }
    }

    private static final class MutableClock extends Clock {
        private volatile long millis;
        private MutableClock(long millis) { this.millis = millis; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) throw new UnsupportedOperationException();
            return this;
        }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis); }
        @Override public long millis() { return millis; }
    }
}
