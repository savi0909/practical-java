# Sliding-window counters: basic, then bucketed

Read the implementations in this order:

1. [SlidingWindowCounterRateLimiter.java](SlidingWindowCounterRateLimiter.java):
   two counters and a weighted estimate.
2. [BucketedSlidingWindowCounterRateLimiter.java](BucketedSlidingWindowCounterRateLimiter.java):
   5-second counters in a circular array.
3. [Main.java](Main.java): deterministic demonstrations of both.

Both implement our existing [RateLimiter](../common/RateLimiter.java) interface
and use the shared configuration provider. Neither stores individual request timestamps.

## 1. Why replace the timestamp log?

The [exact sliding-window log](../slidingwindow/SlidingWindowRateLimiter.java)
stores a timestamp for each accepted request. Its rolling count is exact, but
memory grows with the user's quota.

Counters combine many requests into fewer numbers. This saves memory but loses
information about when requests occurred inside a counted interval. The two
versions below handle that missing information differently.

| Implementation | Stored per user | Accuracy policy |
|---|---|---|
| Exact log | One timestamp per retained accepted request | Exact expiry by age. |
| Two-counter version | Previous count and current count, plus revision/window metadata | Estimate previous traffic as uniformly distributed. |
| 5-second bucket version | 13 bucket IDs/counts for a 60-second duration | Include the entire oldest overlapping bucket. |

## 2. Basic version: two counters

For a 60-second duration, divide time into fixed intervals:

```text
[0s, 60s)              [60s, 120s)
previous window        current window, when now = 90s
```

At `now = 90s`, the actual rolling window is `(30s, 90s]`.
Only the second half of the previous fixed window overlaps it.

The stored state is:

```java
private record CounterWindow(RateLimitConfig config, long windowId,
                             int previousCount, int currentCount) {}
```

- `previousCount`: accepted requests in the preceding fixed interval.
- `currentCount`: accepted requests in the current fixed interval.
- `windowId`: identifies which interval is current.
- `config`: identifies the configuration revision associated with these counters.

### The weighted estimate

```text
elapsed = now modulo windowDuration
remaining = windowDuration - elapsed

estimatedCount = currentCount
               + previousCount * remaining / windowDuration
```

Example: limit 4 per 60 seconds, `previousCount = 4`, `currentCount = 0`:

| Now | Elapsed in current interval | Previous contribution | Extra requests allowed at that instant |
|---|---|---|---:|
| 60s | 0s | 4 × 60/60 = 4 | 0 |
| 75s | 15s | 4 × 45/60 = 3 | 1 |
| 90s | 30s | 4 × 30/60 = 2 | 2 |
| 105s | 45s | 4 × 15/60 = 1 | 3 |

Each row independently assumes no requests were accepted in the current interval
before that row. Accepted requests increase `currentCount` immediately.

### Follow `allowRequest()`

1. Reject null or blank user IDs.
2. Enter `windows.compute(userId, ...)` so rollover, estimation, and admission
   happen atomically for this user.
3. Read the configuration and clock inside the callback.
4. Calculate `windowId = Math.floorDiv(now, windowMillis)`.
5. Choose which counts to carry forward using the rules below.
6. Calculate elapsed and remaining time with `floorMod`.
7. Accept only if `estimatedCount + 1 <= maxRequests`.
8. Store the resulting state, including rollover even if admission was rejected.

### Rollover rules

| Situation | New previous count | New current count before admission |
|---|---:|---:|
| First request or new configuration revision | 0 | 0 |
| Same fixed interval | Keep previous | Keep current |
| Exactly the next interval | Old current | 0 |
| Two or more intervals later | 0 | 0 |

Once an entire empty interval separates the last request from the current one,
neither old counter can contribute. No loop over skipped intervals is needed.

### Why include the incoming request?

Suppose the limit is 2 and the estimate is `1.002`. Merely checking
`estimate < 2` would accept, even though the new estimate becomes `2.002`.
This implementation checks the estimate **after including one more request**.

The helper `fitsIncludingRequest()` rearranges the comparison into:

