package study.boundedqueue;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Fixed-capacity FIFO with interruptible blocking puts and takes. */
public final class BoundedBlockingQueue<E> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final ArrayDeque<E> items = new ArrayDeque<>();
    private final int capacity;

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
    }

    public void put(E item) throws InterruptedException {
        Objects.requireNonNull(item, "item");
        lock.lockInterruptibly();
        try {
            // await releases the lock while waiting, then reacquires it before returning.
            // Recheck in a loop: another caller may act first, or a wakeup may be spurious.
            while (items.size() == capacity) notFull.await();
            items.addLast(item);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (items.isEmpty()) notEmpty.await();
            E item = items.removeFirst();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }
}
