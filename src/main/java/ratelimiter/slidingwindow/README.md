# Sliding-window rate limiter: code walkthrough

Read this beside [SlidingWindowRateLimiter.java](SlidingWindowRateLimiter.java).
This implementation uses an **exact sliding-window log**: for each user, it stores
the timestamps of accepted requests and removes them when they become old enough.

## 1. The rule being enforced

For a configuration of **2 requests per 1,000 milliseconds**, a request at time
`now` is allowed only if fewer than 2 previously accepted requests remain in:

```text
(now - 1000, now]
```

The left boundary is excluded; the right boundary is included. At `now = 1500`,
the window is `(500, 1500]`:

- A request accepted at `500` has expired.
- A request accepted at `501` still counts.
- Multiple requests at `1500` each count separately if accepted.

There is no scheduled reset or background cleanup thread. Each incoming request
cleans up its user's old timestamps before checking quota.

The quota applies independently per user and configuration revision. Updating a
user's configuration intentionally starts a fresh quota, even during an existing window.

## 2. Files and responsibilities

| File / type | Responsibility |
|---|---|
| [SlidingWindowRateLimiter](SlidingWindowRateLimiter.java) | Expire timestamps and decide whether to accept a request. |
| [RateLimiter](../common/RateLimiter.java) | Shared `allowRequest(String userId)` contract. |
| [RateLimitConfig](../common/RateLimitConfig.java) | Immutable positive `maxRequests` and `windowMillis` values. |
| [RateLimitConfigProvider](../common/RateLimitConfigProvider.java) | Supply thread-safe configuration snapshots for each user. |
| [InMemoryRateLimitConfigProvider](../common/InMemoryRateLimitConfigProvider.java) | Store per-user overrides and provide a default configuration. |
| [Main](Main.java) | Demonstrate fixed-window versus sliding-window behavior. |

## 3. What the fields store

```java
private final RateLimitConfigProvider configProvider;
private final Clock clock;
private final ConcurrentHashMap<String, RequestLog> logs = new ConcurrentHashMap<>();
```

`configProvider` answers: **What quota applies to this user?**

`clock` answers: **What time is it now?** Injecting it allows tests to move time
forward without sleeping. Convenience constructors use `Clock.systemUTC()` or
accept a caller-supplied clock.

`logs` answers: **Which accepted requests still consume this user's quota?**

For example, after cleanup at `now = 1500`:

```text
logs
  "alice" -> RequestLog(config: 2/1000ms, acceptedAt: [600, 1400])
  "bob"   -> RequestLog(config: 2/1000ms, acceptedAt: [1200])
```

Alice's next request is rejected; Bob has room for one more. Neither user's
requests consume the other user's quota.

## 4. Why `RequestLog` contains both config and timestamps

```java
private record RequestLog(RateLimitConfig config, ArrayDeque<Long> acceptedAt) {}
```

`acceptedAt` stores timestamps from oldest at the front to newest at the back:

```text
front                         back
oldest                        newest
  600  ->  900  ->  1400
removeFirst()                 addLast(now)
```

The code needs only three operations:

| Operation | Purpose |
|---|---|
| `peekFirst()` | Inspect the oldest accepted request. |
| `removeFirst()` | Remove an expired request. |
| `addLast(now)` | Record an accepted request. |

After cleanup, `acceptedAt.size()` is the quota already consumed in the current
rolling window. Before cleanup, an idle user's deque can still contain expired entries.

The `config` field identifies the configuration revision associated with the
timestamps. If the provider returns a new revision, the limiter discards the old
log and starts a new one.

**A record does not make its contents deeply immutable.** The deque is mutable.
It is safe here because it stays private and all access occurs inside the
`compute` callback for that user.

### Why this does not use `WindowState`

The fixed-window limiter's `WindowState(windowId, requestCount)` represents one
epoch-aligned interval. A sliding window needs individual request timestamps so
they can expire at different times. A single interval ID and counter cannot
represent this exact rolling history.

## 5. Follow `allowRequest()` in code order

### Validate the user ID

```java
Objects.requireNonNull(userId, "userId");
if (userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
```

