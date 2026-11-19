/*
 * Interview critique:
 * - Complexity: this interface has no algorithm or storage; rendering and I/O costs
 *   belong to the implementation.
 * - Concurrency: stateless contract; implementations must make shared logger calls safe.
 * - Edge cases: reject null messages; allow empty messages and repeated calls.
 * - Scale: synchronous output can block callers; bounded queues and batching trade
 *   caller latency for delivery lag and an explicit overflow policy.
 * - Alternative: this common dispatch method keeps convenience methods consistent;
 *   specialized fixed-arity overloads could avoid varargs allocations, at API cost.
 */
package logfk;

/**
 * Application-facing logging contract. Implementations decide whether and where
 * each message is written. Implementations must reject null messages with
 * {@link NullPointerException}; empty messages are allowed.
 */
public interface Logger {

    String name();
    boolean isEnabled(Level level);
    void log(Level level, String template, Object... arguments);
    void log(Level level, Throwable cause, String template, Object... arguments);

    /** Logs fine-grained execution details. */
    default void trace(String message) { log(Level.TRACE, message); }
    default void trace(String template, Object... arguments) { log(Level.TRACE, template, arguments); }

    /** Logs information useful for debugging. */
    default void debug(String message) { log(Level.DEBUG, message); }
    default void debug(String template, Object... arguments) { log(Level.DEBUG, template, arguments); }

    /** Logs normal application activity. */
    default void info(String message) { log(Level.INFO, message); }
    default void info(String template, Object... arguments) { log(Level.INFO, template, arguments); }

    /** Logs a potentially problematic situation. */
    default void warn(String message) { log(Level.WARN, message); }
    default void warn(String template, Object... arguments) { log(Level.WARN, template, arguments); }

    /** Logs a failure. */
    default void error(String message) { log(Level.ERROR, message); }
    default void error(String template, Object... arguments) { log(Level.ERROR, template, arguments); }
    default void error(String message, Throwable cause) { log(Level.ERROR, cause, message); }
}
