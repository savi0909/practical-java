package study.inventoryreservation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InventoryReservationTest {
    public static void main(String[] args) throws Exception {
        InventoryReservation inventory = new InventoryReservation(5);
        check(inventory.reserve(3), "Available stock can be reserved");
        check(!inventory.reserve(3) && inventory.available() == 2,
                "Rejected reservation must leave stock unchanged");
        try {
            inventory.reserve(0);
            throw new AssertionError("Zero quantity must fail");
        } catch (IllegalArgumentException expected) {
            // Expected input rejection.
        }

        InventoryReservation shared = new InventoryReservation(10);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 30; i++) results.add(pool.submit(() -> shared.reserve(1)));
            int accepted = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) accepted++;
            }
            check(accepted == 10 && shared.available() == 0, "Concurrent calls cannot oversell");
        } finally {
            pool.shutdownNow();
        }
        System.out.println("InventoryReservationTest: 3 checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
