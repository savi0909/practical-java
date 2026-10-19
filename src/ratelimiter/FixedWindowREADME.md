# Fixed-window rate limiter: short walkthrough

Java 17; package `ratelimiter`. See README.md for the shared client-facing design.

## Contract

- Share one `RateLimiterManager` across callers in one JVM. State is in memory; restart means fresh quotas.
- Each user has one reusable limiter with their own positive maximum and window duration. Unconfigured users default to 10 requests per 60 seconds.
- Windows align to the Unix epoch: for a 1,000 ms duration, [0, 1,000) and [1,000, 2,000) are separate windows.
- Return allow/reject without waiting for quota to refill. Same-user calls can wait for their limiter lock; no fairness or return-order guarantee.
- Configure users before their first request. Duplicate registration throws instead of replacing consumed quota. Inactive users remain stored.

## Follow the classes and methods

Read in this order:

1. `RateLimiter.java` defines the manager-facing contract: `isAllowed(userId)`.
2. `RateLimiterManager.registerUser(userId, maxRequests, windowMillis)` creates a user-specific `RateLimiterImpl`. `putIfAbsent` prevents replacing an existing limiter, including one already created with defaults.
3. `RateLimiterManager.isAllowed(userId)` finds the stored limiter or uses `computeIfAbsent` to create a default one atomically. The callback only constructs an object. After leaving the map operation, it calls that object's `isAllowed()`.
4. `RateLimiterImpl.isAllowed()` acquires only that limiter's ReentrantLock, reads the window number, resets an old count, rejects if full, or increments and accepts. Every access to mutable admission state is protected by this object's ReentrantLock.
5. `RateLimiterTest.main()` runs six focused checks. It is the executable example; `TimeSlot` is an older exercise class and is not used here.

The existing `RateLimiterImpl` now represents one user rather than the manager. Callers that previously constructed it to pass user IDs should construct `RateLimiterManager` instead.

Example use inside a Java method:

```java
RateLimiterManager manager = new RateLimiterManager();
manager.registerUser("alice", 1, 60_000);
manager.registerUser("bob", 2, 60_000);

manager.isAllowed("alice"); // true
manager.isAllowed("alice"); // false, if still in the same window
manager.isAllowed("bob");   // true: Bob owns a different limiter and lock
manager.isAllowed("bob");   // true, if still in the same window
```

## Interview explanation

“I keep a concurrent map from user ID to a stable limiter object. Each object owns its configuration and mutable window count. Atomic map insertion ensures concurrent requests reuse the same stored limiter. Admission happens outside the map callback, under that user's own ReentrantLock, so window reset, checking, and incrementing are atomic without holding another user's admission lock. Expected lookup and admission work is O(1), excluding map resizing and contention; stored state is O(U) for U distinct users. Same-user calls still coordinate, and map operations can contend briefly. This is a single-JVM fixed-window solution with boundary bursts and no cleanup.”

Likely follow-ups:

- Why one ReentrantLock per user? Each user has an independent critical section. lock() can wait for that user; unlock() in finally releases it on every exit. The default lock is nonfair.
- Can limits change live? This version rejects duplicate registration. Replacing an active limiter would reset quota; live changes need an explicit policy.
- What are the remaining limits? Epoch alignment uses wall-clock time, so clock jumps affect quotas. Boundary bursts, retained inactive users, and coordination across servers are deferred.

## Validation

ReentrantLock revision (2026-10-15): replaced per-user synchronized admission with a private default nonfair ReentrantLock. The same reset/check/increment critical section is protected, and finally always releases the lock. Java 17 compilation with `-Xlint:all` passed without warnings; all six existing checks passed. No performance or fairness claim is implied.

On 2026-10-15, the new configuration test first failed on a baseline still applying one common configuration. After refactoring, Java 17 compilation with all lint checks passed without warnings and all six checks passed: default limits/independence, boundaries, validation, per-user configuration, same-user concurrent quota, and cross-user progress.

The progress check pauses admission for one user and verifies a second finishes, using IDs with equal hashes. Small concurrent tests are not proof: correctness follows from stable map identity and the user object's ReentrantLock protecting the complete admission operation.

```powershell
javac --release 17 -Xlint:all -d out/study-rate-limiter-manager src/ratelimiter/*.java
java -cp out/study-rate-limiter-manager ratelimiter.RateLimiterTest
```
