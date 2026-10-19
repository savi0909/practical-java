package study.delayedscheduler;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DelayedTaskSchedulerTest {
    public static void main(String[] args) throws Exception {
        try (DelayedTaskScheduler scheduler = new DelayedTaskScheduler()) {
            CountDownLatch ran = new CountDownLatch(1);
            AtomicLong executedAt = new AtomicLong();
            long start = System.nanoTime();
            ScheduledFuture<?> delayed = scheduler.schedule(() -> {
                executedAt.set(System.nanoTime());
                ran.countDown();
            }, Duration.ofMillis(40));
            check(ran.await(5, TimeUnit.SECONDS), "Scheduled task must run");
            delayed.get(5, TimeUnit.SECONDS);
            check(executedAt.get() - start >= 40_000_000L, "Task cannot run before its delay");

            ScheduledFuture<?> failed = scheduler.schedule(() -> {
                throw new IllegalStateException("Task failure");
            }, Duration.ZERO);
            try {
                failed.get(5, TimeUnit.SECONDS);
                throw new AssertionError("Failure must be visible through the future");
            } catch (java.util.concurrent.ExecutionException expected) {
                check(expected.getCause() instanceof IllegalStateException, "Preserve task failure");
            }
            scheduler.schedule(() -> {}, Duration.ZERO).get(5, TimeUnit.SECONDS);
        }

        DelayedTaskScheduler closed = new DelayedTaskScheduler();
        ScheduledFuture<?> pending = closed.schedule(() -> {
            throw new AssertionError("Pending task must be cancelled on close");
        }, Duration.ofDays(1));
        closed.close();
        check(pending.isCancelled(), "Close must cancel pending work");
        try {
            closed.schedule(() -> {}, Duration.ZERO);
            throw new AssertionError("Closed scheduler must reject work");
        } catch (IllegalStateException expected) {
            // Expected lifecycle rejection.
        }
        System.out.println("DelayedTaskSchedulerTest: 3 checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
