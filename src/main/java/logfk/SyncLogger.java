package logfk;

import java.util.Objects;

/** Immutable named handle; async delivery, when configured, is an appender concern. */
final class SyncLogger implements Logger {
    private final String name;
    private final Level threshold;
    private final LoggingContext context;

    SyncLogger(String name, Level threshold, LoggingContext context) {
        this.name = name;
        this.threshold = threshold;
        this.context = context;
    }
    @Override public String name() { return name; }
    @Override public boolean isEnabled(Level level) {
        Objects.requireNonNull(level, "level");
        return context.isOpen() && threshold.allows(level);
    }
    @Override public void log(Level level, String template, Object... arguments) {
        log(level, null, template, arguments);
    }
    @Override public void log(Level level, Throwable cause, String template, Object... arguments) {
        context.log(name, threshold, level, cause, template, arguments);
    }
}
