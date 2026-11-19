/*
 * Critique: O(1) append, O(capacity) retained space; snapshot copies are O(size).
 * A monitor guards retention, eviction counts and close. Full storage evicts the
 * oldest event; duplicate messages remain distinct. Snapshot allocation and large
 * messages dominate at scale; use a file/collector or sampling, losing local history.
 */
package logfk;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

/** Bounded event history for tests and local diagnostics; retains data after close. */
public final class InMemoryAppender implements Appender {
    private final Level threshold;
    private final int capacity;
    private final ArrayDeque<LogEvent> events = new ArrayDeque<>();
    private long evicted;
    private boolean closed;

    public InMemoryAppender(Level threshold, int capacity) {
        this.threshold = Objects.requireNonNull(threshold, "threshold");
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }

    @Override public synchronized void append(LogEvent event) {
        Objects.requireNonNull(event, "event");
        if (closed || !threshold.allows(event.level())) return;
        if (events.size() == capacity) { events.removeFirst(); evicted++; }
        events.addLast(event);
    }

    public synchronized List<LogEvent> events() { return List.copyOf(events); }
    public synchronized long evictedCount() { return evicted; }
    @Override public synchronized void close() { closed = true; }
}
