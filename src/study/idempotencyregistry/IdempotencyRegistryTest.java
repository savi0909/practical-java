package study.idempotencyregistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class IdempotencyRegistryTest {
    public static void main(String[] args) throws Exception {
        IdempotencyRegistry registry = new IdempotencyRegistry();
        check(registry.tryRegister("payment-1"), "First registration wins");
        check(!registry.tryRegister("payment-1"), "Duplicate must lose");
        check(registry.tryRegister("payment-2"), "Different key is independent");

        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                results.add(pool.submit(() -> registry.tryRegister("shared")));
            }
            int winners = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) winners++;
            }
            check(winners == 1, "Exactly one concurrent registration wins");
        } finally {
            pool.shutdownNow();
        }
        System.out.println("IdempotencyRegistryTest: 3 checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
