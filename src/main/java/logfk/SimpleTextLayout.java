package logfk;

/** Stateless formatter; Instant.toString supplies an unambiguous UTC timestamp. */
public final class SimpleTextLayout implements Layout {
    @Override public String format(LogEvent event) {
        return event.timestamp() + " [" + event.threadName() + "] " + event.level()
                + " " + event.loggerName() + " - " + event.message() + System.lineSeparator()
                + (event.exceptionText() == null ? "" : event.exceptionText() + System.lineSeparator());
    }
}
