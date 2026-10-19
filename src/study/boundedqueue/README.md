# Bounded blocking queue

Java 17. Read `BoundedBlockingQueue.java`, then `BoundedBlockingQueueTest.java`.

## Contract
- One in-memory queue in one JVM with positive capacity and non-null items.
- FIFO by insertion order under the lock; no fairness guarantee between callers.
- Put blocks when full; take blocks when empty. Both waits are interruptible.

## Short tutorial
An `ArrayDeque` stores the items. One `ReentrantLock` protects it, with `notFull`
and `notEmpty` conditions. Waiting releases the lock; successful put/take signals
the opposite side. Always recheck the condition in a while loop after waking.

```java
var queue = new BoundedBlockingQueue<String>(2);
queue.put("job"); // May throw InterruptedException.
String job = queue.take();
```

The lock keeps size between zero and capacity, and protects FIFO removal.
Deque work is amortized O(1); waiting is unbounded. Space is O(capacity).

## Follow-up
There is no close or timeout API. Interrupt a waiting thread to stop it.
For ordinary application code, Java's `ArrayBlockingQueue` already provides this behavior.

## Validation
2026-10-15: compiled with `javac --release 17 -Xlint:all -Werror`; three basic
checks passed: FIFO, full producer waiting/resumption, and empty consumer
waiting/resumption. Timed observations do not prove every thread schedule;
correctness follows from the lock, condition loops, and signalling.
