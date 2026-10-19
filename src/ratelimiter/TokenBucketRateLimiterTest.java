package ratelimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiterTest {
    public static void main(String[] args) throws Exception {
        initialBurstAndIndependentUsers();
        fractionalRefillAndCapacity();
        validatesInputs();
        concurrentRequestsRespectCapacity();
        System.out.println("TokenBucketRateLimiterTest: all 4 tests passed");
    }

    private static void initialBurstAndIndependentUsers() {
        // Clients can select either algorithm through the same contract.
        RateLimiter limiter = new TokenBucketRateLimiter(2, 1, () -> 0L);
        check(limiter.isAllowed("alice"), "First token should be available");
        check(limiter.isAllowed("alice"), "Second token should be available");
        check(!limiter.isAllowed("alice"), "Empty bucket should reject");
        check(limiter.isAllowed("bob"), "Bob should have his own bucket");
    }

    private static void fractionalRefillAndCapacity() {
        AtomicLong now = new AtomicLong();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2, now::get);
        check(limiter.isAllowed("alice"), "Consume initial token 1");
        check(limiter.isAllowed("alice"), "Consume initial token 2");
        now.set(250_000_000L);
        check(!limiter.isAllowed("alice"), "Half a token is insufficient");
        now.set(500_000_000L);
        check(limiter.isAllowed("alice"), "Fractional progress must survive rejection");
        check(!limiter.isAllowed("alice"), "Refilled token should be consumed");
        now.set(100_000_000_000L);
        check(limiter.isAllowed("alice"), "Idle bucket should refill");
        check(limiter.isAllowed("alice"), "Idle bucket should reach capacity");
        check(!limiter.isAllowed("alice"), "Idle time cannot grow bucket beyond capacity");
    }

    private static void validatesInputs() {
        expectException(IllegalArgumentException.class, () -> new TokenBucketRateLimiter(0, 1));
        for (double rate : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            expectException(IllegalArgumentException.class, () -> new TokenBucketRateLimiter(2, rate));
        }
        expectException(NullPointerException.class, () -> new TokenBucketRateLimiter(2, 1, null));
        expectException(NullPointerException.class, () -> new TokenBucketRateLimiter(2, 1).isAllowed(null));
    }

    private static void concurrentRequestsRespectCapacity() throws Exception {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 1, () -> 0L);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return limiter.isAllowed("alice");
                }));
            }
            start.countDown();
            int accepted = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) {
                    accepted++;
                }
            }
            check(accepted == 10, "Exactly ten calls should consume the initial capacity");
        } finally {
            pool.shutdownNow();
        }
    }

    private static void expectException(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            if (type.isInstance(exception)) {
                return;
            }
            throw exception;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
