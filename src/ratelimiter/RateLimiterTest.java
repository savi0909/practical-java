package ratelimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiterTest {
    public static void main(String[] args) throws Exception {
        defaultLimitAndIndependentUsers();
        resetsAtFixedBoundary();
        validatesInputs();
        differentUsersHaveDifferentConfigurations();
        concurrentRequestsRespectLimit();
        differentUsersCanProceedConcurrently();
        System.out.println("RateLimiterTest: all 6 tests passed");
    }

    private static void defaultLimitAndIndependentUsers() {
        RateLimiter limiter = new RateLimiterManager();
        for (int i = 0; i < 10; i++) {
            check(limiter.isAllowed("alice"), "First ten requests should be allowed");
        }
        check(!limiter.isAllowed("alice"), "Eleventh request should be rejected");
        check(limiter.isAllowed("bob"), "Users should have independent limits");
    }

    private static void resetsAtFixedBoundary() {
        AtomicLong now = new AtomicLong(59_998);
        RateLimiter limiter = new RateLimiterManager(2, 60_000, now::get);
        check(limiter.isAllowed("alice"), "First request should be allowed");
        check(limiter.isAllowed("alice"), "Second request should be allowed");
        now.set(59_999);
        check(!limiter.isAllowed("alice"), "Must not reset before boundary");
        now.set(60_000);
        check(limiter.isAllowed("alice"), "Must reset exactly at fixed boundary");
        check(limiter.isAllowed("alice"), "New window should have a full allowance");
        check(!limiter.isAllowed("alice"), "New window must enforce its limit");
        now.set(240_000);
        check(limiter.isAllowed("alice"), "Must reset after multiple idle windows");
    }

    private static void validatesInputs() {
        expectException(IllegalArgumentException.class, () -> new RateLimiterManager(0, 1000));
        expectException(IllegalArgumentException.class, () -> new RateLimiterManager(-1, 1000));
        expectException(IllegalArgumentException.class, () -> new RateLimiterManager(1, 0));
        expectException(IllegalArgumentException.class, () -> new RateLimiterManager(1, -1));
        expectException(NullPointerException.class, () -> new RateLimiterManager(1, 1000, null));
        expectException(NullPointerException.class, () -> new RateLimiterManager().isAllowed(null));
        RateLimiterManager manager = new RateLimiterManager();
        expectException(NullPointerException.class, () -> manager.registerUser(null, 1, 1000));
        expectException(IllegalArgumentException.class, () -> manager.registerUser("alice", 0, 1000));
        expectException(IllegalArgumentException.class, () -> manager.registerUser("alice", 1, 0));
        manager.isAllowed("default-user");
        expectException(IllegalStateException.class,
                () -> manager.registerUser("default-user", 1, 1000));
    }

    private static void differentUsersHaveDifferentConfigurations() {
        AtomicLong now = new AtomicLong(0);
        RateLimiterManager manager = new RateLimiterManager(10, 60_000, now::get);
        manager.registerUser("alice", 1, 1_000);
        manager.registerUser("bob", 2, 2_000);
        check(manager.isAllowed("alice"), "Alice has one slot");
        check(!manager.isAllowed("alice"), "Alice must be capped at one");
        check(manager.isAllowed("bob"), "Bob has a first slot");
        check(manager.isAllowed("bob"), "Bob has a second slot");
        check(!manager.isAllowed("bob"), "Bob must be capped at two");
        now.set(1_000);
        check(manager.isAllowed("alice"), "Alice's shorter window should reset");
        check(!manager.isAllowed("bob"), "Bob's longer window should not reset yet");
        now.set(2_000);
        check(manager.isAllowed("bob"), "Bob should reset at his own boundary");
        check(manager.isAllowed("alice"), "Alice also starts a new window");
        expectException(IllegalStateException.class, () -> manager.registerUser("alice", 99, 1_000));
        check(!manager.isAllowed("alice"), "Duplicate registration must not reset Alice's quota");
    }

    private static void concurrentRequestsRespectLimit() throws Exception {
        RateLimiter limiter = new RateLimiterManager(10, 60_000, () -> 0L);
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return limiter.isAllowed("alice");
                }));
            }
            start.countDown();
            int allowed = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) {
                    allowed++;
                }
            }
            check(allowed == 10, "Exactly ten concurrent requests should succeed, got " + allowed);
        } finally {
            pool.shutdownNow();
        }
    }

    private static void differentUsersCanProceedConcurrently() throws Exception {
        CountDownLatch aliceInsideAdmission = new CountDownLatch(1);
        CountDownLatch releaseAlice = new CountDownLatch(1);
        AtomicInteger clockCalls = new AtomicInteger();
        RateLimiter limiter = new RateLimiterManager(10, 60_000, () -> {
            // Pause only Alice's first call, inside the admission operation.
            // This artificial delay belongs in the test, never the real clock.
            if (clockCalls.getAndIncrement() == 0) {
                aliceInsideAdmission.countDown();
                try {
                    releaseAlice.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
            }
            return 0L;
        });
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            // Aa and BB have equal String hashes: admission must be outside map locks.
            Future<Boolean> alice = pool.submit(() -> limiter.isAllowed("Aa"));
            check(aliceInsideAdmission.await(5, TimeUnit.SECONDS), "Alice should enter admission");
            Future<Boolean> bob = pool.submit(() -> limiter.isAllowed("BB"));
            check(bob.get(5, TimeUnit.SECONDS), "Bob should finish while Alice is paused");
            releaseAlice.countDown();
            check(alice.get(5, TimeUnit.SECONDS), "Alice should finish after release");
        } finally {
            releaseAlice.countDown();
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
