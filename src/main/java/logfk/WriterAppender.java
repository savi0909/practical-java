/*
 * Critique: O(message + exception length) per accepted event plus I/O; temporary
 * space is the rendered record. A single monitor protects format/write/flush/close,
 * so records do not interleave, but a slow sink blocks all its callers. Empty and
 * repeated messages are valid; OFF rejects everything; close is idempotent.
 * Buffering saves writes; bounded async delivery moves waiting off the caller at
 * the cost of lag and overflow policy. A shared file across contexts is unsupported.
 */
package logfk;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/** Reusable destination for custom Writers; owns the writer only when requested. */
public class WriterAppender implements Appender {
    private final Level threshold;
    private final Layout layout;
    private final Writer writer;
    private final boolean immediateFlush;
    private final boolean ownsWriter;
    private boolean closed;

    public WriterAppender(Level threshold, Layout layout, Writer writer, boolean immediateFlush, boolean ownsWriter) {
        this.threshold = Objects.requireNonNull(threshold, "threshold");
        this.layout = Objects.requireNonNull(layout, "layout");
        this.writer = Objects.requireNonNull(writer, "writer");
        this.immediateFlush = immediateFlush;
        this.ownsWriter = ownsWriter;
    }

    @Override public synchronized void append(LogEvent event) throws IOException {
        Objects.requireNonNull(event, "event");
        if (closed || !threshold.allows(event.level())) return;
        writer.write(Objects.requireNonNull(layout.format(event), "layout result"));
        if (immediateFlush) writer.flush();
    }

    @Override public synchronized void flush() throws IOException { if (!closed) writer.flush(); }

    @Override public synchronized void close() throws IOException {
        if (closed) return;
        closed = true;
        if (ownsWriter) writer.close();
        else writer.flush();
    }
}
