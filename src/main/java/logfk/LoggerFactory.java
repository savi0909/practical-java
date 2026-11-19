package logfk;

import java.util.Objects;

/** Supplies named loggers; the application need not know their concrete backend. */
public interface LoggerFactory {
    Logger getLogger(String name);

    default Logger getLogger(Class<?> type) {
        return getLogger(Objects.requireNonNull(type, "type").getName());
    }
}
