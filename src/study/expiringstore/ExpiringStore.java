package study.expiringstore;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/** In-memory TTL values; share one instance between callers in one JVM. */
public final class ExpiringStore<K, V> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<K, Entry<V>> entries = new HashMap<>();
    private final LongSupplier nanoTime;

    public ExpiringStore() {
        this(System::nanoTime);
    }

    ExpiringStore(LongSupplier nanoTime) {
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    }

    public void put(K key, V value, Duration ttl) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(ttl, "ttl");
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        long ttlNanos = ttl.toNanos(); // Rejects durations that overflow long nanoseconds.
        lock.lock();
        try {
            // Replacing a value also replaces its starting time and TTL atomically.
            entries.put(key, new Entry<>(value, nanoTime.getAsLong(), ttlNanos));
        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        Objects.requireNonNull(key, "key");
        lock.lock();
        try {
            Entry<V> entry = entries.get(key);
            if (entry == null) return null;
            if (expired(entry)) {
                // Lazy cleanup: untouched expired entries still occupy memory.
                entries.remove(key);
                return null;
            }
            return entry.value();
        } finally {
            lock.unlock();
        }
    }

    /** Removes the entry and reports whether it was still live. */
    public boolean remove(K key) {
        Objects.requireNonNull(key, "key");
        lock.lock();
        try {
            Entry<V> removed = entries.remove(key);
            return removed != null && !expired(removed);
        } finally {
            lock.unlock();
        }
    }

    private boolean expired(Entry<V> entry) {
        // Elapsed subtraction handles nanoTime wraparound for gaps below 2^63 ns.
        return nanoTime.getAsLong() - entry.createdNanos() >= entry.ttlNanos();
    }

    private record Entry<V>(V value, long createdNanos, long ttlNanos) {}
}
