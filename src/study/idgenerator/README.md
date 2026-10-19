# ID provider

## Contract
- One JVM. Threads share one provider. IDs start at 1.
- Unique IDs increase when allocated. Threads may see returns in a different order.
- Restart or a new provider can repeat IDs. No persistence.
- After Long.MAX_VALUE, throw. Never wrap.
- `getId()`, `getSynchronizedId()`, and `getLockedId()` use independent counters for comparison. Each is unique within its own sequence; never combine them as one ID namespace. Counters are never reset.

## Approach
One AtomicLong holds the last ID. Read it, check the limit, then try the update. If another thread wins, retry. The successful compare-and-set is the allocation point.

`getSynchronizedId()` uses a plain long. The synchronized method locks this instance for the complete overflow check and increment. Contending callers wait for the monitor; CAS callers may retry. Both are correct for their separate sequences. Neither promises fairness or bounded latency.

`getLockedId()` uses a separate plain long protected by a default nonfair `ReentrantLock`. The lock covers the overflow check and increment. Always unlock in `finally`, including when overflow throws. No fairness or bounded acquisition-time guarantee. Explicit lock contention may allocate internal waiting nodes; no blanket allocation-free claim applies to this variant.

## Code
- IdProvider.java: interface, keeping getId().
- IdProviderImpl.java: implementation, keeping getInstance().
- IdTester.java: three basic checks.
- IdComparisonHarness.java: all three methods, one and 1,000 platform threads, 200,000 calls per thread, warmup, three rounds rotating method order. No virtual threads. Reports median elapsed time, not individual call or lock latency.

Successful calls create no explicit objects. With r failed updates, time is O(r + 1); space is O(1). No fixed retry limit or fairness promise.

## Say it aloud
“All threads share one counter. A thread gets an ID only if its atomic update succeeds. That prevents duplicates. The limit check prevents overflow. Under contention, a thread may retry. Restart resets the sequence.”

## Quick follow-ups
- Why not synchronized? Also correct. Lock the check and increment together. Measure before claiming either is faster.
- Why not LongAdder? It counts totals; it does not hand out unique tickets.
- Why can ID 2 return before ID 1? A thread can pause after allocating ID 1.

## Run (Java 17+; from workspace root)
```powershell
javac --release 17 -Xlint:all -d out/study-id-provider src/study/idgenerator/*.java
java -cp out/study-id-provider study.idgenerator.IdTester
java -cp out/study-id-provider study.idgenerator.IdComparisonHarness
```

## Checks
Comment update (2026-10-15): provider, interface, and IdTester compiled targeting Java 17 with all lint checks and no warnings; all three methods passed increasing-ID, overflow, and five-thread uniqueness checks. Small concurrent checks are not proof. Whole-package Java 17 compilation currently fails because the existing comparison harness references a Java 21 virtual-thread API; the harness was not changed or run for this comment update.

Latest platform-only measurement (2026-10-15): 1,000 threads x 200,000 IDs = 200 million IDs per method per round; median of three rounds after warmup: CAS 9990.88 ms, synchronized 11603.39 ms, ReentrantLock 3936.95 ms. JDK 21.0.9. No counters reset. Virtual-thread attempt failed its start-gate timeout; a retry was cancelled at the learner's request and has no valid result. Virtual-thread support was then removed; coordination timeouts are 60 seconds. Historical results below are from earlier revisions.

Latest revision (2026-10-15, JDK 21.0.9): all sources compiled targeting Java 21 without lint warnings. All three ID methods passed increasing IDs, overflow rejection, and five-thread uniqueness. Existing rate-limiter checks also passed. Latest 100-thread/20-million-ID medians: platform CAS 877.75 ms, synchronized 1139.01 ms, ReentrantLock 415.12 ms; virtual CAS 1504.44 ms, synchronized 1180.63 ms, ReentrantLock 603.19 ms. Local timing demo only, not a universal ranking or measurement of individual lock latency. Workload is a tight CPU loop with no I/O; it does not isolate virtual-thread pinning. Earlier two-method numbers below are historical.

Verified on 2026-10-15: Java 17-targeted compilation passed without lint warnings. All three basic checks passed: increasing IDs, overflow rejection, and five-thread uniqueness. A small concurrent check is not proof of correctness.

Both methods passed those checks. Latest harness run (2026-10-15, JDK 21.0.9): 200,000 IDs with one thread took CAS 1.51 ms / synchronized 3.36 ms; 20,000,000 IDs with 100 threads took CAS 979.09 ms / synchronized 1092.32 ms. Compilation targeting Java 17 passed without lint warnings. These are local demo measurements, not a general performance ranking. Timing excludes thread creation but includes worker release, checksum work, and result collection. Short warmup, JIT compilation, fixed thread-count order, and machine load can skew results; use JMH for rigorous benchmarking.
