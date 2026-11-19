package logfk;

import java.time.Instant;
import java.util.Objects;

/** Caller-captured value snapshot, safe to hand to an asynchronous destination. */
public record LogEvent(Instant timestamp, Level level, String loggerName,
                       String threadName, String message, String exceptionText) {
    public LogEvent {
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(loggerName, "loggerName");
        Objects.requireNonNull(threadName, "threadName");
        Objects.requireNonNull(message, "message");
        if (level == Level.OFF) throw new IllegalArgumentException("OFF is not an event level");
    }
}
