/*
 * ReentrantLock + AutoCloseable: one fresh guard per acquisition. try-with-resources
 * calls close() on normal exit, return, or exception; multiple guards close in reverse
 * declaration order. If a later acquisition fails, earlier guards are still closed.
 *
 * Interview critique:
 * - Cost: O(1) guard space and uncontended acquire/close work; contention can block
 *   indefinitely. ReentrantLock supplies mutual exclusion and unlock-to-lock visibility.
 * - Ownership: acquire and close must run on the same thread. Check the owner before
 *   reading mutable closed state, so only the owner accesses it; no volatile is needed.
 *   Do not transfer a guard into an asynchronous task or manually unlock its lock.
 * - Edges: null is rejected; repeated owner close is a no-op; nested acquisitions need
 *   separate guards and release one hold each. Reusing a closed guard acquires nothing.
 *   ReentrantLock rejects hold-count overflow; IDs, ties, and empty input do not apply.
 * - Multiple locks can still deadlock: acquire them in one consistent global order.
 *   lock() does not cancel on interruption; use lockInterruptibly/tryLock if required.
 * - Scale: one contended lock serializes throughput; partition state/locks or use
 *   suitable concurrent collections. Allocate only where scoped ownership is useful.
 * - Alternative: lock.lock(); try { ... } finally { lock.unlock(); } avoids the guard
 *   allocation and is idiomatic. synchronized is simpler if advanced lock APIs aren't
 *   needed. This wrapper improves scope syntax, not the underlying locking guarantees.
 */
package concurrency;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class ScopedLock implements AutoCloseable {
    private final ReentrantLock lock;
    private final Thread owner;
    private boolean closed;

    private ScopedLock(ReentrantLock lock) {
        this.lock = Objects.requireNonNull(lock, "lock");
        this.owner = Thread.currentThread();
    }

    public static ScopedLock acquire(ReentrantLock lock) {
        // Allocate before locking: allocation failure must not leave a held lock.
        ScopedLock guard = new ScopedLock(lock);
        lock.lock();
        return guard;
    }

    @Override
    public void close() {
        if (Thread.currentThread() != owner) {
            throw new IllegalMonitorStateException("Only the acquiring thread may close");
        }
        if (!closed) {
            lock.unlock();
            closed = true;
        }
    }

    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        try (ScopedLock ignored = ScopedLock.acquire(lock)) {
            System.out.println("Inside block: locked = " + lock.isHeldByCurrentThread());
            throw new IllegalStateException("Example body failure");
        } catch (IllegalStateException failure) {
            System.out.println("Caught: " + failure.getMessage());
        }
        System.out.println("After block: locked = " + lock.isLocked());
    }
}
