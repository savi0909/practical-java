/*
 * Critique: O(1) queue admission (excluding BLOCK waiting), O(capacity) event slots;
 * event byte size is unbounded. One worker serializes the delegate. The same lock
 * guards queue admission and stop, preventing enqueue-after-drain races; conditions
 * release the lock while waiting. Flush barriers cannot be dropped. Interrupted
 * producers restore interruption and reject their event. Close drains accepted work
 * within a wait deadline; a stuck delegate can outlive it on a daemon thread, and
 * only that worker closes its resources. No crash durability or retry/exactly-once
 * claim. Batch/ring-buffer implementations can improve throughput at complexity cost.
 */
package logfk;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Bounded async decorator. Ownership of the delegate transfers to its worker. */
public final class AsyncAppender implements Appender {
    private record Work(LogEvent event, CompletableFuture<Void> barrier) { }

    private final Appender delegate;
    private final AsyncConfig config;
    private final ErrorHandler errors;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();
    private final ArrayDeque<Work> queue = new ArrayDeque<>();
    private final CompletableFuture<Void> termination = new CompletableFuture<>();
    private final LongAdder accepted = new LongAdder();
    private final LongAdder delivered = new LongAdder();
    private final LongAdder dropped = new LongAdder();
    private final LongAdder failures = new LongAdder();
    private final Thread worker;
    private boolean accepting = true;

    public AsyncAppender(String name, Appender delegate, AsyncConfig config, ErrorHandler errors) {
        if (Objects.requireNonNull(name, "name").isBlank()) throw new IllegalArgumentException("blank async name");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.config = Objects.requireNonNull(config, "config");
        this.errors = Objects.requireNonNull(errors, "errors");
        this.worker = new Thread(this::run, "logfk-" + name);
        worker.setDaemon(true);
        worker.start();
    }

    @Override public void append(LogEvent event) throws IOException {
        Objects.requireNonNull(event, "event");
        if (Thread.currentThread() == worker) {
            // A delegate must not recursively enqueue into its own full queue.
            dropped.increment();
            return;
        }
        enqueue(new Work(event, null), config.overflowPolicy() == OverflowPolicy.DROP_NEWEST);
    }

    private boolean enqueue(Work work, boolean mayDrop) throws IOException {
        lock.lock();
        try {
            while (accepting && queue.size() == config.capacity()) {
                if (mayDrop) { dropped.increment(); return false; }
                try { changed.await(); }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    if (work.event() != null) dropped.increment();
                    throw new IOException("Interrupted while waiting for async capacity", interrupted);
                }
            }
            if (!accepting) return false;
            queue.addLast(work);
            if (work.event() != null) accepted.increment();
            changed.signalAll();
            return true;
        } finally { lock.unlock(); }
    }

    /** Waits for work ahead of a non-droppable barrier and flushes the delegate. */
    @Override public void flush() throws IOException {
        ensureNotWorker("flush");
        CompletableFuture<Void> barrier = new CompletableFuture<>();
        if (enqueue(new Work(null, barrier), false)) await(barrier);
        else await(termination);
    }

    @Override public void close() throws IOException {
        ensureNotWorker("close");
        stopAccepting();
        try { worker.join(config.shutdownTimeout().toMillis()); }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while closing async appender; worker continues draining", interrupted);
        }
        if (worker.isAlive()) throw new IOException("Async shutdown deadline exceeded; daemon worker continues draining");
        await(termination);
    }

    void stopAccepting() {
        lock.lock();
        try { accepting = false; changed.signalAll(); }
        finally { lock.unlock(); }
    }

    private void ensureNotWorker(String operation) throws IOException {
        if (Thread.currentThread() == worker) throw new IOException("Async worker cannot " + operation + " itself");
    }

    private static void await(CompletableFuture<Void> completion) throws IOException {
        try { completion.get(); }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for async delivery", interrupted);
        } catch (ExecutionException failure) {
            throw new IOException("Async destination failed", failure.getCause());
        }
    }

    private void run() {
        Exception terminalFailure = null;
        try {
            while (true) {
                Work work;
                lock.lock();
                try {
                    while (queue.isEmpty() && accepting) changed.await();
                    if (queue.isEmpty()) break;
                    work = queue.removeFirst();
                    changed.signalAll();
                } finally { lock.unlock(); }
                try {
                    CallbackGuard.enter();
                    if (work.event() != null) {
                        delegate.append(work.event());
                        delivered.increment();
                    } else {
                        delegate.flush();
                        work.barrier().complete(null);
                    }
                } catch (Exception failure) {
                    failures.increment();
                    if (work.barrier() != null) work.barrier().completeExceptionally(failure);
                    InternalErrors.report(errors, "async destination", failure);
                } catch (Error fatal) {
                    if (work.barrier() != null) work.barrier().completeExceptionally(fatal);
                    throw fatal;
                } finally { CallbackGuard.exit(); }
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            terminalFailure = interrupted;
            InternalErrors.report(errors, "async worker interrupted", interrupted);
        } catch (Error fatal) {
            terminalFailure = new IOException("Fatal async worker failure", fatal);
            throw fatal;
        } finally {
            // Also executed for fatal Errors: unblock producers/barriers before propagating.
            lock.lock();
            try {
                accepting = false;
                IOException stopped = new IOException("Async worker stopped before delivery");
                for (Work pending : queue) {
                    if (pending.barrier() != null) pending.barrier().completeExceptionally(stopped);
                    else dropped.increment();
                }
                queue.clear();
                changed.signalAll();
            } finally { lock.unlock(); }
            try { CallbackGuard.enter(); delegate.close(); }
            catch (Exception failure) {
                terminalFailure = failure;
                InternalErrors.report(errors, "async destination close", failure);
            } catch (Error fatal) {
                terminalFailure = new IOException("Fatal async close failure", fatal);
                throw fatal;
            } finally {
                CallbackGuard.exit();
                if (terminalFailure == null) termination.complete(null);
                else termination.completeExceptionally(terminalFailure);
            }
        }
    }

    public long acceptedCount() { return accepted.sum(); }
    /** Successful delegate.append calls, not a durability or threshold-admission count. */
    public long deliveredCount() { return delivered.sum(); }
    public long droppedCount() { return dropped.sum(); }
    public long failureCount() { return failures.sum(); }
    public int queueSize() { lock.lock(); try { return queue.size(); } finally { lock.unlock(); } }
}
