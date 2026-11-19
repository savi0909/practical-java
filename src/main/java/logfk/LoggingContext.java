/*
 * Interview critique:
 * - Cached lookup is O(N) including name validation; disabled logging is O(1).
 *   First lookup resolves D ancestors with O(N*D) substring work for name length N.
 *   Delivery is O(A*(M+T)) plus I/O
 *   for A appenders, message length M, exception length T. Logger/config storage is
 *   O(logger names + rules + appenders), plus destination buffers and queued events.
 * - Immutable settings, concurrent cache and fair lifecycle read/write lock protect
 *   visibility and close against delivery. Per-sink locks/barriers protect flush;
 *   different sinks need not observe concurrent callers in the same order.
 * - OFF, closest dot-boundary override, null/empty messages, duplicate log calls,
 *   partial startup, failing sinks and duplicate close have explicit contracts.
 * - Blocking I/O and BLOCK queues can stall callers; shutdown waits for in-flight
 *   synchronous calls. A bounded async queue with DROP_NEWEST bounds caller waiting,
 *   trading delivery loss for latency. Event sizes and logger-name growth remain unbounded.
 * - Real SLF4J plus an established backend is the production alternative; this
 *   exercise costs compatibility, rotation and operational maturity for inspectability.
 */
package logfk;

import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Explicit logger factory and resource owner. Use try-with-resources at application scope. */
public final class LoggingContext implements LoggerFactory, AutoCloseable {
    private final Level rootLevel;
    private final Map<String, Level> overrides;
    private final Map<String, Appender> appenders;
    private final Clock clock;
    private final ErrorHandler errors;
    private final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lifecycle = new ReentrantReadWriteLock(true);
    private volatile boolean closed;
    private boolean resourcesClosed;

    public LoggingContext() throws IOException { this(LoggingConfig.defaults()); }
    public LoggingContext(LoggingConfig config) throws IOException { this(config, Clock.systemUTC(), ErrorHandler.stderr()); }

    public LoggingContext(LoggingConfig config, Clock clock, ErrorHandler errors) throws IOException {
        this(config.rootLevel(), config.loggerLevels(), open(config, clock, errors), clock, errors);
    }

    /** Adopts the supplied appenders; do not share their ownership with another context. */
    public LoggingContext(Level rootLevel, Map<String, Level> overrides, Map<String, Appender> appenders,
                          Clock clock, ErrorHandler errors) {
        this.rootLevel = Objects.requireNonNull(rootLevel, "rootLevel");
        this.overrides = Map.copyOf(overrides);
        this.overrides.keySet().forEach(LoggingConfig::validateLoggerName);
        this.clock = Objects.requireNonNull(clock, "clock");
        this.errors = Objects.requireNonNull(errors, "errors");
        if (appenders.isEmpty()) throw new IllegalArgumentException("At least one appender is required");
        var copy = new LinkedHashMap<String, Appender>();
        var identities = Collections.newSetFromMap(new IdentityHashMap<Appender, Boolean>());
        appenders.forEach((name, appender) -> {
            if (Objects.requireNonNull(name, "appender name").isBlank()) throw new IllegalArgumentException("Blank appender name");
            if (!identities.add(Objects.requireNonNull(appender, "appender")))
                throw new IllegalArgumentException("The same appender cannot be registered twice");
            copy.put(name, appender);
        });
        this.appenders = Collections.unmodifiableMap(copy);
    }

    private static Map<String, Appender> open(LoggingConfig config, Clock clock, ErrorHandler errors) throws IOException {
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(errors, "errors");
        var opened = new LinkedHashMap<String, Appender>();
        try {
            for (AppenderConfig spec : config.appenders()) opened.put(spec.name(), spec.open(errors));
            return opened;
        } catch (IOException | RuntimeException | Error failure) {
            for (Appender appender : opened.values()) {
                try { appender.close(); }
                catch (Exception closeFailure) { failure.addSuppressed(closeFailure); }
            }
            throw failure;
        }
    }

    @Override public Logger getLogger(String name) {
        LoggingConfig.validateLoggerName(name);
        CallbackGuard.rejectControlOperation();
        lifecycle.readLock().lock();
        try {
            if (closed) throw new IllegalStateException("Logging context is closed");
            return loggers.computeIfAbsent(name, n -> new SyncLogger(n,
                    LoggingConfig.resolve(rootLevel, overrides, n), this));
        } finally { lifecycle.readLock().unlock(); }
    }

    /** Access for destination diagnostics/counters; the context retains resource ownership. */
    public Appender getAppender(String name) {
        Appender appender = appenders.get(Objects.requireNonNull(name, "name"));
        if (appender == null) throw new IllegalArgumentException("Unknown appender: " + name);
        return appender;
    }

    boolean isOpen() { return !closed; }

    void log(String name, Level threshold, Level level, Throwable cause, String template, Object[] arguments) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(arguments, "arguments");
        if (level == Level.OFF) throw new IllegalArgumentException("OFF is not an event level");
        if (closed || !threshold.allows(level) || CallbackGuard.active() || InternalErrors.isReporting()) return;
        lifecycle.readLock().lock();
        try {
            if (closed) return;
            CallbackGuard.enter();
            var event = new LogEvent(clock.instant(), level, name, Thread.currentThread().getName(),
                    MessageFormatter.format(template, arguments), MessageFormatter.exceptionText(cause));
            appenders.forEach((appenderName, appender) -> {
                try { appender.append(event); }
                catch (Exception failure) { InternalErrors.report(errors, "append " + appenderName, failure); }
            });
        } finally {
            CallbackGuard.exit();
            lifecycle.readLock().unlock();
        }
    }

    /**
     * Per-destination fence for calls completed before this invocation. Concurrent
     * new log calls may cross the fence; no atomic cross-destination snapshot.
     * Never hold the context lock while waiting on a potentially stalled async sink.
     * A flush begun after context close starts is a no-op, including after timeout.
     */
    public void flush() throws IOException {
        CallbackGuard.rejectControlOperation();
        if (!closed) finishAppenders(false);
    }

    @Override public void close() throws IOException {
        CallbackGuard.rejectControlOperation();
        // A BLOCK producer holds a read lock. Wake it BEFORE waiting for write ownership.
        closed = true;
        for (Appender appender : appenders.values())
            if (appender instanceof AsyncAppender async) async.stopAccepting();
        lifecycle.writeLock().lock();
        try {
            if (resourcesClosed) return;
            resourcesClosed = true;
            loggers.clear();
            finishAppenders(true);
        } finally { lifecycle.writeLock().unlock(); }
    }

    private void finishAppenders(boolean closing) throws IOException {
        IOException combined = null;
        for (var entry : appenders.entrySet()) {
            try {
                CallbackGuard.enter();
                if (closing) entry.getValue().close();
                else entry.getValue().flush();
            } catch (Exception failure) {
                if (combined == null) combined = new IOException("One or more appenders failed during " + (closing ? "close" : "flush"));
                combined.addSuppressed(failure);
                InternalErrors.report(errors, (closing ? "close " : "flush ") + entry.getKey(), failure);
            } finally { CallbackGuard.exit(); }
        }
        if (combined != null) throw combined;
    }
}
