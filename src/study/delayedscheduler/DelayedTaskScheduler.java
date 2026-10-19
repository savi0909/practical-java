package study.delayedscheduler;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** One worker executes one-off tasks after a relative delay. Close when finished. */
public final class DelayedTaskScheduler implements AutoCloseable {
    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private boolean closed;

    public DelayedTaskScheduler() {
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    public ScheduledFuture<?> schedule(Runnable task, Duration delay) {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) throw new IllegalArgumentException("Delay cannot be negative");
        long delayNanos = delay.toNanos(); // Overflow is rejected, never silently wrapped.
        lock.lock();
        try {
            // Checking lifecycle and submitting must not race with close().
            if (closed) throw new IllegalStateException("Scheduler is closed");
            // The JDK owns the delay queue and worker; task bodies run outside our lock.
            return executor.schedule(task, delayNanos, TimeUnit.NANOSECONDS);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            closed = true;
            // Pending tasks are cancelled by the policy above; running work may finish.
            // This does not wait for termination or interrupt a running task.
            executor.shutdown();
        } finally {
            lock.unlock();
        }
    }
}