Null IDs fail with `NullPointerException`; empty or whitespace-only IDs fail with
`IllegalArgumentException`. Invalid IDs never get an entry in the map.

### Create a place to store the admission result

```java
boolean[] allowed = {false};
```

The `compute` callback must return the updated **map value**, a `RequestLog`.
The method must return a separate **admission decision**, a boolean.

A Java lambda cannot reassign a captured local variable, so the code captures an
array reference and changes its element. Each method call creates its own array.
`compute` invokes the callback synchronously on the calling thread, so no
`AtomicBoolean`, future, or visibility mechanism is needed for this holder.

### Enter the atomic operation for this user

```java
logs.compute(userId, (key, old) -> {
    // ...
});
```

`key` is the user ID; `old` is that user's current `RequestLog`, or `null` on
their first request. The callback's return value becomes the user's map entry.

Cleanup, quota checking, and appending must happen together. For a quota of 2,
if the count is 1 and two threads independently check it, both could accept and
produce 3 accepted requests. Using `compute` serializes those decisions for the
same user: the second caller sees the first caller's update.

### Read configuration and time inside `compute`

```java
RateLimitConfig config = Objects.requireNonNull(configProvider.getConfig(key), "config");
long now = clock.millis();
```

Reading inside the callback means a caller waiting to enter the atomic operation
does not carry a previously captured time or configuration into it. The provider
must return a non-null snapshot and support concurrent access.

### Select the current log or start a fresh one

```java
RequestLog log = old == null || old.config() != config
        ? new RequestLog(config, new ArrayDeque<>()) : old;
```

| Condition | Action |
|---|---|
| `old == null` | Create the user's first log. |
| `old.config() != config` | Configuration revision changed: discard old quota usage. |
| Otherwise | Reuse the existing log and expire its old timestamps. |

`!=` compares **object identity**, not record values. The provider contract says
to return the same instance until reconfigured, then publish a fresh instance.
`InMemoryRateLimitConfigProvider.configure()` copies settings into a new record
on every call, including updates with equal values or a reused input object.

Consequently, even setting `2/1000ms` to `2/1000ms` resets quota. Replacing `!=`
with `.equals()` would change that behavior. A custom provider that creates a new
record on every read would incorrectly reset quota on every request.

### Remove expired timestamps

```java
if (now >= Long.MIN_VALUE + config.windowMillis()) {
    long cutoff = now - config.windowMillis();
    while (!log.acceptedAt().isEmpty() && log.acceptedAt().peekFirst() <= cutoff) {
        log.acceptedAt().removeFirst();
    }
}
```

The meaningful boundary check is `timestamp <= cutoff`. A request expires at
exactly `windowMillis` age. Using `< cutoff` would keep it for one millisecond
too long.

Because timestamps are ordered, expired requests form a prefix of the deque.
Once its first timestamp is newer than the cutoff, every remaining timestamp is
also newer. There is no need to scan or sort the rest.

The outer `if` guards subtraction underflow. For example, subtracting 1,000 from
`Long.MIN_VALUE + 100` cannot fit in a `long`. In that situation the mathematical
cutoff is below every representable timestamp, so nothing can have expired and
cleanup is skipped. This also supports valid negative epoch timestamps.

### Admit only when quota remains

```java
if (log.acceptedAt().size() < config.maxRequests()) {
    log.acceptedAt().addLast(now);
    allowed[0] = true;
}
return log;
```

If there is room, append the current timestamp and accept. Otherwise leave the
decision `false` and do not append anything. Rejected requests consume no quota
and do not postpone the expiry of accepted requests.

After the callback finishes, `return allowed[0]` returns the decision to the caller.

## 6. Trace the deque through several calls

Configuration: **2 requests per 1,000ms**. All requests below belong to Alice.

| Now | Cutoff | Expired timestamps removed | Deque after the call | Result |
|---:|---:|---|---|---|
| 100 | -900 | None | `[100]` | Accept |
| 600 | -400 | None | `[100, 600]` | Accept |
| 1099 | 99 | None | `[100, 600]` | Reject |
| 1100 | 100 | `100` | `[600, 1100]` | Accept |
| 1100 | 100 | None | `[600, 1100]` | Reject |
| 1600 | 600 | `600` | `[1100, 1600]` | Accept |

