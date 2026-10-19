# Fixed-window rate limiter: code walkthrough

Read this beside [FixedWindowRateLimiter.java](FixedWindowRateLimiter.java).
The limiter keeps one counter per user for the current **epoch-aligned fixed
interval**. A request either starts a new counter, increments the current counter,
or is rejected because the counter has reached its limit.

## 1. The rule being enforced

For **3 requests per 10,000 milliseconds**, the intervals are:

```text
[0, 10000)    [10000, 20000)    [20000, 30000)    ...
window 0      window 1          window 2
```

The left boundary is included; the right boundary belongs to the next window.
A request at `9999` uses window 0. A request at `10000` uses window 1.

The window starts at a multiple of the configured duration measured from the
Unix epoch. It does **not** start when a user makes their first request. Users
with the same window duration therefore have the same interval boundaries.

Quota is independent per user. A configuration update intentionally starts fresh
quota for that user, even if the current interval has not ended.

## 2. Files and responsibilities

| File / type | Responsibility |
|---|---|
| [FixedWindowRateLimiter](FixedWindowRateLimiter.java) | Atomically reset, increment, or reject. |
| [WindowState](WindowState.java) | Store the interval ID and accepted request count. |
| [Main](Main.java) | Demonstrate per-user settings, default settings, and configuration updates. |
| [RateLimiter](../common/RateLimiter.java) | Shared `allowRequest(String userId)` interface. |
| [RateLimitConfig](../common/RateLimitConfig.java) | Validate and store positive request limit and duration. |
| [RateLimitConfigProvider](../common/RateLimitConfigProvider.java) | Supply thread-safe configuration snapshots. |
| [InMemoryRateLimitConfigProvider](../common/InMemoryRateLimitConfigProvider.java) | Supply per-user overrides or the default configuration. |

## 3. What the fields store

```java
private final RateLimitConfigProvider configProvider;
private final Clock clock;
private final ConcurrentHashMap<String, ConfiguredWindow> windows = new ConcurrentHashMap<>();
```

`configProvider` supplies a user's quota settings. `clock` supplies the current
time. Injecting both keeps the admission logic independent of configuration
storage and makes boundary tests possible without sleeping.

`windows` stores the latest state for each user:

```text
"alice" -> config: 3/10000ms, state: windowId=4, requestCount=2
"bob"   -> config: 3/10000ms, state: windowId=4, requestCount=3
```

If the current time still belongs to window 4 and the configuration is unchanged,
Alice has one request remaining; Bob's next request is rejected.

The map does not store every historical interval. A new interval replaces that
user's previous state when their next request arrives.

## 4. Why there are two records

```java
public record WindowState(long windowId, int requestCount) {}

private record ConfiguredWindow(RateLimitConfig config, WindowState state) {}
```

`WindowState` contains the counter information specific to this algorithm:

- `windowId`: which fixed interval the counter belongs to.
- `requestCount`: how many requests were accepted in that interval for this revision.

`ConfiguredWindow` couples that state with the configuration revision that created
it. A counter alone cannot tell whether the user's configuration has changed.

For example, Alice exhausts 3 requests, then is reconfigured to 1 request in the
same interval. The stored configuration allows the limiter to recognize the
update and start fresh rather than reject based on the old count.

Both records hold immutable values here. An accepted request creates replacement
records; the implementation never mutates an existing counter.

You could flatten the fields into one record containing `config`, `windowId`,
and `requestCount`. That reduces nesting and one allocation per accepted request;
the two-record arrangement keeps the algorithm's counter separate from its revision.

## 5. Follow `allowRequest()` in code order

### Validate the user ID

```java
Objects.requireNonNull(userId, "userId");
if (userId.isBlank()) {
    throw new IllegalArgumentException("userId must not be blank");
}
```

Null IDs fail with `NullPointerException`. Empty or whitespace-only IDs fail with
`IllegalArgumentException`. These checks happen before touching the map.

### Create the result holder

