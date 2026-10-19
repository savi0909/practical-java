# Expiring in-memory store

Java 17. Read `ExpiringStore.java`, then `ExpiringStoreTest.java`.

## Contract
- Share one instance within one JVM; no persistence or distributed coordination.
- Non-null keys/values; positive TTL fitting in long nanoseconds. Put replaces value and TTL.
- Get returns null at or after expiry. Remove reports whether it removed a live value.

## Short tutorial
Store a value, its creation time, and its TTL in a map. On get, compare elapsed
`System.nanoTime()` against the TTL; delete and return null when expired.
One `ReentrantLock` protects lookup, expiry checks, replacement, and removal.

```java
var store = new ExpiringStore<String, String>();
store.put("session", "alice", java.time.Duration.ofMinutes(5));
String value = store.get("session");
```

The lock prevents an expiry check from deleting a concurrent replacement.
Average map work is O(1) per call, plus lock waiting; space is O(n) stored entries.
Elapsed-time comparisons assume gaps below 2^63 nanoseconds (about 292 years).

## Follow-up
Expired untouched keys remain in memory. A periodic cleanup scan can be added later.
Returned mutable values need their own protection; this lock protects the store.

## Validation
2026-10-15: compiled with `javac --release 17 -Xlint:all -Werror`; three basic
checks passed: expiry boundary, replacement/removal, and clock wrap/input rejection.
No exhaustive concurrency testing; correctness relies on the complete locked operation.
