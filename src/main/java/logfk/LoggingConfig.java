package logfk;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable startup configuration; destination writers are created separately. */
public record LoggingConfig(Level rootLevel, Map<String, Level> loggerLevels, List<AppenderConfig> appenders) {
    public LoggingConfig {
        Objects.requireNonNull(rootLevel, "rootLevel");
        loggerLevels = Map.copyOf(loggerLevels);
        loggerLevels.keySet().forEach(LoggingConfig::validateLoggerName);
        appenders = List.copyOf(appenders);
        if (appenders.isEmpty()) throw new IllegalArgumentException("At least one appender is required");
        var names = new HashSet<String>();
        var paths = new HashSet<Path>();
        for (var appender : appenders) {
            if (!names.add(appender.name())) throw new IllegalArgumentException("Duplicate appender: " + appender.name());
            if (appender.path() != null && !paths.add(appender.path()))
                throw new IllegalArgumentException("Duplicate output file: " + appender.path());
        }
    }

    public static LoggingConfig defaults() {
        return new LoggingConfig(Level.INFO, Map.of(), List.of(AppenderConfig.console("console")));
    }

    static void validateLoggerName(String name) {
        Objects.requireNonNull(name, "logger name");
        if (name.isBlank() || !name.equals(name.strip()) || name.startsWith(".") || name.endsWith(".") || name.contains(".."))
            throw new IllegalArgumentException("Invalid logger name: " + name);
    }

    static Level resolve(Level root, Map<String, Level> overrides, String name) {
        String candidate = name;
        while (true) {
            Level level = overrides.get(candidate);
            if (level != null) return level;
            int dot = candidate.lastIndexOf('.');
            if (dot < 0) return root;
            candidate = candidate.substring(0, dot);
        }
    }
}
