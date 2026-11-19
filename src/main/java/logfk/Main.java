package logfk;

import java.nio.file.Path;

/** Run with an optional explicit properties path; no arguments uses INFO + stderr. */
public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        if (args.length > 1) throw new IllegalArgumentException("Usage: logfk.Main [logging.properties]");
        LoggingConfig config = args.length == 0 ? LoggingConfig.defaults() : PropertiesConfigLoader.load(Path.of(args[0]));
        try (var context = new LoggingContext(config)) {
            Logger logger = context.getLogger("logfk.demo.Main");
            logger.trace("Trace details are filtered");
            logger.debug("Starting {}", "logfk");
            logger.info("Service {} started on port {}", "orders", 8080);
            logger.warn("Queue utilization {}%", 85);
            logger.error("Example failure", new IllegalStateException("demonstration only"));
            context.getLogger("logfk.noisy.Worker").info("Hidden by OFF when the example config is loaded");
            context.flush();
            for (var spec : config.appenders()) {
                if (context.getAppender(spec.name()) instanceof AsyncAppender async)
                    System.out.println("Async " + spec.name() + ": delegateCalls=" + async.deliveredCount()
                            + ", dropped=" + async.droppedCount() + ", failures=" + async.failureCount());
            }
        }
    }
}
