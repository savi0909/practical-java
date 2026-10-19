# Simple delayed-task scheduler

Java 17. Read `DelayedTaskScheduler.java`, then `DelayedTaskSchedulerTest.java`.

## Contract
- One JVM, in-memory one-off tasks, one worker; no persistence or retries.
- Non-negative relative delay fitting in long nanoseconds; zero means eligible now.
- Tasks run after their delay, possibly later when the worker is busy.
- Close rejects submissions and cancels pending tasks; running work may finish.

## Short tutorial
Use a `ScheduledThreadPoolExecutor` with one worker. The JDK handles relative
time, the delayed queue, and execution. Our `ReentrantLock` makes checking the
closed flag and submitting atomic with close. Task bodies run outside our lock.

```java
try (var scheduler = new DelayedTaskScheduler()) {
    var future = scheduler.schedule(() -> System.out.println("hello"),
            java.time.Duration.ofMillis(100));
    future.get(); // Wait here; closing immediately would cancel pending work.
}
```

The returned future exposes cancellation and task failure. Tasks are selected by
due time; one slow task delays others. Queue insertion is O(log n) plus lock
waiting; storage is O(n) pending tasks. Task execution time is separate.

## Follow-up
Close does not wait for termination or interrupt running work. Always close the
scheduler; its worker can keep the JVM alive. The pending queue is unbounded.
A custom delayed queue/worker can be studied later.

## Validation
2026-10-15: compiled with `javac --release 17 -Xlint:all -Werror`; three basic
checks passed: delayed execution, failure reporting with subsequent progress,
and close cancellation/rejection. These checks do not establish timing guarantees
under load; lifecycle correctness relies on the lock and JDK executor contract.
