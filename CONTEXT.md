# Study context

## Current-window implementation — 2026-10-19
- Learner chose two requests per current five-second window with no idle carry-over. Implemented basic fixed-window request path in rate-limiter-lab, adapting the existing interface/per-user lock implementation; parent Java exercises unchanged. Each user ID is a tenant.
- Java 21 strict compilation passed; focused quota, user isolation, exact boundary, minute-idle, and concurrent quota checks passed. No million-user benchmark or proof by test claimed. See lab CONTEXT.md for details; cleanup and advanced policies deferred.

## Latest rate-limiter direction — 2026-10-19
- Saved learner preference: Java 21+ incremental implementation, concise explanations, interview-level design decisions. Reported discussion covers Fixed Window, Sliding Window Counter, brief Token Bucket, and ConcurrentHashMap concurrency; no mastery inferred.
- Continue in rate-limiter-lab. Current conceptual step concerns lazy token refill for tenant/API buckets at large cardinality; no code changes or checks in this step.

## Dedicated rate-limiter repository (2026-10-18)
- Learner requested a separate project/sub-repository to understand rate limiting end to end. Created rate-limiter-lab under this workspace as an independent local Git repository; the parent workspace has no Git repository. No remote or submodule configured.
- New README records the proposed requirements and a short learning sequence: core admission, concurrency, tenant/user policy, manual cleanup, scheduled worker lifecycle, caller demo. New AGENTS.md, MEMORY.md, and CONTEXT.md preserve the one-small-step-per-turn preference.
- Continue extended rate-limiter study in rate-limiter-lab, adapting existing src/ratelimiter code as the starting point instead of maintaining matching evolving solutions. Existing parent Java files remain unchanged.
- Setup increment only: no Java implementation or executable checks supplied. Next: basic request-path increment; end-to-end learning remains the broader goal.

## Fixed-window cleanup requirements under review (2026-10-18)
- Learner requested requirements for review before basic implementation, with emphasis on in-memory storage, tenant/user-specific limits, window reset, and practical cleanup-thread behavior. No implementation authorized in this step.
- Proposed scope: improve existing src/ratelimiter classes; one JVM, concurrent callers, no persistence or cross-server quota sharing. In-memory storage demonstrates local state management, not Redis distribution semantics.
- Proposed identity: (tenantId, userId), independent user quotas; tenant default policy with optional user override and a system fallback. Aggregate tenant quotas and live policy updates deferred.
- Proposed semantics: retain existing epoch-aligned fixed windows; reset lazily on the first request in a new window, independently of cleanup scheduling. Wall-clock jumps and boundary bursts remain explicit limitations.
- Proposed cleanup: one scheduled worker with fixed delay (example: 30 seconds), removing only expired window state; retain separately configured policies. Cleanup racing with admission must not detach an in-use limiter and create a second quota for the same key. No premature size-based eviction of active counters.
- Include worker shutdown and failure handling; sweep cost O(U) for U stored states, with no strict memory bound from periodic cleanup alone. Start with focused boundary, independent-policy, and cleanup checks; concurrency checks when implementing removal coordination.
- Pending learner review. Java code unchanged; no compilation or checks run for this requirements-only increment. Supplied requirements do not establish implementation or mastery.

## Current project focus (2026-10-18)
Practical Java engineering: concurrency, thread-safe components, rate limiters,
ID generators, caches, queues, executors, and basic logging framework design.
This broader focus supersedes the earlier interview-only framing; the eight-topic
sequence is an initial set of exercises, not an exhaustive project scope.
Continue with short explanations and basic implementations, one study increment
at a time. Existing implementations and progress remain preserved. Logging
framework design is in scope for future study; no implementation is claimed here.

