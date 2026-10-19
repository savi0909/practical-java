package study.boundedqueue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BoundedBlockingQueueTest {
    public static void main(String[] args) throws Exception {
        BoundedBlockingQueue<Integer> fifo = new BoundedBlockingQueue<>(2);
        fifo.put(1);
        fifo.put(2);
        check(Integer.valueOf(1).equals(fifo.take()) && Integer.valueOf(2).equals(fifo.take()),
                "Items must leave in FIFO order");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(1);
            queue.put(10);
            CountDownLatch started = new CountDownLatch(1);
            Future<?> producer = pool.submit(() -> {
                started.countDown();
                queue.put(20);
                return null;
            });
            check(started.await(5, TimeUnit.SECONDS), "Producer must start");
            expectWaiting(producer);
            check(queue.take() == 10, "Taking frees space");
            producer.get(5, TimeUnit.SECONDS);
            check(queue.take() == 20, "Waiting producer must resume");

            CountDownLatch consumerStarted = new CountDownLatch(1);
            Future<Integer> consumer = pool.submit(() -> {
                consumerStarted.countDown();
                return queue.take();
            });
            check(consumerStarted.await(5, TimeUnit.SECONDS), "Consumer must start");
            expectWaiting(consumer);
            queue.put(30);
            check(consumer.get(5, TimeUnit.SECONDS) == 30, "Waiting consumer must resume");
        } finally {
            pool.shutdownNow();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "Workers must stop");
        }
        System.out.println("BoundedBlockingQueueTest: 3 checks passed");
    }

    private static void expectWaiting(Future<?> future) throws Exception {
        try {
            future.get(100, TimeUnit.MILLISECONDS);
            throw new AssertionError("Operation must wait while its condition is false");
        } catch (TimeoutException expected) {
            // Bounded observation of waiting, not a proof for every thread schedule.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
