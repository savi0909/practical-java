package ratelimiter.fixedwindow;

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

/** Standalone checks: run main; no assertion-enable flag or test runner required. */
public class FixedWindowRateLimiterCheck {
    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock();
        clock.millis = 9_999;
        RateLimiter limiter = new FixedWindowRateLimiter(3, 10_000, clock);
        exhaust(limiter, "alice");
        exhaust(limiter, "bob"); // independent quota
        clock.millis = 10_000;
        exhaust(limiter, "alice"); // exact fixed boundary, not 10s since first request
        clock.millis = 35_000;
        exhaust(limiter, "alice"); // skipped windows reset lazily

        expect(IllegalArgumentException.class, () -> new FixedWindowRateLimiter(0, 10));
        expect(IllegalArgumentException.class, () -> new FixedWindowRateLimiter(3, -1));
        expect(NullPointerException.class, () -> new FixedWindowRateLimiter(3, 10, null));
        expect(NullPointerException.class, () -> limiter.allowRequest(null));
        expect(IllegalArgumentException.class, () -> limiter.allowRequest("  "));

        RateLimiter concurrent = new FixedWindowRateLimiter(3, 10_000,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        try (var executor = Executors.newFixedThreadPool(16)) {
            CountDownLatch ready = new CountDownLatch(16);
            CountDownLatch start = new CountDownLatch(1);
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 200; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Start gate timed out");
                    }
                    return concurrent.allowRequest("shared-user");
                }));
            }
            boolean workersReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            check(workersReady, "Workers did not become ready");
            int admitted = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) admitted++;
            }
            check(admitted == 3, "Expected exactly 3 concurrent admissions, got " + admitted);
        }
        System.out.println("PASS: quotas, users, boundaries, skipped windows, validation, concurrency");
    }

    private static void exhaust(RateLimiter limiter, String user) {
        for (int i = 0; i < 3; i++) check(limiter.allowRequest(user), "Rejected within quota");
        for (int i = 0; i < 5; i++) check(!limiter.allowRequest(user), "Admitted over quota");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            if (type.isInstance(exception)) return;
            throw exception;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
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
