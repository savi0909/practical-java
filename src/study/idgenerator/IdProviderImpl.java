package study.idgenerator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-memory ID allocation for threads sharing one provider in one JVM.
 * Each method has an independent sequence for comparison; never mix their IDs.
 * IDs increase in allocation order, but a thread can pause before returning its ID.
 * A new provider or JVM restart can reissue IDs because no state is persisted.
 */
public final class IdProviderImpl implements IdProvider {
    // Class initialization safely publishes the shared instance to all callers.
    private static final IdProvider INSTANCE = new IdProviderImpl();
    // Stores the last allocated ID; CAS protects updates without an explicit lock.
    private final AtomicLong id;
    private long synchronizedId; // Protected by this instance's monitor.
    private final ReentrantLock lock = new ReentrantLock(); // Default: nonfair.
    private long lockedId; // Protected by lock.

    private IdProviderImpl() {
        this(0);
    }

    // Test-only starting state for overflow checks; never reset a live sequence.
    IdProviderImpl(long lastAllocated) {
        if (lastAllocated < 0) {
            throw new IllegalArgumentException("lastAllocated must be nonnegative");
        }
        id = new AtomicLong(lastAllocated);
        synchronizedId = lastAllocated;
        lockedId = lastAllocated;
    }

    public static IdProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public long getLockedId() {
        lock.lock();
        try {
            // Check and increment under the same lock so allocation is atomic.
            // Leave the counter at MAX_VALUE: exhaustion is permanent, never wrap.
            if (lockedId == Long.MAX_VALUE) {
                throw new IllegalStateException("ID sequence exhausted");
            }
            return ++lockedId;
        } finally {
            lock.unlock(); // Also, release when overflow throws.
        }
    }

    @Override
    public synchronized long getSynchronizedId() {
        // synchronized holds this instance's monitor for the whole method.
        // Protect the check and increment together; volatile alone would not do that.
        if (synchronizedId == Long.MAX_VALUE) {
            throw new IllegalStateException("ID sequence exhausted");
        }
        return ++synchronizedId;
    }

    @Override
    public long getId() {
        while (true) {
            long current = id.get();
            // Check before adding to avoid wrapping to a negative ID.
            if (current == Long.MAX_VALUE) {
                throw new IllegalStateException("ID sequence exhausted");
            }
            // Allocate only if the value still equals our snapshot. Success is the
            // allocation point: exactly one caller can claim this next value.
            if (id.compareAndSet(current, current + 1)) {
                return current + 1;
            }
            // Another caller advanced the counter; reread and recheck the limit.
            // Contention can cause repeated retries; no per-thread fairness guarantee.
        }
    }
}
