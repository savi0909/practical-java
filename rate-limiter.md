# Rate-limiter exercise: architectural context

This is a Java 21+ LLD interview exercise using plain Java and Maven. Read this
before modifying `src/main/java/ratelimiter` or its tests. It records the current
implementation and decisions established during this exercise.

This file is the context entry point referenced by [AGENTS.md](AGENTS.md).
The previously requested `RATE_LIMITER_CONTEXT.md` was not found; this document
does not claim to reproduce its contents. If that file becomes available, review
it and reconcile any differences explicitly.

## Working approach

- Review the relevant implementation first and identify gaps before making changes.
- Implement algorithms incrementally, starting with the simplest useful version.
- Preserve existing abstractions and design decisions unless the user requests a change.
- Explain important Java concurrency decisions before significant architectural changes.
- Keep chat explanations concise. Put detailed explanations in the algorithm README.
- Put interview critique in the implementation header: complexity, concurrency,
  edge cases, scale limits, and alternatives with their tradeoffs.
- Keep tutorial tests focused on core behavior and boundaries. Add broader tests
  only when needed to verify the implementation; avoid unnecessary stress suites.
- Compile, run relevant tests, and exercise the demo before claiming code is complete.
- Keep [docs/learning-plan.md](docs/learning-plan.md) current when exercises are added
  or completed. Demonstrated reference examples do not count as independent solves.

## Shared abstractions

Shared types live in `ratelimiter.common`:

| Type | Contract to preserve |
|---|---|
| `RateLimiter` | `boolean allowRequest(String userId)`; each invocation represents a distinct request. |
| `RateLimitConfig` | Immutable positive `maxRequests` and `windowMillis`; counters belong to algorithms. |
| `RateLimitConfigProvider` | Thread-safe non-null snapshots; object identity identifies a configuration revision. |
| `InMemoryRateLimitConfigProvider` | Per-user overrides with a fixed default; every update publishes a fresh snapshot. |

Keep admission logic separate from configuration storage. Inject the provider and
`Clock` through constructors. Existing convenience constructors create the default
provider and/or use `Clock.systemUTC()`.

Algorithm-specific state stays with its algorithm. For example, `WindowState`
belongs to fixed-window counting; it is not a shared abstraction for timestamp logs.

## Admission and configuration semantics

- Reject null user IDs and null required collaborators with `NullPointerException`.
- Reject blank IDs and invalid positive-limit/duration settings with `IllegalArgumentException`.
- Accepted calls consume quota; rejected calls do not. Equal timestamps and repeated
  calls are distinct requests, with no deduplication or fairness guarantee.
- Quotas are independent per user and per limiter instance.
- A configuration update lazily resets only its user's state on the next request
  observing the new revision. Equal-value updates and reused input objects also reset.
- Preserve identity comparison for revisions; value equality would miss equal-value updates.
- A custom provider must return the same snapshot instance until reconfiguration.
  Returning a new object on every read would reset quota on every request.
- Configuration publication and admission are not a single transaction. An overlapping
  admission can finish under a snapshot it already read; a later one observing the
  new snapshot resets. Do not silently promise immediate cancellation of in-flight calls.

## Java concurrency decisions

The implementations use `ConcurrentHashMap.compute(userId, ...)` for the complete
reset/expiry, quota check, and admission update. Preserve this atomic operation;
separate thread-safe map calls do not make a multi-step admission decision atomic.

Read configuration and time inside `compute` so waiting callers do not carry stale
values into their update. Same-user calls serialize; colliding map bins can also
contend. Keep the callback short and avoid blocking provider operations.

Fixed-window and basic two-counter entries use immutable records. Sliding-log deques
and bucketed arrays are mutable and safe because every access stays inside their
owning user's `compute` callback. A record does not make a contained collection immutable.

The per-call `boolean[]` carries the admission result out of the callback, whose
return value updates the map. It does not require an atomic wrapper: `compute`
executes synchronously and each call owns its holder.

Before replacing this approach with locks, atomics, background cleanup, or shared
storage, explain the new atomicity boundary, visibility guarantees, and tradeoffs.

