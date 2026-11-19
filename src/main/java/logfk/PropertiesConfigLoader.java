/*
 * Critique: O(configuration text + rules + destinations) parsing/storage, excluding
 * hashing and path normalization. Immutable output has no reload races. Detect
 * duplicate keys before Properties silently overwrites them; reject unused options
 * and typoed names. Large deployments need schema/versioning and atomic reload with
 * resource retirement; XML/YAML libraries add richer schemas at dependency cost.
 */
package logfk;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Strict UTF-8 properties loader. Relative output paths use the config directory. */
public final class PropertiesConfigLoader {
    private PropertiesConfigLoader() { }

    public static LoggingConfig load(Path file) throws IOException {
        Properties properties = new Properties() {
            @Override public synchronized Object put(Object key, Object value) {
                if (containsKey(key)) throw new IllegalArgumentException("Duplicate configuration key: " + key);
                return super.put(key, value);
            }
        };
        Path absolute = file.toAbsolutePath().normalize();
        try (Reader reader = Files.newBufferedReader(absolute, StandardCharsets.UTF_8)) { properties.load(reader); }
        Set<String> consumed = new HashSet<>();
        Level root = enumValue(Level.class, required(properties, consumed, "root.level"));
        Map<String, Level> overrides = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("logger.") && key.endsWith(".level")) {
                if (key.length() <= 13) throw new IllegalArgumentException("Missing logger name: " + key);
                String name = key.substring(7, key.length() - 6);
                LoggingConfig.validateLoggerName(name);
                overrides.put(name, enumValue(Level.class, required(properties, consumed, key)));
            }
        }
        String[] names = required(properties, consumed, "appenders").split(",", -1);
        List<AppenderConfig> appenders = new ArrayList<>();
        for (String rawName : names) {
            String name = rawName.strip();
            String prefix = "appender." + name + ".";
            var type = enumValue(AppenderConfig.Destination.class, required(properties, consumed, prefix + "type"));
            Level threshold = enumValue(Level.class, optional(properties, consumed, prefix + "level", "TRACE"));
            AppenderConfig.Format format = AppenderConfig.Format.TEXT;
            if (type != AppenderConfig.Destination.MEMORY)
                format = enumValue(AppenderConfig.Format.class, optional(properties, consumed, prefix + "layout", "TEXT"));
            Path path = null;
            int bufferSize = 8192, capacity = 1000;
            boolean immediateFlush = true;
            AppenderConfig.ConsoleTarget target = AppenderConfig.ConsoleTarget.STDERR;
            switch (type) {
                case FILE -> {
                    path = absolute.getParent().resolve(required(properties, consumed, prefix + "path")).normalize();
                    bufferSize = positiveInt(optional(properties, consumed, prefix + "bufferSize", "8192"));
                    immediateFlush = bool(optional(properties, consumed, prefix + "immediateFlush", "true"));
                }
                case CONSOLE -> target = enumValue(AppenderConfig.ConsoleTarget.class,
                        optional(properties, consumed, prefix + "target", "STDERR"));
                case MEMORY -> capacity = positiveInt(optional(properties, consumed, prefix + "capacity", "1000"));
            }
            AsyncConfig async = null;
            if (bool(optional(properties, consumed, prefix + "async", "false"))) {
                async = new AsyncConfig(positiveInt(optional(properties, consumed, prefix + "queueCapacity", "1024")),
                        enumValue(OverflowPolicy.class, optional(properties, consumed, prefix + "overflowPolicy", "BLOCK")),
                        Duration.ofMillis(positiveLong(optional(properties, consumed, prefix + "shutdownTimeoutMillis", "5000"))));
            }
            appenders.add(new AppenderConfig(name, type, threshold, format, path, target,
                    bufferSize, immediateFlush, capacity, async));
        }
        Set<String> unknown = new HashSet<>(properties.stringPropertyNames());
        unknown.removeAll(consumed);
        if (!unknown.isEmpty()) throw new IllegalArgumentException("Unknown or inapplicable configuration keys: " + unknown);
        return new LoggingConfig(root, overrides, appenders);
    }

    private static String required(Properties p, Set<String> consumed, String key) {
        String value = optional(p, consumed, key, "");
        if (value.isBlank()) throw new IllegalArgumentException("Missing or blank configuration: " + key);
        return value;
    }
    private static String optional(Properties p, Set<String> consumed, String key, String defaultValue) {
        consumed.add(key);
        return p.getProperty(key, defaultValue).strip();
    }
    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        try { return Enum.valueOf(type, value); }
        catch (IllegalArgumentException failure) { throw new IllegalArgumentException("Invalid " + type.getSimpleName() + ": " + value, failure); }
    }
    private static boolean bool(String value) {
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;
        throw new IllegalArgumentException("Expected true or false: " + value);
    }
    private static int positiveInt(String value) {
        long parsed = positiveLong(value);
        if (parsed > Integer.MAX_VALUE) throw new IllegalArgumentException("Capacity exceeds integer range: " + value);
        return (int) parsed;
    }
    private static long positiveLong(String value) {
        long parsed;
        try { parsed = Long.parseLong(value); }
        catch (NumberFormatException failure) { throw new IllegalArgumentException("Invalid positive integer: " + value, failure); }
        if (parsed <= 0) throw new IllegalArgumentException("Expected positive integer: " + value);
        return parsed;
    }
}
