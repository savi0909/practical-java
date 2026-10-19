# Token bucket — increment 1: atomic lazy refill

Each user ID is a tenant. A new bucket starts at its configured capacity.
Every completed 5-second period adds **2 whole tokens**, capped at capacity;
an accepted call consumes one token. A rejected call consumes none.
The period starts when that tenant first appears, independently of other tenants.
There is no refill scan or scheduler.

```java
RateLimiter limiter = new TokenBucketRateLimiter(10);
boolean accepted = limiter.allowRequest("tenant-42");
```

This deliberately uses batch refill: an empty bucket stays empty at 2.5 seconds
and gains two tokens at 5 seconds. Continuous refill at 0.4 tokens/second is a
different policy. Capacity controls the immediate burst, independently of refill
rate; capacity 1 discards one token from each batch.

## Read the implementation in three small parts

1. **State:** `ConcurrentHashMap<String, Bucket>` allocates only for seen tenants.
   Each bucket holds its config revision, integer tokens, and last refill boundary.
   Expected O(1) work/request; O(1) state/tenant and O(U) total storage.
2. **Refill:** divide elapsed milliseconds by the refill period. Add two tokens per
   completed period without exceeding capacity. Clamp the number of useful periods
   before multiplying, so even a huge idle gap cannot overflow token arithmetic.
   Advance the boundary even when refill reaches capacity; excess tokens are lost.
3. **Admission:** inside the same `compute`, check for at least one token and
   decrement exactly once. The callback runs synchronously, so a call-local
   `boolean[]` can carry its answer out without an atomic wrapper.

For capacity 3, starting at t=0:

| Time | Calls | Results | Remaining tokens |
|---|---|---|---|
| 0 ms | 4 | accept, accept, accept, reject | 0 |
| 4,999 ms | 1 | reject | 0 |
| 5,000 ms | 3 | accept, accept, reject | 0 |
| 100,000 ms | 4 | accept, accept, accept, reject | 0 |

If refill happens at 13.5s after a refill boundary at 5s, only one full period
has elapsed: move the boundary to 10s, retaining the remaining 3.5s. Moving it to
13.5s would postpone the next refill incorrectly. Rejected calls also perform lazy
refill calculations; they never consume tokens or reset the period.

## Atomicity and configuration

Keep lookup, lazy reset/refill, and consumption in `ConcurrentHashMap.compute`.
Separate `get` and `put`, or an atomic token counter alone, cannot protect the
relationship between tokens and refill time. Every mutable bucket access is inside
its owning map's callback, which provides synchronization and visibility.
Same-tenant requests serialize; different keys can still contend on colliding bins.
Keep providers fast and nonblocking. No separate per-tenant lock is needed here.

The existing shared provider remains supported: `maxRequests` means burst capacity,
and `windowMillis` means the period for adding two tokens. The capacity constructors
always use 5,000 ms. Inject a provider with 5,000 ms settings for this exercise;
custom periods are an optional configuration capability, not a different default.
Read configuration and time inside `compute`. Snapshot identity denotes a revision;
an update lazily resets that tenant to full. Equal-value updates also reset. An
already running admission may finish using its previously read snapshot.

## Scale limits and next increments

One million users means at most one million bucket entries **if user cardinality
stays within that assumption**. Java map nodes, user strings, config overrides, and
bucket objects require heap sizing; profile the actual JVM rather than assuming an
exact bytes/user figure. Bucket mutation avoids a replacement object per admission.
There is no hard tenant-count bound or eviction in this increment. A global lock
would serialize all users; per-user lock objects add memory and lifecycle complexity.

The injected `Clock` follows the existing exercise convention. Rollback freezes
refill until time catches up; forward clock jumps refill early in real elapsed time.
Elapsed gaps exceeding a signed long in milliseconds throw explicitly. These are
clock-contract limits; use a monotonic elapsed-time source in a later increment.

Follow-ups, one at a time:

- Add a monotonic ticker while preserving deterministic tests.
- Add safe idle eviction for tenant churn, without scanning every bucket on a timer.
  Delete only when recreating a full bucket cannot grant extra quota, and serialize
  eviction with admission through the same map operation.

Enforcement is per limiter instance in one JVM. Shared atomic storage is a separate
extension for multiple JVMs. This is a demonstrated reference, not an independent solve.

## Focused checks

```powershell
mvn test '-Dtest=ratelimiter.tokenbucket.TokenBucketRateLimiterTest'
mvn test
java -cp target/classes ratelimiter.tokenbucket.Main
```

The focused tests cover bursts, isolation, the exact refill boundary, retained partial
periods, skipped periods and overflow-safe clamping, capacity 1, discarded overflow,
configuration resets, input validation, and competing same-tenant admissions.