## Implemented algorithms and study order

| Order | Implementation | Behavior and tradeoff |
|---:|---|---|
| 1 | `ratelimiter.fixedwindow.FixedWindowRateLimiter` | Epoch-aligned counter; expected O(1) admission and O(1) state/user; permits bursts across adjacent boundaries. |
| 2 | `ratelimiter.slidingwindow.SlidingWindowRateLimiter` | Exact rolling log for `(now - duration, now]`; expected amortized O(1) admission, O(limit) retained timestamps/user. |
| 3 | `ratelimiter.slidingwindowcounter.SlidingWindowCounterRateLimiter` | Weighted previous/current counters; expected O(1) admission and O(1) state/user; can underestimate or overestimate burst traffic. |
| 4 | `ratelimiter.slidingwindowcounter.BucketedSlidingWindowCounterRateLimiter` | Small counters in a circular array; O(B) expiry/summation per request and O(B) state/user. |
| 5 | `ratelimiter.tokenbucket.TokenBucketRateLimiter` | Increment 1: full initial capacity, lazy batch refill of 2 tokens every 5 seconds, expected O(1) admission and O(1) state/user. |

The bucketed implementation defaults to 5-second buckets. A 60-second limit retains
12 previous buckets plus the active bucket: **13 slots**, not 12 total. Window
duration must be a multiple of bucket duration; a custom bucket duration is supported.

Count the whole oldest overlapping bucket conservatively. Under nondecreasing time
and unchanged configuration, this prevents false admissions relative to the exact
rolling quota, but can reject for up to one extra bucket duration. A 5-second bucket
does not imply a 5% count-error bound; error depends on request distribution.

The circular array bounds storage; the current implementation still scans all slots.
A running total and incremental expiry are optional follow-ups, not existing behavior.
Token bucket increment 1 is implemented: each tenant starts full, gains 2 whole
tokens per completed 5-second period since its creation, and loses excess refill at
capacity. Preserve unfinished periods. Its shared config interprets `maxRequests`
as capacity and `windowMillis` as refill period; capacity constructors use 5,000 ms.
Configuration revisions reset to full. Mutable buckets stay confined to `compute`.
Next token-bucket increments: monotonic ticker, then safe idle eviction.

## Known limitations to discuss before extending

- All implementations assume nondecreasing time. Injected `Clock` enables deterministic
  tests but does not make wall time monotonic. Rollback can violate expiry or quota behavior.
- User entries are retained indefinitely. Lazy timestamp/bucket expiry is not idle-user eviction.
- Enforcement is local to one limiter instance. Multiple instances need shared atomic
  state if they are expected to enforce one combined quota.
- Bucket arrays scale with duration divided by bucket size. Very large configurations
  need practical allocation limits; an int-sized array check alone is not a memory budget.
- Fresh quota on reconfiguration is intentional and can increase admissions. Do not
  turn configuration updates into a per-request action.

Treat these as explicit interview tradeoffs. Discuss their relevance before adding
production infrastructure or significantly changing the concurrency model.

## Tutorial guides and verification

- [Fixed-window walkthrough](src/main/java/ratelimiter/fixedwindow/README.md).
- [Exact sliding-window walkthrough](src/main/java/ratelimiter/slidingwindow/README.md).
- [Basic and bucketed counter walkthrough](src/main/java/ratelimiter/slidingwindowcounter/README.md).
- [Token-bucket increment 1 walkthrough](src/main/java/ratelimiter/tokenbucket/README.md).

From the repository root, run `mvn test` for the existing suite. For demos after
compilation, use the fully qualified main class:

```powershell
mvn compile
java -cp target/classes ratelimiter.fixedwindow.Main
java -cp target/classes ratelimiter.slidingwindow.Main
java -cp target/classes ratelimiter.slidingwindowcounter.Main
java -cp target/classes ratelimiter.tokenbucket.Main
```

Do not use bare `mvn exec:java` for these demos: the project's default main class
points at a separate exercise. Keep the tutorial READMEs and this context aligned
with actual implementation changes.
