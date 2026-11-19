package logfk;

import java.nio.file.Path;
import java.util.Objects;

/** Immutable destination specification. Use factories and with-methods for common cases. */
public record AppenderConfig(String name, Destination type, Level level, Format layout, Path path,
                             ConsoleTarget target, int bufferSize, boolean immediateFlush,
                             int memoryCapacity, AsyncConfig async) {
    public enum Destination { CONSOLE, FILE, MEMORY }
    public enum Format { TEXT, JSON }
    public enum ConsoleTarget { STDOUT, STDERR }

    public AppenderConfig {
        if (!Objects.requireNonNull(name, "name").matches("[A-Za-z][A-Za-z0-9_-]*"))
            throw new IllegalArgumentException("Invalid appender name: " + name);
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(target, "target");
        if (bufferSize <= 0 || memoryCapacity <= 0) throw new IllegalArgumentException("capacities must be positive");
        if (type == Destination.FILE) {
            Objects.requireNonNull(path, "file path");
            if (path.toString().isBlank()) throw new IllegalArgumentException("Blank file path");
            path = path.toAbsolutePath().normalize();
        }
        else if (path != null) throw new IllegalArgumentException("Only FILE accepts a path");
    }

    public static AppenderConfig console(String name) { return defaults(name, Destination.CONSOLE, null); }
    public static AppenderConfig file(String name, Path path) { return defaults(name, Destination.FILE, path); }
    public static AppenderConfig memory(String name, int capacity) {
        var c = defaults(name, Destination.MEMORY, null);
        return new AppenderConfig(c.name, c.type, c.level, c.layout, null, c.target, c.bufferSize,
                c.immediateFlush, capacity, null);
    }
    private static AppenderConfig defaults(String name, Destination type, Path path) {
        return new AppenderConfig(name, type, Level.TRACE, Format.TEXT, path, ConsoleTarget.STDERR,
                8192, true, 1000, null);
    }
    public AppenderConfig withLevel(Level level) {
        return new AppenderConfig(name, type, level, layout, path, target, bufferSize, immediateFlush, memoryCapacity, async);
    }
    public AppenderConfig withLayout(Format layout) {
        return new AppenderConfig(name, type, level, layout, path, target, bufferSize, immediateFlush, memoryCapacity, async);
    }
    public AppenderConfig withBuffering(int bufferSize, boolean immediateFlush) {
        return new AppenderConfig(name, type, level, layout, path, target, bufferSize, immediateFlush, memoryCapacity, async);
    }
    public AppenderConfig withAsync(AsyncConfig async) {
        return new AppenderConfig(name, type, level, layout, path, target, bufferSize, immediateFlush, memoryCapacity, async);
    }

    Appender open(ErrorHandler errors) throws java.io.IOException {
        Layout formatter = layout == Format.TEXT ? new SimpleTextLayout() : new JsonLayout();
        Appender destination = switch (type) {
            case CONSOLE -> new ConsoleAppender(level, formatter, target == ConsoleTarget.STDOUT ? System.out : System.err);
            case FILE -> new FileAppender(level, formatter, path, bufferSize, immediateFlush);
            case MEMORY -> new InMemoryAppender(level, memoryCapacity);
        };
        if (async == null) return destination;
        try { return new AsyncAppender(name, destination, async, errors); }
        catch (RuntimeException | Error failure) {
            try { destination.close(); } catch (Exception closeFailure) { failure.addSuppressed(closeFailure); }
            throw failure;
        }
    }
}
