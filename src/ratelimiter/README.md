# Rate limiter design: client selects the algorithm

Both implementations live directly in `src/ratelimiter`, package `ratelimiter`, with no dependencies. Read `RateLimiterExample.java` first for a runnable client.

## Contract and scope

- One JVM and in-memory state; reuse one limiter instance per use case across all callers. Restart or a new instance means fresh quotas.
- A use case selects an algorithm and its settings once. The same user has independent quotas in separate use cases. Sharing an instance intentionally shares its quota namespace.
- `RateLimiter.isAllowed(userId)` consumes one slot/token on acceptance and returns a boolean. It does not queue requests or guarantee fairness, return order, or bounded lock wait.
- No inactive-user expiry, cleanup, persistence, distributed coordination, or live policy switching in this basic design.

## Main classes and request flow

```text
Client -> use-case lookup -> RateLimiter.isAllowed(userId)
                              |
                 +------------+----------------+
                 |                             |
         RateLimiterManager          TokenBucketRateLimiter
         (fixed-window choice)        (token-bucket choice)
                 |
         RateLimiterImpl per user
```

1. **RateLimiter** is the common client-facing strategy interface. Clients need only `isAllowed(userId)` after choosing/configuring an implementation.
2. **RateLimiterManager** configures fixed windows through its constructor or `registerUser`. It stores one **RateLimiterImpl** per user. Each user's reset/check/increment uses their own ReentrantLock; admission runs outside the map callback.
3. **TokenBucketRateLimiter** configures capacity and tokens per second. Each user has a separate token balance, starting full and refilling gradually. Its existing synchronized admission method still serializes users of that instance; this package move preserves that behavior.
4. **RateLimiterExample** defines `login -> fixed window` and `search -> token bucket` in a map. Its lookup selects a reusable object, then delegates through the interface. Unknown use cases throw; no silent algorithm fallback.

Each use case owns both its policy and its state. An enum/factory is unnecessary for these two constructor-based choices; add a typed configuration/factory only when selection must come from external configuration. A generic manager over per-user algorithm objects could later unify per-user custom configuration and locking for token buckets too; that refactor is deferred.

## Picking settings

| Choice | Settings | Behavior |
| --- | --- | --- |
| Fixed window | max accepted calls, window milliseconds; optional overrides per user | Full quota resets at an epoch-aligned boundary; boundary bursts are possible |
| Token bucket | capacity, refill tokens per second, common settings for users of one instance | Burst up to capacity, then gradual refill using monotonic elapsed time |

Both have expected O(1) state work per call, excluding contention and map resizing, and O(U) stored state per instance for U distinct users. Fixed-window wall-clock adjustments affect quotas. Token balances use approximate floating-point arithmetic. Neither choice is lock-free.

Interview explanation: “The caller selects a rate-limiting strategy per use case and depends on one interface. Each selected instance retains its users' quota state. The algorithm owns admission atomicity; the use-case lookup only routes the call. Separate use-case instances keep login and search quotas independent. This is currently a local design; distributing it would require a separate consistency and shared-state decision.”

Detailed existing notes: [fixed window](FixedWindowREADME.md), [token bucket](TokenBucketREADME.md). `TimeSlot` is an older, unused exercise class retained during the move.

## Validation

2026-10-15: root package compiled targeting Java 17 with `-Xlint:all -Werror` without warnings. All six fixed-window and four token-bucket checks passed after the move; the token-bucket burst check now calls through the shared interface. Small concurrency checks support validation, not proof; each algorithm's complete critical section establishes atomicity.

The runnable example printed true for login and search. All workspace sources also compiled targeting Java 21 with `-Xlint:all -Werror` without warnings; unrelated check runners and the performance harness were not rerun.

```powershell
javac --release 17 -Xlint:all -Werror -d out/root-rate-limiters src/ratelimiter/*.java
java -cp out/root-rate-limiters ratelimiter.RateLimiterTest
java -cp out/root-rate-limiters ratelimiter.TokenBucketRateLimiterTest
java -cp out/root-rate-limiters ratelimiter.RateLimiterExample
```
