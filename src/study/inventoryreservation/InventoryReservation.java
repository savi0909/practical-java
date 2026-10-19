package study.inventoryreservation;

import java.util.concurrent.locks.ReentrantLock;

/** One product's available stock, held in memory in one shared instance. */
public final class InventoryReservation {
    private final ReentrantLock lock = new ReentrantLock();
    private int stock;

    public InventoryReservation(int initialStock) {
        if (initialStock < 0) throw new IllegalArgumentException("Stock cannot be negative");
        stock = initialStock;
    }

    public boolean reserve(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        lock.lock();
        try {
            // Checking and subtracting under the same lock prevents overselling.
            if (quantity > stock) return false;
            stock -= quantity;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int available() {
        lock.lock();
        try {
            // Reads need the same protection as writes for a consistent visible value.
            return stock;
        } finally {
            lock.unlock();
        }
    }
}
