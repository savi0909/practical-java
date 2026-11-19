package logfk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class LifecycleTest {
    @TempDir Path directory;
    private static LogEvent event(String message) {
        return new LogEvent(Instant.EPOCH, Level.INFO, "test", "caller", message, null);
    }
    private static final ErrorHandler ERRORS = (operation, failure) -> { };

    private static class GateAppender implements Appender {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final InMemoryAppender memory = new InMemoryAppender(Level.TRACE, 10);
        final AtomicBoolean closed = new AtomicBoolean();
        @Override public void append(LogEvent event) throws IOException {
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) throw new IOException("test gate timed out");
            } catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IOException(ex); }
            assertFalse(closed.get(), "delegate was closed during delivery");
            memory.append(event);
        }
        @Override public void close() { closed.set(true); memory.close(); }
    }

    @Test void closeDeadlineDoesNotCloseDelegateUnderActiveWriteAndWakesBlockedProducer() throws Exception {
        var delegate = new GateAppender();
        var async = new AsyncAppender("deadline", delegate,
                new AsyncConfig(1, OverflowPolicy.BLOCK, Duration.ofMillis(50)), ERRORS);
        var producerStarted = new CountDownLatch(1);
        var producerFinished = new CountDownLatch(1);
        Thread producer = new Thread(() -> {
            producerStarted.countDown();
            try { async.append(event("third")); }
            catch (IOException failure) { throw new AssertionError(failure); }
            finally { producerFinished.countDown(); }
        });
        try {
            async.append(event("first")); assertTrue(delegate.started.await(3, TimeUnit.SECONDS));
            async.append(event("second"));
            producer.start(); assertTrue(producerStarted.await(3, TimeUnit.SECONDS));
            assertThrows(IOException.class, async::close);
            assertTrue(producerFinished.await(3, TimeUnit.SECONDS));
            assertFalse(delegate.closed.get());
        } finally {
            delegate.release.countDown(); producer.join(3000); async.close();
        }
        assertEquals(List.of("first", "second"), delegate.memory.events().stream().map(LogEvent::message).toList());
        assertTrue(delegate.closed.get());
    }

    @Test void interruptedBlockedProducerRestoresInterruptAndDoesNotEnqueue() throws Exception {
        var delegate = new GateAppender();
        var async = new AsyncAppender("interrupt", delegate,
                new AsyncConfig(1, OverflowPolicy.BLOCK, Duration.ofSeconds(5)), ERRORS);
        var producerStarted = new CountDownLatch(1);
        var interrupted = new AtomicBoolean();
        Thread producer = new Thread(() -> {
            producerStarted.countDown();
            try { async.append(event("interrupted")); }
            catch (IOException expected) { interrupted.set(Thread.currentThread().isInterrupted()); }
        });
        try {
            async.append(event("first")); assertTrue(delegate.started.await(3, TimeUnit.SECONDS));
            async.append(event("second"));
            producer.start(); assertTrue(producerStarted.await(3, TimeUnit.SECONDS));
            producer.interrupt(); producer.join(3000);
            assertFalse(producer.isAlive()); assertTrue(interrupted.get());
            assertEquals(1, async.droppedCount());
        } finally { delegate.release.countDown(); producer.join(3000); async.close(); }
        assertEquals(2, async.deliveredCount());
    }

    @Test void asyncFailureDoesNotKillWorkerAndFlushFailureReachesCaller() throws Exception {
        var writes = new AtomicInteger();
        Appender failing = new Appender() {
            public void append(LogEvent event) throws IOException {
                if (writes.getAndIncrement() == 0) throw new IOException("first write fails");
            }
            public void flush() throws IOException { throw new IOException("flush fails"); }
        };
        var async = new AsyncAppender("failure", failing,
                new AsyncConfig(4, OverflowPolicy.BLOCK, Duration.ofSeconds(5)), ERRORS);
        try {
            async.append(event("fails")); async.append(event("succeeds"));
            assertThrows(IOException.class, async::flush);
            assertEquals(2, writes.get()); assertEquals(1, async.deliveredCount());
            assertEquals(2, async.failureCount());
        } finally { async.close(); }
    }

    @Test void contextClosesAllDestinationsEvenWhenOneCloseFails() {
        var closes = new AtomicInteger();
        Appender failing = new Appender() {
            public void append(LogEvent event) { }
            public void close() throws IOException { closes.incrementAndGet(); throw new IOException("close fails"); }
        };
        Appender good = new Appender() {
            public void append(LogEvent event) { }
            public void close() { closes.incrementAndGet(); }
        };
        var context = new LoggingContext(Level.INFO, Map.of(), Map.of("bad", failing, "good", good), Clock.systemUTC(), ERRORS);
        assertThrows(IOException.class, context::close);
        assertEquals(2, closes.get());
        assertDoesNotThrow(context::close);
    }

    @Test void partialStartupClosesAlreadyOpenedAsyncFile() throws Exception {
        Path first = directory.resolve("first.log");
        var config = new LoggingConfig(Level.INFO, Map.of(), List.of(
                AppenderConfig.file("first", first).withAsync(new AsyncConfig(2, OverflowPolicy.BLOCK, Duration.ofSeconds(5))),
                AppenderConfig.file("invalid", directory)));
        assertThrows(IOException.class, () -> new LoggingContext(config));
        Files.move(first, directory.resolve("released.log"));
        assertFalse(Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.getName().equals("logfk-first") && thread.isAlive()));
    }

    @Test void callbackCannotCloseContextDuringDispatch() throws Exception {
        var memory = new InMemoryAppender(Level.TRACE, 10);
        try (var context = new LoggingContext(Level.INFO, Map.of(), Map.of("memory", memory), Clock.systemUTC(), ERRORS)) {
            context.getLogger("test").info("{}", new Object() {
                @Override public String toString() {
                    assertThrows(IllegalStateException.class, context::close);
                    return "safe";
                }
            });
            assertEquals("safe", memory.events().getFirst().message());
        }
    }

    @Test void contextCloseStopsBlockedAsyncAdmissionBeforeWaitingForDispatch() throws Exception {
        var delegate = new GateAppender();
        var async = new AsyncAppender("context-stop", delegate,
                new AsyncConfig(1, OverflowPolicy.BLOCK, Duration.ofMillis(100)), ERRORS);
        var context = new LoggingContext(Level.INFO, Map.of(), Map.of("async", async), Clock.systemUTC(), ERRORS);
        var logger = context.getLogger("test");
        Thread producer = new Thread(() -> logger.info("third"));
        var closed = new CountDownLatch(1);
        var closeFailure = new AtomicReference<Exception>();
        Thread closer = new Thread(() -> {
            try { context.close(); } catch (Exception failure) { closeFailure.set(failure); }
            finally { closed.countDown(); }
        });
        try {
            logger.info("first"); assertTrue(delegate.started.await(3, TimeUnit.SECONDS));
            logger.info("second"); producer.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (producer.getState() != Thread.State.WAITING && System.nanoTime() < deadline) Thread.onSpinWait();
            assertEquals(Thread.State.WAITING, producer.getState());
            closer.start();
            assertTrue(closed.await(2, TimeUnit.SECONDS), "close must wake blocked admission before taking lifecycle write lock");
            assertInstanceOf(IOException.class, closeFailure.get());
        } finally {
            delegate.release.countDown(); producer.join(3000); closer.join(3000); context.close(); async.close();
        }
        assertEquals(2, async.acceptedCount());
    }

    @Test void asyncCallbacksCannotReenterLoggingOrFactory() throws Exception {
        var logger = new AtomicReference<Logger>();
        var context = new AtomicReference<LoggingContext>();
        var callbackDone = new CountDownLatch(1);
        var factoryRejected = new AtomicBoolean();
        var memory = new InMemoryAppender(Level.TRACE, 10);
        Appender callback = new Appender() {
            public void append(LogEvent event) {
                try {
                    logger.get().info("recursive");
                    try { context.get().getLogger("callback"); }
                    catch (IllegalStateException expected) { factoryRejected.set(true); }
                } finally { callbackDone.countDown(); }
            }
        };
        var async = new AsyncAppender("callback", callback,
                new AsyncConfig(2, OverflowPolicy.BLOCK, Duration.ofSeconds(5)), ERRORS);
        try (var ctx = new LoggingContext(Level.INFO, Map.of(), Map.of("async", async, "memory", memory), Clock.systemUTC(), ERRORS)) {
            context.set(ctx); logger.set(ctx.getLogger("test"));
            logger.get().info("original"); assertTrue(callbackDone.await(3, TimeUnit.SECONDS));
            assertTrue(factoryRejected.get());
            ctx.flush();
            assertEquals(List.of("original"), memory.events().stream().map(LogEvent::message).toList());
        }
    }

    @Test void concurrentFlushDoesNotPreventAsyncCloseDeadline() throws Exception {
        var delegate = new GateAppender();
        var async = new AsyncAppender("flush-close", delegate,
                new AsyncConfig(4, OverflowPolicy.BLOCK, Duration.ofMillis(100)), ERRORS);
        var context = new LoggingContext(Level.INFO, Map.of(), Map.of("async", async), Clock.systemUTC(), ERRORS);
        var flushFailure = new AtomicReference<Exception>();
        var closeFailure = new AtomicReference<Exception>();
        var closeDone = new CountDownLatch(1);
        Thread flusher = new Thread(() -> { try { context.flush(); } catch (Exception failure) { flushFailure.set(failure); } });
        Thread closer = new Thread(() -> {
            try { context.close(); } catch (Exception failure) { closeFailure.set(failure); }
            finally { closeDone.countDown(); }
        });
        try {
            context.getLogger("test").info("held"); assertTrue(delegate.started.await(3, TimeUnit.SECONDS));
            flusher.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (async.queueSize() == 0 && System.nanoTime() < deadline) Thread.onSpinWait();
            assertEquals(1, async.queueSize(), "flush barrier should be queued behind held output");
            closer.start();
            assertTrue(closeDone.await(2, TimeUnit.SECONDS), "a flush waiter must not hide the close deadline");
            assertInstanceOf(IOException.class, closeFailure.get());
        } finally {
            delegate.release.countDown(); flusher.join(3000); closer.join(3000); context.close(); async.close();
        }
        assertFalse(flusher.isAlive()); assertNull(flushFailure.get());
    }
}