At `1100`, only the request at `100` expires. The request at `600` still counts.
The implementation releases quota individually rather than resetting the whole counter.

## 7. What `Main` demonstrates

Open [Main.java](Main.java), or run from the repository root after compilation:

```powershell
mvn compile
java -cp target/classes ratelimiter.slidingwindow.Main
```

Both limiters use a quota of 2 per 1,000ms. They accept two requests at `999`.

At `1000`, fixed-window starts its next interval `[1000, 2000)` and accepts
another request. Sliding-window still counts both requests at `999`, so it rejects.
Those requests expire at `1999`, when each reaches exactly 1,000ms of age.

Expected output:

```text
t=999, request 1: fixed=true, sliding=true
t=999, request 2: fixed=true, sliding=true
t=1000: fixed=true, sliding=false
t=1999, old requests expire: sliding=true
After config update: true
Quota exhausted: false
```

The last two lines show that reconfiguring Alice to 1 request per 1,000ms resets
her log; one request succeeds and the next fails.

## 8. Concurrency guarantees and limits

`ConcurrentHashMap` protects the map, but does not automatically make objects
stored inside it thread-safe. The deque is protected by the stronger discipline
that **every read and mutation happens inside `compute` for its user**.

- Same-user admissions are serialized, including cleanup and configuration selection.
- Different map bins can progress concurrently; hash collisions can cause contention.
- The callback runs on the caller's thread. It creates no worker threads and should
  remain short; a slow configuration provider blocks progress in that operation.
- Updating configuration uses a separate map in the provider. It is not one atomic
  transaction with admission. An overlapping call may finish using its old snapshot;
  the next call observing the new snapshot starts fresh.
- Separate limiter instances have separate logs, even if they share a provider.

## 9. Clock assumption

The deque must remain ordered by timestamp, so this implementation assumes the
clock does not move backward. A wall-clock correction can violate that ordering
and make front-only expiry inaccurate.

Injected clocks make boundary tests deterministic; they do not themselves make
time monotonic. A production variation can use an injected monotonic elapsed-time
source, such as one based on `System.nanoTime()`, with consistent duration units.

## 10. Complexity, scale, and alternatives

Let `U` be the number of retained users and `L` a user's configured request limit.
Assume constant-time configuration lookup and ordinary hash distribution.

| Operation / resource | Cost |
|---|---|
| One request | Expected amortized O(1): each accepted timestamp is appended and removed at most once. |
| One request after a long idle period | O(L) worst case to remove a full log. |
| Stored timestamps | At most the quota per user for the active revision. |
| Memory | O(U + sum of retained per-user log capacities). |

`ArrayDeque` retains its backing array after removals, and the map retains inactive
users. Lazy expiry does not reclaim those map entries. Configuration changes
replace the deque; otherwise its capacity can remain at its historical peak.

For millions of users or large quotas, add coordinated idle eviction. For a quota
shared across service instances, use a shared atomic store, for example a Redis
sorted set with an atomic expiry/check/append script and TTL. Give tied timestamps
unique members so distinct accepted requests are not collapsed.

| Alternative | Tradeoff |
|---|---|
| Fixed window | O(1) state per user, but adjacent intervals permit a boundary burst. |
| Sliding-window counter | O(1) state per user using adjacent counts, but the rolling count is approximate. |
| Token bucket | O(1) state per user with refill and burst capacity; different admission semantics. |

## 11. Tests to read after the implementation

[SlidingWindowRateLimiterTest.java](../../../../test/java/ratelimiter/slidingwindow/SlidingWindowRateLimiterTest.java)
exercises rolling boundaries, exact individual expiry, rejection behavior, user
isolation, configuration resets, long idle periods, validation, extreme timestamps,
and simultaneous requests for two users.

From the repository root:

```powershell
mvn test '-Dtest=SlidingWindowRateLimiterTest'
```

The central invariant to explain in an interview: **after cleanup and before
admission, the deque contains only accepted requests in the current rolling window
for this configuration revision; append only when its size is below the limit.**