## Current rate-limiter organization and design (2026-10-15)
- Learner requested moving both limiter implementations to a root package and studying how clients choose an algorithm per use case; explicitly selected `src/ratelimiter`, not the Java default package.
- Moved both existing topic packages' Java files, checks, and notes into package `ratelimiter`. Notes are FixedWindowREADME.md and TokenBucketREADME.md; root README.md gives a concise common design and runnable RateLimiterExample. Study index links updated. Unrelated topic implementations preserved.
- Existing RateLimiter.isAllowed(userId) is the common strategy interface: RateLimiterManager supplies the fixed-window choice, TokenBucketRateLimiter now implements the same interface. Fixed-window RateLimiterImpl remains a per-user object with ReentrantLock. Token-bucket algorithm and its existing synchronized instance-wide critical section are preserved.
- Clients construct/configure one reusable limiter per use case, with a map selecting login/fixed-window or search/token-bucket. Separate instances isolate quota namespaces; reconstructing on each call would reset state. Unknown use cases throw. No factory, distributed design, policy hot-swap, or token-bucket per-user locking refactor added.
- Final validation: root package compiled targeting Java 17 with -Xlint:all -Werror without warnings; all six fixed-window and four token-bucket checks passed. Existing token-bucket burst test uses the common interface. RateLimiterExample ran and printed true for login and search. All workspace Java sources compiled targeting Java 21 with -Xlint:all -Werror without warnings; unrelated check runners and performance harness were not rerun. Checks do not establish independent mastery or prove concurrency correctness.
- Next: follow the shared interface, use-case selection, and retained state in a small design increment. Earlier historical package paths below are superseded by `src/ratelimiter`.

## Latest request and current state (2026-10-15)
- Learner explicitly requested basic versions of all remaining topics with short tutorials, overriding the earlier one-topic-per-turn rule for this request.
- Learner then clarified: preserve the three existing topics and use ReentrantLock for all new implementations. All 14 existing topic files were verified unchanged by SHA-256 comparison.
- Added topics 4–8: expiringstore, idempotencyregistry, inventoryreservation, boundedqueue, delayedscheduler. Each has a basic implementation, three focused checks, and a short README tutorial. src/study/README.md links all eight topics.
- Scope: one JVM, in-memory state, Java 17-compatible new code, no dependencies. All new classes use ReentrantLock; bounded queue uses two Conditions. Existing locking choices were preserved.
- Baselines: lazy TTL cleanup; permanent duplicate-key admission registry without result replay; one-product permanent stock deduction; interruptible bounded FIFO; one-worker JDK scheduled executor with a locked lifecycle. Scheduler close cancels pending work, permits running work to finish, and does not wait for termination.
- Verification: all five new packages compiled with `javac --release 17 -Xlint:all -Werror`, no warnings. All workspace sources compiled with `--release 21 -Xlint:all -Werror`, no warnings; the existing ID comparison harness uses a Java 21 API and was left untouched. All eight check runners passed: ID checks, six fixed-window checks, four token-bucket checks, and three checks in each new package. Performance harness not run.
- Limits: small concurrent checks are not proof; correctness explanations identify the complete locked operation. No exhaustive edge-case, load, or distributed testing. Prior failing stubs were observed to fail each new runner before implementation.
- These are supplied study examples, not independent implementation or mastery. Next: wait for the learner to select a topic, then study it in small increments.

The sections below retain earlier study history; the latest request above supersedes their pending-work notes.

## Candidate
Experienced Staff/Principal backend engineer. Strong Java, distributed systems, payments, and IAM background.

## How to teach
- Short, plain language. Small steps. See MEMORY.md.
- Show the solution directly. No mock interview.
- Improve existing code; avoid duplicate solutions.
- A few basic tests first. Detailed tests later if needed.
- One useful increment per turn, then wait.
- Latest preference: explain code through clear comments at key points, especially the reasoning and concurrency protection. Avoid separate learning guides unless requested; keep required README tracking minimal.

## Current problem
Revisiting fixed-window rate limiter in src/study/fixedwindowratelimiter at the learner's request. Token-bucket work is preserved.

Latest lock revision (2026-10-15): RateLimiterImpl.isAllowed() now uses a private per-user default nonfair ReentrantLock instead of synchronized. lock() covers the clock read, window reset, limit check, and increment; unlock() is in finally, including rejection and exception paths. Manager behavior is unchanged. Same-user callers may block on their lock; other users have independent locks. Java 17 `-Xlint:all` compilation passed without warnings; all six existing fixed-window checks passed. These checks are not proof; atomicity follows from the lock covering all mutable admission state. No performance improvement or fairness guarantee claimed.

Latest step (2026-10-15): implemented the agreed manager/per-user-object design. RateLimiterManager implements the existing RateLimiter.isAllowed(userId) interface; RateLimiterImpl now owns one user's configuration, window ID, accepted count, and synchronized isAllowed() method. Manager registerUser configures a user before traffic, rejects duplicates, and never replaces active state. Unconfigured users get defaults via computeIfAbsent; admission runs outside the map callback. One shared manager per quota namespace/JVM; no singleton enforcement. Inline learning comments and a short README tutorial explain the classes and methods. Same-user contention remains; other users have independent admission locks. No mastery claimed.

