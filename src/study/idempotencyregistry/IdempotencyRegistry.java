package study.idempotencyregistry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/** Basic duplicate admission only; does not execute work or store its result. */
public final class IdempotencyRegistry {
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> registered = new HashSet<>();

    public boolean tryRegister(String key) {
        Objects.requireNonNull(key, "key");
        if (key.isBlank()) throw new IllegalArgumentException("Key must not be blank");
        lock.lock();
        try {
            // Test-and-add is one operation: only the first caller gets permission.
            // Retain the key even if the caller's later work fails; no automatic retry.
            return registered.add(key);
        } finally {
            lock.unlock();
        }
    }
}
