package logfk;

import java.time.Duration;
import java.util.Objects;

public record AsyncConfig(int capacity, OverflowPolicy overflowPolicy, Duration shutdownTimeout) {
    public AsyncConfig {
        if (capacity <= 0) throw new IllegalArgumentException("async capacity must be positive");
        Objects.requireNonNull(overflowPolicy, "overflowPolicy");
        Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        try {
            if (shutdownTimeout.toMillis() < 1) throw new IllegalArgumentException("shutdownTimeout must be at least 1ms");
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException("shutdownTimeout is too large", overflow);
        }
    }
}