Current token-bucket step (2026-10-15): learner confirmed fixed-window rate limiting is done and requested the next implementation. Supplied independent per-client buckets, full initial capacity, fractional refill using nanoTime, and one token per accepted call. One instance monitor protects lookup, refill, check, and consumption. In-memory only; no cleanup or distributed coordination. Learning comments explain the logic. Topic 3 is in progress; supplied code does not establish mastery. The fixed-window notes below describe the previous topic.

Current study step (2026-10-15): replaced RateLimiterImpl's method-wide synchronized lock and HashMap with ConcurrentHashMap.compute. Lookup, window replacement, checking, and incrementing are atomic per user. Different users can generally progress concurrently; same-user calls and colliding bins can contend, so this is not lock-free or a latency guarantee. Clock read stays inside compute. A per-call boolean holder returns the admission decision. Added learning comments and one focused cross-user concurrency check. Topic remains in progress; no mastery claimed.

Previous topic: compare CAS, synchronized, and ReentrantLock ID allocation.
- Keep getId() and getInstance().
- AtomicLong compare-and-set protects allocation and overflow checking.
- One shared instance per namespace. Allocation order is not observed return order.
- New instances and restarts can repeat IDs. Exhaustion throws permanently.
- Duplicate IdGenerator and large IdGeneratorCheck removed.
- Added getSynchronizedId(): plain long with overflow check and increment protected by the instance monitor.
- Comparison methods use independent sequences, never reset. Do not mix their IDs in one namespace.
- Added getLockedId(): independent counter, default nonfair ReentrantLock, overflow check and increment inside lock, unlock in finally.
- IdTester applies the same three basic checks to all three methods. Harness warms all methods, rotates order over three rounds, and now uses one and 1,000 platform threads (200,000 calls per thread, 200 million IDs per measured round at 1,000 threads). Learner requested no virtual-thread runs; that option was removed. No counter resets. Coordination and worker result timeouts are 60 seconds.

## Progress
Topic 1 solution supplied; the updated provider and three basic checks are ready. No independent implementation or mastery claimed.
Topic 2 completed per learner's confirmation (2026-10-15). Topic 3 first implementation supplied; review in small increments.

## Verification

Manager/per-user object revision (2026-10-15): new per-user configuration check first failed against a baseline retaining one common configuration. Refactored fixed-window package then compiled with `javac --release 17 -Xlint:all` without warnings; all six checks passed. Checks cover defaults, boundaries, input/duplicate registration validation, different user limits and durations, same-user concurrent quota, and progress for colliding-hash IDs while another user's admission is paused. Stable stored limiter identity and each object's monitor establish atomicity; small concurrency tests are not proof or a bounded-latency guarantee. Unrelated topic code and harnesses were not changed or rerun.

Token bucket (2026-10-15): package compiled targeting Java 17 with all lint checks and no warnings; all four focused checks passed (burst/client independence, fractional refill/capacity cap, validation, concurrent consumption with frozen time). Initial stub failed the first-token assertion before implementation. Full workspace Java 17 compilation failed on the existing IdComparisonHarness virtual-thread API; subsequent checks used fresh output directories. All current sources compiled targeting Java 21 without warnings; token-bucket checks, all five current fixed-window checks, and current IdTester passed. An accidental stale IdTester run from the failed compilation's output directory was stopped and is not validation. Small concurrent checks are not proof; token-bucket atomicity follows from its monitor. Floating-point thresholds are approximate; inactive clients remain stored.

Per-user concurrency update (2026-10-15): fixed-window package compiled targeting Java 17 with `-Xlint:all`, no warnings; all five tests passed. New test first failed on the old global lock (Bob timed out while Alice was paused inside admission), then passed with compute. Existing same-user concurrent quota test passed too. Test uses different hash bins; not proof of universal nonblocking behavior. Correctness relies on atomic compute and all Window-state accesses staying inside it. No unrelated ID harness runs.

