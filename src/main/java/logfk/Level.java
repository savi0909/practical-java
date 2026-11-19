package logfk;

import java.util.Objects;

/** Severity threshold; OFF disables output and is never a valid event severity. */
public enum Level {
    TRACE(10), DEBUG(20), INFO(30), WARN(40), ERROR(50), OFF(Integer.MAX_VALUE);

    private final int severity;
    Level(int severity) { this.severity = severity; }

    public boolean allows(Level eventLevel) {
        Objects.requireNonNull(eventLevel, "eventLevel");
        return this != OFF && eventLevel != OFF && eventLevel.severity >= severity;
    }
}
