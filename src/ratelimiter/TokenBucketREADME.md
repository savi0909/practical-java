# Token-bucket rate limiter — first implementation

Java 17, package `ratelimiter`, plain standard library. Start with `TokenBucketRateLimiter.java`; learning comments explain the state and atomic operation. See README.md for the shared client-facing design.

## Contract

- One shared instance in one JVM, concurrent callers, independent bucket per non-null client ID.
- Each bucket starts full. Capacity bounds the burst; a positive finite tokens-per-second rate controls continuous refill. One accepted call consumes one token.
- Immediate allow/reject, no queuing, fairness, or caller-order guarantee. Exactly one available token is enough; fractions remain for later calls.
- In-memory only; restarts give fresh buckets. No expiry or cleanup yet, so inactive clients remain stored.

## Approach

Store each client's available tokens and last refill time. On a call, add elapsed seconds times refill rate, cap at capacity, then check and consume one token. Use `System.nanoTime()` for elapsed time, with an injectable clock for tests. Synchronize the complete operation: the map and each bucket's mutable state share the instance monitor.

Expected time is O(1) for map work, plus monitor contention; space is O(U) for U distinct clients. There is no refill thread. Double arithmetic approximates fractional tokens and can round near a threshold; this is not exact accounting. Elapsed subtraction assumes clock gaps below 2^63 nanoseconds (about 292 years). A very large computed refill is capped at capacity, including floating-point positive infinity; configuration itself rejects infinity and NaN.

Example: capacity 2, refill 2 tokens/second. Two immediate calls pass; a third fails. After 250 ms only half a token exists, so a call fails. After another 250 ms one token is available and a call passes. There is no fixed-window quota reset.

## Interview explanation

“I separate burst size from refill rate. Each client has a token balance and a monotonic timestamp. Before checking a request, I refill based on elapsed time and cap the balance, then consume one token if available. The entire operation runs under one monitor, so callers cannot spend the same token. The balance stays between zero and capacity, subject to floating-point rounding. Map work is expected constant time, with space proportional to distinct clients. This baseline serializes clients and retains their entries; those are deliberate limits of the first implementation.”

## Follow-ups

- Why not fixed windows? Refill is gradual instead of resetting a whole quota at a boundary.
- Why not just a concurrent map? Refill, check, and consumption must also be atomic for each bucket.
- Production limitations? Retained client entries, global-lock contention, and approximate arithmetic; distributed coordination is outside this lesson.

## Validation

Validation (2026-10-15): package compiled targeting Java 17 with all lint checks and no warnings; all four checks passed. Checks cover initial burst and client independence, fractional refill and capacity, invalid inputs, and concurrent consumption with a frozen clock. Concurrency checks are not proof; correctness relies on the monitor and balance invariant.

```powershell
javac --release 17 -Xlint:all -d out/study-packages src/ratelimiter/*.java
java -cp out/study-packages ratelimiter.TokenBucketRateLimiterTest
```