Per-client state step (2026-10-15): fixed-window package compiled with `javac --release 17 -Xlint:all` without warnings; all four existing checks passed. Interface documentation and minimal README notes updated; implementation behavior unchanged. The concurrent check is not proof: the monitor protecting the complete admission operation establishes the quota invariant under a stable clock.
Rate-limiter comment update (2026-10-15): added concise learning comments to RateLimiterImpl explaining single-instance/JVM scope, epoch-aligned window boundaries, shared-state protection, atomic check-and-increment, rejection behavior, clock jumps, and retained user entries. No behavior changes. Entire rate-limiter package compiled targeting Java 17 with all lint checks and no warnings; all four existing tests passed, including the concurrent quota check. This check is not proof: correctness relies on the instance monitor covering lookup, replacement, check, and increment. Topic remains in progress; no mastery claimed.

Comment update (2026-10-15): added learning comments to IdProviderImpl covering shared state, safe singleton publication, independent sequences, allocation versus return order, restart behavior, atomic check/increment, overflow, and CAS retries. No behavior changes. Provider, interface, and IdTester compiled targeting Java 17 with all lint checks and no warnings; all three variants passed the basic increasing-ID, overflow, and five-thread checks. These checks are not proof of concurrency correctness. Whole-package Java 17 compilation failed because the current comparison harness references a Java 21 virtual-thread API, contrary to the earlier platform-only notes; harness left unchanged and not run in this step.

Latest 1,000-platform-thread run (2026-10-15, JDK 21.0.9): 200 million IDs per method per round, median of three measured rounds: CAS 9990.88 ms, synchronized 11603.39 ms, ReentrantLock 3936.95 ms. Platform comparison completed successfully. A virtual-thread run subsequently failed the 10-second start-gate timeout; retry with longer coordination timeout was cancelled at the learner's request. No valid 1,000-virtual-thread measurement exists. Virtual-thread support was removed afterward; platform workload and measurement logic unchanged.

Latest three-method revision (2026-10-15): all Java sources compiled targeting Java 21 with lint enabled and no warnings. All three ID variants passed basic increasing-ID, overflow, and five-thread checks; existing rate-limiter checks passed. Both harness modes completed. For 100 threads/20 million IDs, platform medians: CAS 877.75 ms, synchronized 1139.01 ms, ReentrantLock 415.12 ms; virtual medians: CAS 1504.44 ms, synchronized 1180.63 ms, ReentrantLock 603.19 ms. Demo only; not a universal ranking. This CPU-only workload does not isolate pinning. Batch elapsed/call count is inverse throughput, not average wait, hold time, or maximum lock latency. Earlier numbers below are historical.

On 2026-10-15, all Java sources compiled targeting Java 17 with all lint checks enabled and no warnings. IdTester passed its three basic checks: increasing IDs, overflow rejection, and five-thread uniqueness. The existing rate-limiter suite also passed all four checks. A small concurrent run is not proof of correctness. Earlier larger checks applied to the removed solution, not this revision.

Latest comparison (2026-10-15): ID package recompiled targeting Java 17 with lint enabled and no warnings. Harness completed: one thread/200,000 IDs CAS 1.51 ms vs synchronized 3.36 ms; 100 threads/20,000,000 IDs CAS 979.09 ms vs synchronized 1092.32 ms. CAS took about 10.4% less elapsed time for the 100-thread case. Local timing demo only; not a general speed guarantee or rigorous benchmark. Basic correctness checks and rate-limiter checks passed in the prior revision; this change only adjusts thread count and result timeout.

## Decisions
- Plain Java, Java 17-compatible source, including the platform-only comparison harness. Installed JDK: 21.0.9.
- Java 21 synchronized pinning is not a blanket reason to avoid every short synchronized block. Oracle recommends replacing frequent long-lived pinning around blocking work; JDK 24 JEP 491 removes synchronized-related pinning. Do not explain these timing differences as proof of pinning.
- Each problem has its own package under src/study/ with README, code, and checks together.
- RateLimiterManager preserves RateLimiter.isAllowed(String userId); RateLimiterImpl is one user's object with ReentrantLock protecting isAllowed(). Admission runs outside ConcurrentHashMap.computeIfAbsent. Per-user settings are registered before traffic; duplicate registration throws to avoid resetting quota. Epoch-aligned wall-clock windows, boundary bursts, and retained client entries remain limitations.
- Advanced variants and detailed test cases wait until needed or requested.

## Next
Wait for the learner's next instruction. Follow the fixed-window manager/per-user-object tutorial in one small increment if requested; token-bucket refill walkthrough remains available afterward. Do not create another topic until requested.
Later: expiring store, idempotency registry, inventory reservation, bounded queue, delayed scheduler.
