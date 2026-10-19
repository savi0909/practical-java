# Idempotency-key registry

Java 17. Read `IdempotencyRegistry.java`, then `IdempotencyRegistryTest.java`.

## Contract
- One shared instance in one JVM; in memory, lost on restart.
- Non-blank, case-sensitive keys; exactly one registration succeeds per key.
- No expiry, result replay, or retry after failure; keys stay registered.

## Short tutorial
Keep the accepted keys in a set. `tryRegister` adds a key while holding a
`ReentrantLock` and returns whether it was new. The caller performs its work
only after a true result, outside the registry lock.

```java
var registry = new IdempotencyRegistry();
boolean mayStart = registry.tryRegister("customer-7:payment-123");
// Start work only when mayStart is true.
```

The locked set addition gives one winning caller even under concurrency.
Average work is O(1) per call, plus lock waiting; space is O(k) registered keys.

## Follow-up
This only suppresses duplicate admission. A crash after registration may leave
work undone; exactly-once side effects require coordination with durable work/results.

## Validation
2026-10-15: compiled with `javac --release 17 -Xlint:all -Werror`; three basic
checks passed: duplicate rejection, independent keys, and one winner across 20
calls on four threads. The concurrent check is evidence, not proof; the lock
covering set addition establishes the invariant.