```java
boolean[] allowed = {false};
```

The callback must return a `ConfiguredWindow` to update the map, while the method
must return a boolean to the caller. This array carries the separate decision.

Java lambdas cannot reassign a captured local variable, but can change an element
of a captured array. Each invocation creates its own array, and `compute` runs
the callback synchronously on that invocation's thread. No `AtomicBoolean` is needed.

### Enter the atomic operation

```java
windows.compute(userId, (key, old) -> {
    // ...
});
```

`key` is the user ID; `old` is their previous state, or `null` on the first call.
The returned value becomes the user's entry.

The whole decision must be atomic. Suppose the limit is 3 and the current count
is 2. If two threads separately read 2, both can accept and overwrite the count
with 3, although 4 requests were actually accepted. `compute` serializes the
check and replacement for the same user, preventing that race.

### Read configuration and calculate the interval ID

```java
RateLimitConfig config = Objects.requireNonNull(configProvider.getConfig(key), "config");
long windowId = Math.floorDiv(clock.millis(), config.windowMillis());
```

For a 10,000ms duration:

| Current time in milliseconds | Window ID |
|---:|---:|
| -1 | -1 |
| 0 | 0 |
| 9999 | 0 |
| 10000 | 1 |
| 19999 | 1 |
| 45000 | 4 |

`Math.floorDiv` handles negative epoch times correctly. Ordinary Java integer
division rounds toward zero: `-1 / 10000` gives 0, while floor division gives -1,
placing that timestamp in `[-10000, 0)`.

Configuration and time are read inside `compute`, so a caller waiting to enter
does not use values captured before another admission completed.

### Start fresh when there is no reusable counter

```java
if (old == null || old.config() != config || old.state().windowId() != windowId) {
    allowed[0] = true;
    return new ConfiguredWindow(config, new WindowState(windowId, 1));
}
```

There are three reasons to reset:

| Condition | Meaning |
|---|---|
| `old == null` | This is the user's first request. |
| `old.config() != config` | Their configuration revision changed. |
| Stored window ID differs | Their last request belonged to another interval. |

The incoming request is accepted immediately and recorded as count **1**, because
validated configuration always permits at least one request. Starting at 0 would
forget to count this accepted request.

Reset is lazy: no timer changes the entry at the boundary. The first request in
the new interval replaces it. Jumping across several unused intervals works the
same way; the code does not loop through the missed windows.

### Increment if the current counter still has room

```java
if (old.state().requestCount() < config.maxRequests()) {
    allowed[0] = true;
    return new ConfiguredWindow(config,
            new WindowState(windowId, old.state().requestCount() + 1));
}
```

This branch applies only when both the interval and configuration revision match.
The count increases once per accepted call, including calls at identical timestamps.

No `AtomicInteger` is needed because checking and replacing the immutable counter
already happen inside the same atomic map operation. An atomic increment alone
would not coordinate interval resets and configuration changes.

### Reject without modifying the entry

```java
return old;
```

If the counter has reached the limit, keep it unchanged. The result holder remains
`false`. Repeated rejections do not consume additional quota or grow the counter.
The guarded increment cannot overflow: it happens only below a positive `int` limit.

Once the callback returns, the method returns `allowed[0]` to the caller.

## 6. Trace several requests

Configuration: **3 requests per 10,000ms**. All calls below belong to Alice.

| Time | Window ID | State before | State after | Result |
|---:|---:|---|---|---|
| 9000 | 0 | Absent | `(0, 1)` | Accept |
| 9500 | 0 | `(0, 1)` | `(0, 2)` | Accept |
| 9999 | 0 | `(0, 2)` | `(0, 3)` | Accept |
| 9999 | 0 | `(0, 3)` | `(0, 3)` | Reject |
| 10000 | 1 | `(0, 3)` | `(1, 1)` | Accept |
| 10000 | 1 | `(1, 1)` | `(1, 2)` | Accept |
| 45000 | 4 | `(1, 2)` | `(4, 1)` | Accept |