```text
previousCount × remaining
    <= (maxRequests - currentCount - 1) × windowMillis
```

It compares integer products to avoid floating-point rounding at the threshold.
Ordinary settings use `long`; unusually large products fall back to `BigInteger`.
This arithmetic makes the weighted decision precise, but the underlying traffic
estimate is still approximate.

### Where the estimate goes wrong

Consider 4 requests accepted together at `59.999s`. At `90s`, all 4 really are
inside the last 60 seconds. The two-counter estimate retains only 2 of them and
allows 2 more requests. This is a **false admission** relative to an exact limit.

If the 4 requests instead arrived at `0s`, they have all expired at `90s`.
The same estimate still retains 2 and allows only 2 new requests. That produces
**false rejections** relative to the exact limit.

The counters cannot distinguish these distributions. Their total was 4 in both cases.

## 3. Enhanced version: 5-second buckets

Instead of aggregating an entire 60-second interval, aggregate smaller intervals:

```text
Bucket ID:  0       1       2                    11        12
Interval: [0,5)   [5,10) [10,15) ...          [55,60)   [60,65) seconds
```

At `now = 62s`, the exact rolling window is `(2s, 62s]`.
Buckets 1 through 11 belong entirely to it. Bucket 0 overlaps its oldest edge,
and bucket 12 is active.

We need **12 previous buckets plus the active bucket = 13 slots**.
Storing only 12 total slots would overwrite bucket 0 when bucket 12 starts,
losing requests at times such as `4.999s` that are still within the rolling window.

The bucket size defaults to 5,000ms. Configured window duration must be a positive
multiple of that size. A custom bucket duration is available for smaller exercises:

```java
var provider = new InMemoryRateLimitConfigProvider(new RateLimitConfig(4, 1_000));
RateLimiter limiter = new BucketedSlidingWindowCounterRateLimiter(provider, clock, 100);
// 100ms buckets: 10 previous buckets + active = 11 slots.
```

Use imports from `ratelimiter.common` for the provider, settings, and interface;
`clock` is any caller-supplied `java.time.Clock`.

### Circular-array state

```java
private record BucketWindow(RateLimitConfig config, long[] bucketIds, int[] counts) {}
```

The two arrays are parallel: `counts[i]` belongs to `bucketIds[i]`.
A count of zero means the slot has nothing to contribute, regardless of its old ID.

```text
activeBucketId = floorDiv(now, bucketMillis)
slotIndex = floorMod(activeBucketId, numberOfSlots)
```

For 13 slots:

| Absolute bucket ID | Array index |
|---:|---:|
| 0 | 0 |
| 12 | 12 |
| 13 | 0 |
| 14 | 1 |
| 26 | 0 |

The same array position is reused, but each entry retains its absolute bucket ID.
That ID lets expiry distinguish current contents from a previous rotation.
`floorMod` also produces a valid array index for negative epoch times.

### Follow `allowRequest()`

1. Validate the user and enter `compute` for that user.
2. Read the configuration; validate that its duration is compatible with bucket size.
3. Calculate the active bucket ID from the current time.
4. On first use or configuration change, allocate fresh fixed-size arrays.
5. Scan the slots. For each nonempty one, calculate `age = activeId - storedId`.
6. Clear counters older than the retained range; sum the remaining counts.
7. If the sum is below the limit, increment the active bucket's slot and accept.
8. Otherwise reject without adding to any counter.

With 60s/5s, ages `0..12` are retained; ages greater than 12 are cleared.
After expiry, a reused active slot is either empty or already tagged with the
active bucket ID. There is no live older bucket at the same index.

The unsigned comparison in the implementation also handles a forward time jump
large enough to overflow a signed age subtraction. The normal case is simply
"expire when age is greater than 12."

### The oldest bucket is counted conservatively

At `62s`, bucket 0 spans `[0s, 5s)`, but only timestamps after `2s` still belong
to the exact rolling window. We do not know how bucket 0's requests were distributed.

