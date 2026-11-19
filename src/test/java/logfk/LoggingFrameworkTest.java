package logfk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class LoggingFrameworkTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"), ZoneOffset.UTC);
    private static final ErrorHandler FAIL_ON_ERROR = (operation, error) -> fail(operation, error);
    @TempDir Path directory;

    private LoggingContext context(Level level, Map<String, Level> overrides, Appender appender) {
        return new LoggingContext(level, overrides, Map.of("test", appender), CLOCK, FAIL_ON_ERROR);
    }

    private LogEvent event(String message) {
        return new LogEvent(CLOCK.instant(), Level.INFO, "test", "caller", message, null);
    }

    @Test void levelsAndOffHaveExplicitThresholdSemantics() {
        assertTrue(Level.DEBUG.allows(Level.ERROR));
        assertTrue(Level.INFO.allows(Level.INFO));
        assertFalse(Level.INFO.allows(Level.DEBUG));
        assertFalse(Level.OFF.allows(Level.ERROR));
        assertFalse(Level.TRACE.allows(Level.OFF));
    }

    @Test void hierarchyCacheDefaultsAndAppenderThresholdWorkTogether() throws Exception {
        var memory = new InMemoryAppender(Level.WARN, 20);
        try (var ctx = context(Level.INFO, Map.of("shop", Level.DEBUG, "shop.noisy", Level.OFF,
                "shop.noisy.important", Level.ERROR), memory)) {
            var logger = ctx.getLogger("shop.orders.Checkout");
            assertSame(logger, ctx.getLogger("shop.orders.Checkout"));
            assertEquals("shop.orders.Checkout", logger.name());
            assertTrue(logger.isEnabled(Level.DEBUG));
            assertFalse(ctx.getLogger("shopping.Checkout").isEnabled(Level.DEBUG));
            assertFalse(ctx.getLogger("shop.noisy.Worker").isEnabled(Level.ERROR));
            assertTrue(ctx.getLogger("shop.noisy.important.Worker").isEnabled(Level.ERROR));
            logger.debug("filtered by appender");
            logger.warn("retained");
            assertEquals(List.of("retained"), memory.events().stream().map(LogEvent::message).toList());
        }
        assertEquals(Level.INFO, LoggingConfig.defaults().rootLevel());
    }

    @Test void capturesMetadataFormatsArgumentsAndExceptionsOnceOnCaller() throws Exception {
        var memory = new InMemoryAppender(Level.TRACE, 20);
        var renders = new AtomicInteger();
        Object value = new Object() {
            @Override public String toString() { return "v" + renders.incrementAndGet(); }
        };
        try (var ctx = context(Level.DEBUG, Map.of(), memory)) {
            var logger = ctx.getLogger(LoggingFrameworkTest.class);
            logger.trace("{}", value);
            assertEquals(0, renders.get());
            logger.info("value={} null={} missing={}", value, null);
            logger.log(Level.ERROR, new IllegalStateException("broken"), "Order {}", 42);
            assertEquals(1, renders.get());
            var captured = memory.events().getFirst();
            assertEquals("value=v1 null=null missing={}", captured.message());
            assertEquals(CLOCK.instant(), captured.timestamp());
            assertEquals(Thread.currentThread().getName(), captured.threadName());
            assertEquals(LoggingFrameworkTest.class.getName(), captured.loggerName());
            assertTrue(memory.events().getLast().exceptionText().contains("IllegalStateException: broken"));
            assertThrows(NullPointerException.class, () -> logger.info(null));
            assertThrows(IllegalArgumentException.class, () -> logger.log(Level.OFF, "invalid"));
            logger.info("");
            logger.info("extra", 1, 2);
            assertEquals("extra [1, 2]", memory.events().getLast().message());
        }
    }

    @Test void badToStringAndRecursiveLoggingDoNotBreakPipeline() throws Exception {
        var memory = new InMemoryAppender(Level.TRACE, 10);
        try (var ctx = context(Level.TRACE, Map.of(), memory)) {
            var logger = ctx.getLogger("test");
            logger.info("{}", new Object() {
                @Override public String toString() {
                    logger.info("recursive");
                    throw new IllegalStateException("bad renderer");
                }
            });
            assertEquals(1, memory.events().size());
            assertTrue(memory.events().getFirst().message().contains("toString failed"));
        }
    }

    @Test void destinationFailureAndFailingErrorHandlerAreIsolated() throws Exception {
        var memory = new InMemoryAppender(Level.TRACE, 10);
        var failures = new AtomicInteger();
        Appender broken = new Appender() {
            public void append(LogEvent e) throws IOException { throw new IOException("disk full"); }
        };
        try (var ctx = new LoggingContext(Level.INFO, Map.of(), Map.of("bad", broken, "good", memory), CLOCK,
                (operation, failure) -> { failures.incrementAndGet(); throw new IllegalStateException(); })) {
            ctx.getLogger("test").info("still delivered");
            assertEquals(1, failures.get());
            assertEquals(1, memory.events().size());
        }
    }

    @Test void memoryRetentionIsBoundedAndSnapshotsAreImmutable() throws Exception {
        var memory = new InMemoryAppender(Level.INFO, 2);
        memory.append(event("a")); memory.append(event("b")); memory.append(event("c"));
        assertEquals(List.of("b", "c"), memory.events().stream().map(LogEvent::message).toList());
        assertEquals(1, memory.evictedCount());
        assertThrows(UnsupportedOperationException.class, () -> memory.events().clear());
        memory.close(); memory.close(); memory.append(event("ignored"));
        assertEquals(2, memory.events().size());
    }

    @Test void writerBufferingFlushAndResourceOwnershipAreExplicit() throws Exception {
        class TrackedWriter extends StringWriter {
            int flushes; boolean closed;
            @Override public void flush() { flushes++; }
            @Override public void close() { closed = true; }
        }
        var output = new TrackedWriter();
        var appender = new WriterAppender(Level.TRACE, new SimpleTextLayout(), output, false, false);
        appender.append(event("hello"));
        assertEquals(0, output.flushes);
        appender.flush(); assertEquals(1, output.flushes);
        appender.close(); appender.close();
        assertFalse(output.closed);
        assertTrue(output.toString().contains("INFO test - hello"));
    }

    @Test void fileOutputIsUtf8AppendModeAndCloseFlushesBufferedData() throws Exception {
        Path path = directory.resolve("nested/application.log");
        var spec = AppenderConfig.file("file", path).withBuffering(8192, false);
        for (String text : List.of("hello 世界", "second")) {
            try (var ctx = new LoggingContext(new LoggingConfig(Level.INFO, Map.of(), List.of(spec)))) {
                ctx.getLogger("test").info(text);
                ctx.flush();
                assertTrue(Files.readString(path).contains(text));
            }
        }
        assertEquals(2, Files.readAllLines(path).size());
    }

    @Test void jsonLayoutEscapesControlCharactersAndExceptionText() {
        String result = new JsonLayout().format(new LogEvent(CLOCK.instant(), Level.ERROR,
                "test", "thread", "quote\" line\n tab\t\\\u0001", "failure\n"));
        assertTrue(result.contains("quote\\\" line\\n tab\\t\\\\\\u0001"));
        assertTrue(result.contains("\"exception\":\"failure\\n\""));
        assertEquals(1, result.lines().count());
    }

    @Test void configurationLoadsRelativePathsOverridesBufferingAndAsync() throws Exception {
        Path file = directory.resolve("logging.properties");
        Files.writeString(file, """
                root.level=INFO
                logger.shop.orders.level=DEBUG
                appenders=console,file,memory
                appender.console.type=CONSOLE
                appender.console.target=STDOUT
                appender.file.type=FILE
                appender.file.path=logs/output.log
                appender.file.layout=JSON
                appender.file.immediateFlush=false
                appender.file.async=true
                appender.file.queueCapacity=8
                appender.file.overflowPolicy=DROP_NEWEST
                appender.memory.type=MEMORY
                appender.memory.capacity=3
                """, StandardCharsets.UTF_8);
        var config = PropertiesConfigLoader.load(file);
        assertEquals(Level.DEBUG, config.loggerLevels().get("shop.orders"));
        assertEquals(directory.resolve("logs/output.log"), config.appenders().get(1).path());
        assertNotNull(config.appenders().get(1).async());
        try (var ctx = new LoggingContext(config)) {
            ctx.getLogger("shop.orders.Service").debug("configured");
        }
        assertTrue(Files.readString(directory.resolve("logs/output.log")).contains("configured"));
    }

    @Test void configurationRejectsTyposDuplicatesInvalidValuesAndDuplicatePaths() throws Exception {
        String base = "root.level=INFO\nappenders=out\nappender.out.type=CONSOLE\n";
        for (String extra : List.of("unknown=true\n", "root.level=DEBUG\n", "appender.out.level=NOPE\n",
                "appender.out.path=file.log\n", "appender.out.queueCapacity=0\n", "logger.level=DEBUG\n")) {
            Path file = directory.resolve("bad.properties");
            Files.writeString(file, base + extra);
            assertThrows(IllegalArgumentException.class, () -> PropertiesConfigLoader.load(file), extra);
        }
        assertThrows(IllegalArgumentException.class, () -> new LoggingConfig(Level.INFO, Map.of(),
                List.of(AppenderConfig.file("one", directory.resolve("a.log")),
                        AppenderConfig.file("two", directory.resolve("./a.log")))));
        assertThrows(IllegalArgumentException.class, () -> new LoggingConfig(Level.INFO, Map.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new InMemoryAppender(Level.INFO, 0));
        assertThrows(IllegalArgumentException.class, () -> AppenderConfig.file("blank", Path.of("")));
    }

    @Test void concurrentProducersPreserveWholeRecordsAndCloseIsIdempotent() throws Exception {
        var writer = new StringWriter();
        var output = new WriterAppender(Level.TRACE, e -> e.message() + "\n", writer, false, false);
        var ctx = context(Level.INFO, Map.of(), output);
        var logger = ctx.getLogger("test");
        try (var pool = Executors.newFixedThreadPool(6)) {
            for (int worker = 0; worker < 6; worker++) {
                final int id = worker;
                pool.submit(() -> { for (int n = 0; n < 100; n++) logger.info("worker={}/{}", id, n); });
            }
        }
        ctx.close(); ctx.close(); logger.info("ignored after close");
        assertFalse(logger.isEnabled(Level.INFO));
        assertEquals(600, writer.toString().lines().distinct().count());
        assertThrows(IllegalStateException.class, () -> ctx.getLogger("new"));
    }

    @Test void asyncDropPolicyIsBoundedAndCloseDrainsAcceptedEvents() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var memory = new InMemoryAppender(Level.TRACE, 10);
        Appender slow = new Appender() {
            public void append(LogEvent e) throws IOException {
                started.countDown();
                try { if (!release.await(5, TimeUnit.SECONDS)) throw new IOException("test gate timeout"); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IOException(ex); }
                memory.append(e);
            }
            public void close() { memory.close(); }
        };
        var async = new AsyncAppender("test", slow,
                new AsyncConfig(1, OverflowPolicy.DROP_NEWEST, Duration.ofSeconds(5)), FAIL_ON_ERROR);
        try {
            async.append(event("first")); assertTrue(started.await(5, TimeUnit.SECONDS));
            async.append(event("second")); async.append(event("dropped"));
            assertEquals(1, async.droppedCount());
        } finally { release.countDown(); async.close(); }
        assertEquals(List.of("first", "second"), memory.events().stream().map(LogEvent::message).toList());
        assertEquals(2, async.deliveredCount());
        async.close();
    }

    @Test void asyncBlockFlushAndCallerSnapshotWork() throws Exception {
        var memory = new InMemoryAppender(Level.TRACE, 1000);
        var async = new AsyncAppender("test", memory,
                new AsyncConfig(2, OverflowPolicy.BLOCK, Duration.ofSeconds(5)), FAIL_ON_ERROR);
        try (var ctx = context(Level.INFO, Map.of(), async)) {
            var logger = ctx.getLogger("test");
            var mutable = new StringBuilder("before");
            logger.info("{}", mutable); mutable.append("-after");
            for (int i = 0; i < 100; i++) logger.info("{}", i);
            ctx.flush();
            assertEquals(101, memory.events().size());
            assertEquals("before", memory.events().getFirst().message());
            assertEquals(Thread.currentThread().getName(), memory.events().getFirst().threadName());
            assertEquals(0, async.droppedCount());
        }
    }
}
