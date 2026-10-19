package study.idgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Small timing demo, not a rigorous JVM benchmark. Never resets counters. */
public class IdComparisonHarness {
    private static volatile long sink;

    public static void main(String[] args) throws Exception {
        IdProvider provider = IdProviderImpl.getInstance();
        LongSupplier[] methods = {provider::getId, provider::getSynchronizedId, provider::getLockedId};
        String[] names = {"CAS", "synchronized", "ReentrantLock"};
        boolean virtual = args.length > 0 && args[0].equals("virtual");
        int callsPerThread = 200_000;

        System.out.println((virtual ? "Virtual" : "Platform")
                + " threads. Independent sequences. Median of 3 runs; lower elapsed time wins this run.");
        for (int threads : new int[]{1, 100}) {
            // Warm both code paths before measuring.
            for (LongSupplier method : methods) {
                run(method, threads, 50_000, virtual);
            }
            long[][] times = new long[methods.length][3];
            for (int round = 0; round < 3; round++) {
                // Rotate order so each method runs first once.
                for (int offset = 0; offset < methods.length; offset++) {
                    int method = (round + offset) % methods.length;
                    times[method][round] = run(methods[method], threads, callsPerThread, virtual);
                }
            }
            System.out.printf(Locale.ROOT, "%d thread(s), %,d IDs:",
                    threads, (long) threads * callsPerThread);
            for (int method = 0; method < methods.length; method++) {
                Arrays.sort(times[method]);
                System.out.printf(Locale.ROOT, " %s %.2f ms", names[method], times[method][1] / 1_000_000.0);
            }
            System.out.println();
        }
        System.out.println("JIT, scheduling and machine load affect results. Use JMH for reliable benchmarking.");
    }

    private static long run(LongSupplier nextId, int threads, int calls, boolean virtual) throws Exception {
        ExecutorService pool = virtual ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Long>> results = new ArrayList<>();
        try {
            for (int worker = 0; worker < threads; worker++) {
                results.add(pool.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Start timeout");
                    }
                    long checksum = 0;
                    for (int i = 0; i < calls; i++) {
                        checksum ^= nextId.getAsLong();
                    }
                    return checksum;
                }));
            }
            if (!ready.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Workers not ready");
            }
            // Excludes thread creation; includes release and result collection overhead.
            long begin = System.nanoTime();
            start.countDown();
            long checksum = 0;
            for (Future<Long> result : results) {
                checksum ^= result.get(60, TimeUnit.SECONDS);
            }
            long elapsed = System.nanoTime() - begin;
            sink = checksum; // Keep returned IDs observable, outside the timed section.
            return elapsed;
        } finally {
            pool.shutdownNow();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Workers did not stop");
            }
        }
    }
}