The last row illustrates lazy reset after skipped intervals. Only the new interval
ID matters; there is no need to preserve the old history.

## 7. Configuration identity and reset behavior

```java
old.config() != config
```

This compares object identity, not record field equality. The provider contract
requires a stable snapshot instance until reconfiguration, followed by a fresh
instance on each update.

`InMemoryRateLimitConfigProvider.configure()` copies settings into a new record,
so even passing equal values or reusing an old input object creates a new revision.
The next admission observing it resets that user's counter. A sequence A -> B -> A
is recognized even if no request was made while configuration B was active.

Using `.equals()` would miss equal-value updates. Conversely, a custom provider
that returns a fresh record on every read would accidentally reset every request.

Updates and admission operate on separate maps. An update overlapping a callback
can occur after it reads the old configuration; that request finishes under the
old revision. A subsequent request observing the new revision resets. Frequent
updates therefore intentionally grant additional quota; do not configure per request.

## 8. What `Main` demonstrates

Run from the repository root:

```powershell
mvn compile
java -cp target/classes ratelimiter.fixedwindow.Main
```

The demo freezes time at `1000ms` so output is repeatable. It configures:

| User | Request limit | Duration |
|---|---:|---:|
| user-A | 3 | 10,000ms |
| user-B | 10 | 60,000ms |
| user-C, using default settings | 5 | 60,000ms |

Expected output:

```text
user-A request 1: true
user-A request 2: true
user-A request 3: true
user-A request 4: false
user-A request 5: false
user-B: true
Default user: true
user-A after config change: true
user-A quota exhausted: false
```

The last two calls follow an update to 1 request per 60,000ms. Time has not moved,
and the window ID is still 0. The new configuration revision causes the reset.
This demonstrates why the map stores `ConfiguredWindow`, rather than only `WindowState`.

## 9. Concurrency and clock limits

- Same-user reset, check, increment, and decision are atomic inside `compute`.
- Stored state is immutable and safely published through the concurrent map.
- Users in different map bins can progress concurrently; collisions may cause contention.
- The callback runs on the caller's thread, so keep the provider fast.
- Separate limiter instances have independent counters, even with a shared provider.
- Clock rollback can revisit an older interval. The differing ID triggers another
  reset and may grant additional quota. Strict enforcement assumes nondecreasing time.

## 10. Complexity, boundary bursts, and scale

With ordinary hash distribution and constant-time configuration lookup, a request
takes expected **O(1)** time. The map retains **O(U)** entries for `U` distinct users.
Accepted calls allocate new state records; rejections reuse the old state.

The principal algorithmic tradeoff is a boundary burst: with a limit of 3 per
10 seconds, a user can send 3 requests at `9999ms` and 3 more at `10000ms`.
Both fixed intervals satisfy the quota, but 6 requests were accepted across 1ms.
The [sliding-window log](../slidingwindow/README.md) tracks individual timestamps
to enforce a rolling quota, at a higher memory cost.

Inactive users are never evicted here. At scale, add coordinated expiry without
discarding a live counter early. Across application instances, use a shared
atomic counter/reset operation, for example a Redis script with appropriate TTL.

Alternatives include a sliding-window counter for approximate rolling limits
with O(1) state per user, and token bucket for explicit burst capacity and refill.

## 11. Tests to read after the implementation

[FixedWindowRateLimiterTest.java](../../../../test/java/ratelimiter/fixedwindow/FixedWindowRateLimiterTest.java)
covers quotas, boundaries, skipped intervals, independent users, overrides, configuration
revisions, validation, simultaneous admissions, and an update overlapping an in-flight call.

From the repository root:

```powershell
mvn test '-Dtest=FixedWindowRateLimiterTest'
```

The central invariant to explain in an interview: **the stored count equals accepted
requests for the stored user, interval ID, and configuration revision; reset to 1
when either identity changes, otherwise increment only while below the limit.**