This implementation keeps **all** of that bucket's count. It does not apply a
fractional weight. Therefore, under a nondecreasing clock and unchanged configuration,
the sum is at least as large as the true rolling count of accepted requests:
there are no false admissions, but false rejections are possible.

An expired request may continue contributing for up to 5 additional seconds,
until its entire bucket expires. Reducing bucket size narrows that time uncertainty
while increasing the number of counters.

**A 5-second bucket does not imply a 5% count error.** Even the 5/60 duration ratio
is about 8.33%; neither ratio bounds count error without assumptions about request
distribution. A whole quota can be concentrated inside the uncertain bucket.

### Trace expiry and slot reuse

Limit: 4 requests per 60 seconds; buckets: 5 seconds; slots: 13.

| Time | Action | Retained counts after action | Result |
|---|---|---|---|
| 4.999s | Make 3 requests | bucket 0 = 3 | All accepted |
| 60s | Make 1 request | bucket 0 = 3, bucket 12 = 1 | Accepted |
| 60s | Make another request | Same counts, total 4 | Rejected |
| 65s | Make 3 requests | bucket 12 = 1, bucket 13 = 3 | All accepted |
| 65s | Make another request | Same counts, total 4 | Rejected |

At `65s`, bucket 0 is age 13 and expires. Its array index 0 is then reused for
bucket 13. Bucket 12 still contributes; rotating the array does not reset everything.

## 4. Shared behavior and thread safety

Both implementations use `ConcurrentHashMap.compute` for the complete per-user
operation. Separate quota check and increment calls would race under concurrent traffic.
The callback runs synchronously; a per-call `boolean[]` carries its decision back
because the callback itself must return the map value.

The basic version stores immutable state records. The bucketed version mutates
arrays, but every access stays inside its user's `compute` callback. The record
does not make those arrays immutable; confinement to the atomic operation is essential.

Configuration object identity acts as a revision, just as in our fixed-window and
exact-log versions. Every `configure()` call publishes a new snapshot and lazily
resets only that user's state. An overlapping admission may finish using a snapshot
it already read. New revision resets intentionally permit fresh quota.

Both versions assume nondecreasing time, retain idle users, and enforce traffic
only within their own limiter instance. Production extensions include coordinated
idle eviction and shared atomic storage for enforcement across multiple instances.

## 5. Complexity and the next optimization

Assume ordinary hash distribution and O(1) configuration lookup.

| Version | Request cost | Space per user |
|---|---|---|
| Basic two counters | Expected O(1) | O(1) |
| Bucketed circular array | O(B), with B=13 for 60s/5s | O(B) |

The circular array bounds storage and avoids allocating individual timestamps.
It does not automatically make summing and expiry O(1): this version scans every
slot on every request. Thirteen slots keeps that loop small and easy to follow.

A further optimization is a running total: subtract counters when slots expire,
add on admission, and advance a cursor only when time crosses a bucket boundary.
That avoids repeated summation but adds state and rollover bookkeeping. It is
deliberately left out of this tutorial version.

## 6. Run the tutorial and basic checks

From the repository root:

```powershell
mvn compile
java -cp target/classes ratelimiter.slidingwindowcounter.Main
```

The demo first shows two-counter rollover and half-window weighting, then bucket
expiry and array index reuse. No sleeping is necessary because it uses a controllable clock.

Five focused tests per implementation cover quota, boundaries, rollover/expiry,
and configuration changes:

- [Two-counter tests](../../../../test/java/ratelimiter/slidingwindowcounter/SlidingWindowCounterRateLimiterTest.java).
- [Bucketed tests](../../../../test/java/ratelimiter/slidingwindowcounter/BucketedSlidingWindowCounterRateLimiterTest.java).

```powershell
mvn test '-Dtest=SlidingWindowCounterRateLimiterTest,BucketedSlidingWindowCounterRateLimiterTest'
```

Read the basic version until you can explain the weighted previous count. Then
read the bucketed version until you can explain why slot 0 can safely be reused
at bucket 13 and why the oldest bucket can cause conservative rejection.
