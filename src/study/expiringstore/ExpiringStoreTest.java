package study.expiringstore;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class ExpiringStoreTest {
    public static void main(String[] args) {
        AtomicLong now = new AtomicLong();
        ExpiringStore<String, String> store = new ExpiringStore<>(now::get);
        store.put("key", "first", Duration.ofNanos(10));
        check("first".equals(store.get("key")), "Value must be readable before expiry");
        now.set(10);
        check(store.get("key") == null, "Value expires exactly at TTL");

        store.put("key", "old", Duration.ofNanos(10));
        now.set(15);
        store.put("key", "new", Duration.ofNanos(20));
        now.set(20);
        check("new".equals(store.get("key")), "Replacement must have a fresh TTL");
        check(store.remove("key") && !store.remove("key"), "Remove reports a live entry once");

        now.set(Long.MAX_VALUE - 5);
        store.put("wrap", "value", Duration.ofNanos(10));
        now.set(Long.MIN_VALUE + 4);
        check(store.get("wrap") == null, "Elapsed subtraction must handle nanoTime wraparound");
        try {
            store.put("bad", "value", Duration.ZERO);
            throw new AssertionError("Zero TTL must fail");
        } catch (IllegalArgumentException expected) {
            // Expected input rejection.
        }
        System.out.println("ExpiringStoreTest: 3 checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
