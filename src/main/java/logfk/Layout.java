package logfk;

/** Thread-safe event-to-text formatter; returned text includes its record delimiter. */
@FunctionalInterface
public interface Layout {
    String format(LogEvent event);
}
