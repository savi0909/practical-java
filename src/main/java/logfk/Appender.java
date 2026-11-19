package logfk;

import java.io.IOException;

/**
 * Thread-safe event destination. Ownership transfers to its context or async wrapper.
 * close is idempotent; append after close is a no-op. An async flush after stop
 * waits for termination and may report its failure. flush is not fsync.
 */
public interface Appender extends AutoCloseable {
    void append(LogEvent event) throws IOException;
    default void flush() throws IOException { }
    @Override default void close() throws IOException { }
}
