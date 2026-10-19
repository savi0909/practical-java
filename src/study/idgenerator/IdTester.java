package study.idgenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public class IdTester {
    public static void main(String[] args) throws Exception {
        IdProvider provider = IdProviderImpl.getInstance();
        checkProvider(provider::getId);
        checkProvider(provider::getSynchronizedId);
        checkProvider(provider::getLockedId);
        IdProvider edge = new IdProviderImpl(Long.MAX_VALUE - 1);
        checkOverflow(edge::getId);
        checkOverflow(edge::getSynchronizedId);
        checkOverflow(edge::getLockedId);
        System.out.println("PASS: CAS, synchronized, ReentrantLock: increasing IDs, overflow, five-thread uniqueness");
    }

    private static void checkProvider(LongSupplier nextId) throws Exception {
        long first = nextId.getAsLong();
        check(nextId.getAsLong() == first + 1, "IDs must increase");

        ExecutorService clients = Executors.newFixedThreadPool(5);
        try {
            List<Future<Long>> results = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                results.add(clients.submit(nextId::getAsLong));
            }
            Set<Long> ids = new HashSet<>();
            for (Future<Long> result : results) {
                check(ids.add(result.get(5, TimeUnit.SECONDS)), "Duplicate ID");
            }
        } finally {
            clients.shutdownNow();
            check(clients.awaitTermination(5, TimeUnit.SECONDS), "Threads did not stop");
        }
    }

    private static void checkOverflow(LongSupplier nextId) {
        check(nextId.getAsLong() == Long.MAX_VALUE, "Last valid ID");
        for (int i = 0; i < 2; i++) {
            try {
                nextId.getAsLong();
                throw new AssertionError("Must reject overflow");
            } catch (IllegalStateException expected) {
                // Exhausted stays exhausted.
            }
        }

    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
