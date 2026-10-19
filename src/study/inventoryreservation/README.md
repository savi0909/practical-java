# Concurrent inventory reservation

Java 17. Read `InventoryReservation.java`, then `InventoryReservationTest.java`.

## Contract
- One shared object per product in one JVM; in-memory stock only.
- Initial stock is non-negative; reservation quantity must be positive.
- Reserve all requested units or reject without changing stock; no expiry or release.

## Short tutorial
Keep the available stock as an integer. Hold one `ReentrantLock` while checking
availability and subtracting the requested quantity. Protect stock reads with
the same lock so callers see a consistent value.

```java
var inventory = new InventoryReservation(10);
boolean reserved = inventory.reserve(3);
int remaining = inventory.available(); // 7
```

The check and subtraction are atomic, so stock cannot become negative.
Each operation does O(1) work plus lock waiting; space is O(1) per product.

## Follow-up
This is a permanent stock deduction. Reservation IDs, cancellation, payment,
expiry, and coordination across products or servers are later extensions.

## Validation
2026-10-15: compiled with `javac --release 17 -Xlint:all -Werror`; three basic
checks passed: reserve/reject behavior, invalid quantity, and 30 concurrent
attempts against 10 units. The small concurrent check is not proof; the locked
check-and-subtract preserves non-negative stock.
