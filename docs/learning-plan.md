# Learning Plan — Principal Engineer Backend Interview Prep

Living document. Study order groups problems by dependency — each phase leans on a
pattern introduced in the phase before it. Update this file whenever new problems are
added or the order changes; don't let it go stale.

Status legend: ✅ solved+verified in repo · 📥 imported from `D:\aa-coding-prep-java` (compiles,
not yet reviewed/critiqued here) · 🔲 not yet added

> **Caveat:** ✅ means a reference solution exists and was verified in this repo — it does **not**
> mean you solved it independently (see `coding-guidance-handover.md` §5). Count a category as
> done only after a blind re-solve with no help; the 📥 "twin" drills below are for that.

## Current preparation priorities — 2026-10-18

Repository update (2026-10-19): the active IDE project is `C:\Users\abhis\hello`,
published to `savi0909/practical-java`. Prefer separate modules, such as Rate
Limiter Lab, for performance-testing runs and benchmarking; keep each lab's
workloads, configuration, instructions, and results together. This records an
organization preference, with no change to exercise completion or mastery.

Target **Java coding rounds at Staff / Senior Staff / Principal level**. Use a
**four-week horizon**, reserving roughly **2 hours/day for Java coding + concurrency**
as the primary preparation block. Java language depth includes concurrency, the JVM,
performance, and modern Java.

Choose exercises that combine algorithmic correctness with practical Java engineering:
thread-safe components, rate limiters, ID generators, caches, queues, executors,
synchronization, and performance-sensitive implementation. Keep core algorithms in
rotation and use the phases below as a dependency map, not a mandatory linear syllabus.
These exercise families are selection priorities, not newly assigned or completed problems.

System design / distributed systems runs as a parallel secondary track. Prioritize
IAM, gateways, Kafka, and gRPC for architectural depth; cover GraphQL only to
architecture-discussion level. Defer Go / Rust unless a specific role requires them.

The Okta practice protocol and recorded attempts remain in force. The next queued
category remains graph traversal and dependency ordering; these broader preferences
do not establish progress or change recruiter-confirmed requirements.

## Company focus — 2026-10-18

**Primary targets:** Atlassian, Google, Meta, and comparable coding interviews.
**Secondary targets:** Adobe, Intuit, Target, Visa, Amazon. These are application
priorities, not a ranking of interview difficulty; use the same correctness bar.

Prepare a shared algorithm foundation, with a separate code-design track for
Atlassian. Do not let concurrency or JVM study displace timed algorithm practice.
The allocations and example exercises below are preparation recommendations,
not verified company question frequencies or actual interview questions.

### Two-hour daily block

| Minutes | Work |
|---|---|
| 75 | Algorithm practice: one unseen medium, boundary tests, one follow-up; on mock days use the full block for a timed round and review. |
| 30 | Java code design / concurrency: implement one small contract or extend yesterday's component. |
| 15 | Blind re-solve of a previous failure or review of a concrete Java/JVM gap exposed by coding. |

Use mostly medium problems; introduce selected hard variants once the underlying
pattern can be implemented independently. Avoid spending the month collecting
hard-problem solutions. Reference code in this repo does not establish fluency.

### Four-week order

| Week | Algorithm emphasis | Java implementation emphasis | Evidence to collect |
|---|---|---|---|
| 1 | Graph BFS/DFS, components, dependency ordering and cycles; brief window/interval/heap revisits. | Cache invariants, interfaces, testable clocks; single-threaded correctness first. | Implement an unseen graph medium and blind re-solve a previously assisted pattern. |
| 2 | Tree traversals, BSTs, LCA; binary search on a monotonic answer; heap/interval combinations. | Rate limiter, then atomicity and locking follow-ups. | Working code with boundary tests, justified complexity, and a changed requirement. |
| 3 | Backtracking, basic DP, prefix sums; shortest paths / union-find if traversal is independent. | Bounded queue or executor exercise: interruption, shutdown, visibility, failure handling. | Transfer a known technique to an unfamiliar statement; explain concurrency guarantees. |
| 4 | Mixed timed rounds, blind re-solves, selected hard follow-ups targeted at observed gaps. | Timed code-design rounds with tests and one requirement change. | Repeatable independent performance across mixed topics; log errors and assistance. |

Keep the current graph/dependency exercise next. Do not restart at arrays just
because company targets changed. Trees and binary search move into the near-term
rotation; basic DP and backtracking are required coverage in this month, even
though the older general roadmap labels them "Later". Advanced variants remain
conditional on independent fundamentals. The phases below remain a dependency map.

### Practice formats and representative families

| Focus | Practice format / representative families |
|---|---|
| Shared algorithm foundation | Graph dependencies and grid traversal; trees/LCA; intervals and heaps; windows/prefix sums; binary search; basic DP and backtracking. |
| Atlassian emphasis | Alternate data-structure rounds with a 60-minute code-design mock: cache, rate limiter, ranking component, or scheduler. Define APIs, execute tests, then handle a requirement change. These component choices are our drills, not claimed company questions. |
| Google emphasis | Practice deriving an invariant for an unfamiliar variant, explaining alternatives, and adapting to a follow-up; retain broad algorithm coverage. This is our preparation emphasis, not a distinct verified rubric. |
| Meta emphasis | Add 40-minute two-problem coding drills after single-medium accuracy is stable; use medium problems from different patterns and validate both. This is a training target, not a confirmed current interview format. |
| Secondary targets | Reuse the same foundation and code-design drills. Adjust only when role-specific recruiter guidance establishes a different format or topic. |

For a single-medium drill, aim for a correct approach within 5–10 minutes,
implementation within another 20–25, and meaningful validation before time expires.
These are training goals, not employer cutoffs. For code design, assess API clarity,
state invariants, tests, and adaptability; add thread safety only when requested by
the contract. Occasionally code without IDE assistance, then compile and test.

Readiness requires multiple unseen, independently completed attempts on different
days, plus a successful blind revisit. Track time only when measured. Preserve the
existing independent / hint-assisted / guided / demonstrated labels and attempt log.

### Public guidance supporting the emphasis

Reviewed 2026-10-18; public guides can differ from an individual role's loop.

- [Atlassian engineering handbook](https://www.atlassian.com/company/careers/resources/interviewing/engineering): coding covers data structures and code design.
- [Atlassian backend guide](https://www.atlassian.com/dam/jcr:931efbe4-fa94-4988-9867-7ad172981257/Backend.pdf?cdnVersion=1365): describes 60-minute coding interviews, executed code, testing, communication, and alternatives. The PDF carries a 2023 copyright; confirm the actual loop with the recruiter.
- [Google's official coding preparation video](https://www.youtube.com/watch?v=6ZZX9iIgFoo): communication, algorithms/data structures, coding, and testing. Published 2019; useful fundamentals, not confirmation of a 2026 loop.
- [Amazon software-development interview topics](https://amazon.jobs/content/en-gb/how-we-hire/interview-prep/software-development-topics): fundamentals, effective problem solving, and practice outside an IDE; ask the recruiter about role-specific subjects.

No current primary-source format was verified here for Meta, Adobe, Intuit,
Target, or Visa. Their inclusion records the candidate's targets; it does not
establish company-specific topic distributions.

## Topic map (where code lives)
All paths under `src/main/java/`. `dsa.*` = algorithm drills, `lld.*` = low-level design.

| Topic | Packages |
|---|---|
| Hashing | `dsa.hashing`, `interviews.Prices` |
| Sliding window | `arrays.ref` (A/B sets), `arrays.sliding`, `dsa.slidingwindow` |
| Two pointers / prefix | `arrays.ref` (C set), `dsa.twopointers` |
| Array basics / binary search | `dsa.arrays`, `dsa.binarysearch` |
| Linked list / stack | `dsa.linkedlist`, `dsa.stack`, `dsa.datastructures` |
| Heaps | `arrays.ref` (D set), `dsa.heap` |
| Trees | `dsa.tree` (+ `.mindepth`, `.rightview`) |
| Graphs | `dsa.graph` |
| Backtracking | `dsa.backtracking` (+ `.queens`, `.sudoku`, `.maze`, `.knighttour`, `.graphcoloring`, `.hamiltonian`) |
| DP | `dsa.dp`, `arrays.ref.LongestIncreasingSubsequence` |
| LLD | `lld.cache` (+ `.lfu`, `.listbased`), `tweet`, `bank`, `access` |
| Logging framework design | `logfk` (design notes only; implementation pending) |

## Phase 0 — Array basics (optional warm-up, ~1 session)
- 📥 `dsa.arrays` — `AlternatingSums`, `AddBorder`, `AllLongestStrings`, `AlmostIncreasingSequence`,
  `PopularityAnalysis` (monotonic check), `FindMaxUsingDivideAndConquer`, `CountSubarrays`
  (left/right run-length arrays — preview of Phase 4's prefix/suffix pass).
- 📥 `dsa.binarysearch.BinarySearch` — classic + boundary variants. Do before heaps; the
  `lo <= hi` vs `lo < hi` invariant comes up again in "search answer space" problems.

## Phase 1 — Warm-up / hashmap keying
1. ✅ `GroupAnagrams.java` — simplest hashmap-signature problem, no window concept yet.
2. ✅ `LongestSubstringWithoutRepeating.java` — first sliding window (shrinks on a
   "no duplicates" invariant). Bridges into Phase 2/3.
   - ✅ `interviews.Prices#solution` — first non-repeating character; `LinkedHashMap` for
     insertion order + a `-1` sentinel to mark "seen twice". Same hashmap-keying muscle as
     GroupAnagrams, no window yet.
- Drills: 📥 `dsa.hashing` — `FirstDuplicateInArray`, `FirstNotRepeatingCharacterInString`
  (compare with `Prices#solution`), `GroupSimilarTitles` (= GroupAnagrams, Netflix framing),
  `PossiblesumsGoogle` (set-of-reachable-sums; really a bounded-knapsack DP — revisit in Phase 10).

## Phase 2 — Fixed window
3. ✅ `arrays.ref.MaxSumSubarraySizeK.java` (A1) — simplest window: slide, no shrink logic.
4. ✅ `arrays.ref.FirstNegativeInEachWindow.java` (A3) — same fixed window + a monotonic deque.
5. ✅ `arrays.ref.FindAnagramIndices.java` (A2) — match-count trick (`satisfied` counter), reused in B7.
6. ✅ `arrays.sliding.MaxSum.java` — A1 re-solved from scratch, also returns the window's start index.
7. ✅ `arrays.sliding.MaxIndexPerWindow.java` — sliding-window maximum returning indices; shows why
   the reversible-sum trick fails for max and a monotonic deque is required (same deque as A3).
8. ✅ `arrays.sliding.MaxInEveryWindow.java` — same window, returns max values. Carries the O(n*k)
   brute force as a differential-test oracle, plus a deliberately-bad ArrayList deque + benchmark
   showing why `remove(0)` (O(size) shift) makes ArrayDeque non-negotiable here.
- Drill: 📥 `dsa.slidingwindow.MaxSumSubArrayOfSizeK` — Grokking version of A1; diff against yours.

## Phase 3 — Variable window
6. ✅ `arrays.ref.MinSubArrayLen.java` (B1) — canonical shrink-while-valid loop.
7. ✅ `arrays.ref.MaxConsecutiveOnesIII.java` (B4) — simplest "budget" variant of the same loop.
8. ✅ `arrays.ref.LongestKDistinctSubstring.java` (B2) — frequency-map version of the invariant.
9. ✅ `arrays.ref.FruitIntoBaskets.java` (B3) — B2 with k=2; confirms pattern recognition under a new name.
10. ✅ `arrays.ref.LongestRepeatingCharReplacement.java` (B5) — hardest single-invariant window
    (stale-max correctness trick).
11. ✅ `arrays.ref.SubarrayProductLessThanK.java` (B6) — same shrink loop, product instead of sum.
12. ✅ `arrays.ref.MinimumWindowSubstring.java` (B7) — closer: A2's match-counting + B1's shrink loop.
- Drills (📥 `dsa.slidingwindow`, Grokking twins of the above — re-solve blind, then diff):
  `MinSizeSubArraySum`↔B1, `ReplacingOnes`↔B4, `LongestSubstringKDistinct`↔B2,
  `CharacterReplacement`↔B5, `NoRepeatSubstring`↔Phase 1 #2,
  `FindLongestSubarraySums` (CodeSignal longest/shortest subarray by sum, positives only).

## Phase 4 — Two-pointer & prefix
13. ✅ `arrays.ref.SubarraySumEqualsK.java` (C4) — do right after B7: looks like a window problem but
    isn't (negatives break monotonicity) — contrast lands best while windows are fresh.
14. ✅ `arrays.ref.TwoSumSorted.java` (C1) — simplest two-pointer.
15. ✅ `arrays.ref.ContainerWithMostWater.java` (C2) — two-pointer + a greedy argument to justify out loud.
16. ✅ `arrays.ref.TrapRainWater.java` (C3) — harder two-pointer, same greedy family.
17. ✅ `arrays.ref.ProductExceptSelf.java` (C5) — prefix/suffix pass, standalone technique.
18. ✅ `arrays.ref.MaxDistinctWindowSum.java` (C6) — capstone: A1's fixed window + B2/B3's distinctness tracking.
    - ✅ `interviews.Prices#solutionPrices` — best time to buy/sell stock (LC 121). Running-minimum
      single pass; sits next to C5 as the other "carry one extremum forward" problem. Its delta-array
      framing *is* Kadane, so treat it as the on-ramp to the Phase 10 DP set (LC 122 → sum of positive
      deltas; LC 123/188 → k-transaction DP). `solutionPricesWithDays` covers the "which days?" follow-up.
- Drills (📥 `dsa.twopointers`), in order:
  `PairWithTargetSum`↔C1 then `PairWithTargetSumUsingMap` (unsorted → hashmap tradeoff),
  `RemoveDuplicates`, `RemoveElement`, `MoveZerostoLeft` (read/write-pointer compaction family),
  `SortedArraySquares` (converge from both ends), `TripletSumToZero` → `TripletSumCloseToTarget`
  (sort + fix one + two-pointer), `Sort0s1s` → `DutchFlag` (3-way partition; same as quicksort's).

## Phase 5 — Heaps / priority queues
19. ✅ `arrays.ref.KthLargestElement.java` (D1) — foundational bounded min-heap.
20. ✅ `arrays.ref.KthLargestInStream.java` (D2) — same heap, now stateful/online.
21. ✅ `arrays.ref.TopKFrequentElements.java` (D3) — heap keyed on map entries.
22. ✅ `arrays.ref.MeetingRoomsII.java` (D5) — heap + greedy interval scheduling.
23. ✅ `arrays.ref.MergeKSortedLists.java` (D4) — k-way merge, comparator on objects instead of primitives.
24. ✅ `arrays.ref.MedianFinder.java` (D6) — hardest: two synchronized heaps, saved for last.
- Drills (📥 `dsa.heap`): `KLargestNumbers`↔D1, `KthLargestNumberInStream`↔D2,
  `TopKFrequentNumbers`↔D3, `FrequencySort` (D3 + rebuild string), `KClosestElements`
  (heap on |x−X|; binary-search + two-pointer alternative is O(log n + k)),
  `MedianOfAStream` / `MedianOfAges`↔D6 (same two-heap code twice — pick one, delete the other later).

## Phase 6 — Linked lists & stacks (📥 new)
Do before revisiting D4 — `MergeKSortedLists` here is the node-based version.
25. 📥 `dsa.linkedlist.LinkedListNode` — shared node + `LinkedList` helpers (insert/print). Read first.
26. 📥 `dsa.linkedlist.RemoveNodesWothValFromLinkedList` — dummy-head trick.
27. 📥 `dsa.linkedlist.LinkedListPalindrome` — fast/slow pointer + reverse second half.
28. 📥 `dsa.linkedlist.MergeSortList` — split at middle + merge; O(n log n), O(log n) stack.
29. 📥 `dsa.linkedlist.MergeKSortedLists` — heap of list heads (compare with `arrays.ref` D4).
30. 📥 `dsa.stack.Stack` → `MinStack` → `maxStack` (+ `MaxStackDemo` driver) — auxiliary-stack
    invariant: top of aux = min/max of everything below.
31. 📥 `dsa.datastructures.Bag` — Sedgewick resizing-array bag + `Iterable`; amortized O(1) add.

## Phase 7 — Trees (📥 new)
BFS first (queue + level-size loop), then DFS.
32. 📥 `dsa.tree.InOrderTraversal` — iterative inorder with explicit stack.
33. 📥 `dsa.tree.LevelOrderTraversal` — the level-size BFS template everything below reuses.
34. 📥 `dsa.tree.AverageOfLevel`, `dsa.tree.ZigZagLevelOrderTraversal`,
    `dsa.tree.rightview.Solution`, `dsa.tree.mindepth.Solution` (early exit on first leaf).
35. 📥 `dsa.tree.CountAllPathSum` — DFS with path list; count sub-paths summing to S
    (prefix-sum-on-path is the O(n) upgrade — same idea as C4).

## Phase 8 — Graphs (📥 new)
36. 📥 `dsa.graph.FriendCircleCountInMatrixUsingDFS` — connected components on adjacency matrix
    (union-find is the alternative worth naming).
37. 📥 `dsa.graph.CloneGraph` + `dsa.graph.Node` (holds `Solution#clone`) — DFS with
    old→new map to handle cycles.

## Phase 9 — Backtracking (📥 new)
Choose → explore → un-choose. Easiest to hardest:
38. 📥 `dsa.backtracking.PermutationsOfLetters` — the template (used[] + path copy).
39. 📥 `dsa.backtracking.PhoneNumbertoLetter` / `PhoneNumbertoLetterEdu` — product of choices.
40. 📥 `dsa.backtracking.queens` (N-Queens), `.sudoku`, `.maze` — constraint check before recurse.
    Note: `queens.App` runs N=100 and does not finish in reasonable time — lower N to ~8–20.
41. 📥 `dsa.backtracking.knighttour`, `.graphcoloring`, `.hamiltonian` — graph-flavored backtracking.

## Phase 10 — Dynamic programming (📥 new)
42. 📥 `dsa.dp.ClimbingStairs` — Fibonacci recurrence. Currently O(n) array and **throws
    AIOOBE for n=1**; fix + reduce to O(1) rolling vars as the exercise.
43. 📥 `dsa.dp.HouseRobber` / `HouseThief` — take/skip recurrence; same problem twice.
44. 📥 `dsa.dp.DecodeNumbersToLetters` — top-down memo (decode ways).
45. 📥 `dsa.dp.RodCutter` — unbounded knapsack shape, top-down memo. **`main` NPEs on line 15**
    (leftover `Node` scratch code, pre-existing in source) — delete those lines to run it.
46. ✅ `arrays.ref.LongestIncreasingSubsequence` — O(n²) DP vs patience-sort O(n log n).
47. 📥 `dsa.dp.DpScratchpad` — scratch file (rod-cut variant, 2D `f(n,k)` table, a productExceptSelf
    attempt). Mine for practice, don't treat as reference.

## Phase 11 — LLD: caches (📥 new)
48. 📥 `lld.cache.listbased.LRUCache` — naive: `java.util.LinkedList` remove is O(n). Start here to
    articulate *why* it's wrong.
49. 📥 `lld.cache.LRUCacheBasic` — HashMap + hand-rolled doubly linked list, O(1) get/put.
50. 📥 `lld.cache.LRUCache` — generic, thread-safe version (`ConcurrentHashMap`) — critique its
    locking granularity.
51. 📥 `lld.cache.lfu.LFUCache` — frequency buckets; O(1) LFU needs freq→list map + minFreq.
- Then `tweet.TwitterApp` (fan-out read vs write), `bank`, `access`.

## Backlog (not yet added)
- 🔲 Graphs: course schedule / topological sort, number of islands, Dijkstra, union-find.
- 🔲 Trees: BST validation, LCA, serialize/deserialize.
- 🔲 DP: 0/1 knapsack, edit distance, LCS.
- 🔲 Review pass: critique each 📥 file per CLAUDE.md rules and flip it to ✅.

## Okta Round 2 practice track (parallel to the phases above)
State, roadmap, and attempt log live in `coding-guidance-handover.md` (same folder). Problems are
presented one at a time and solved by the user; each gets a scaffold (statement, stub, verified
test scenarios) in the matching `dsa.<topic>` package and flips to ✅ here once solved + critiqued.

**Order for this track is priority-driven (handover §7), not the phase order above** — the
phases are a dependency map for the whole repo; Okta Round 2 needs the medium tier of the
Revisit/Next categories first.

Done at easy level (self-reported 2026-10-10, assistance not recorded): longest substring
without repeating characters; merge overlapping intervals. Intervals had no entry in this plan
before — treat Merge Intervals as the on-ramp to the intervals rungs below.

Current requested revisit (2026-10-10): `dsa.slidingwindow.LongestSubstringWithoutRepeating`
— scaffold added with the four supplied examples and five additional sample checks;
implementation and approach review pending. This does not establish independent completion.

Problem 2 revisit (2026-10-10): `dsa.arrays.MergeOverlappingIntervals` — scaffold with
three supplied examples and five additional sample checks; approach and implementation pending.

Medium ladder (tentative — adjust from observed results; one problem active at a time):

| # | Category | Medium problem | Status |
|---|---|---|---|
| 1 | Sliding window (extremum invariant) | Longest subarray with bounded spread (max − min ≤ limit) | 🔲 `dsa.slidingwindow.LongestBoundedSpreadSubarray` — active |
| 2 | Intervals | Insert interval, or min removals to make non-overlapping | not presented |
| 3 | Hash aggregation + bounded heap | Top-K with tie-break comparator | not presented |
| 4 | Graph / dependency ordering | Min rounds to finish tasks (cycle → -1) | 🔲 `dsa.graph.MinRoundsToFinishTasks` — scaffolded, queued |
| 5 | Graph traversal | Grid components (islands) or shortest steps in a grid | not presented |
| 6 | Composite data structures | O(1) insert/delete/getRandom, or LRU variant | not presented |
| 7 | Binary search on answer | Capacity / rate feasibility search | not presented |

Later (only after the rungs above are independent): stacks/queues, trees, prefix sums, greedy,
basic DP, backtracking. Repo gaps this track exposes: no intervals set, thin graph set (2 files),
binary search is classic-only, and composite structures sit late (Phase 11) despite being "Next".

## Concurrency reference examples

- Design in progress (2026-10-19): `logfk` — application API versus backend,
  logger/event/appender/layout contracts, immutable startup configuration, and a
  synchronous console/file pipeline followed by a bounded async exploration.
  See `src/main/java/logfk/README.md`; design only, no implementation or completion
  claim. Start with contracts, then one working console path before file/config I/O.
- ✅ `ratelimiter.fixedwindow.FixedWindowRateLimiter` — demonstrated `RateLimiter` abstraction,
  immutable per-user `RateLimitConfig`, default/override provider, epoch-aligned windows,
  atomic admission with `ConcurrentHashMap.compute`, and injected clock. Configuration
  snapshots act as revision tokens; updates lazily reset the associated counter safely.
  JUnit 5 checks cover quotas, defaults/overrides, independent users, exact boundary
  reset, skipped windows, validation, concurrent admission, and reconfiguration.
  Shared admission/configuration types now live in `ratelimiter.common`.
  Reference example, not an independent solve; see the implementation header for critique.
- ✅ `ratelimiter.slidingwindow.SlidingWindowRateLimiter` — demonstrated exact sliding-window
  log with per-user timestamp deques, atomic expiry/admission, injected clock, and configuration
  revision resets. Tests cover rolling boundaries, individual expiry, rejected requests,
  independent quotas, concurrent admission, validation, and timestamp overflow boundaries.
  Run `ratelimiter.slidingwindow.Main` to compare fixed and sliding boundary behavior.
  Reference example, not an independent solve; critique is in the implementation header.
- ✅ `ratelimiter.slidingwindowcounter.SlidingWindowCounterRateLimiter` — demonstrated
  previous/current counters with weighted estimation and atomic per-user rollover/admission.
  Follow with `BucketedSlidingWindowCounterRateLimiter`: 5-second buckets in a circular
  array (12 previous plus active for 60 seconds), full oldest-bucket retention for
  conservative admission. Five focused tutorial tests per version cover quota, boundaries,
  expiry/rollover, and configuration resets. Both have critique headers and a shared README
  walkthrough; run `ratelimiter.slidingwindowcounter.Main`. These are reference examples,
  not independent solves. Follow with the token-bucket core below.
- ✅ `ratelimiter.tokenbucket.TokenBucketRateLimiter` — increment 1 demonstrated:
  full initial burst capacity, lazy batch refill (2 tokens per 5 seconds), retained
  partial periods, capacity clamping, and atomic per-tenant admission via `compute`.
  Preserves shared configuration revision resets and injected Clock. Focused checks
  cover timing boundaries, tenant isolation, long idle gaps, validation, and concurrency.
  Run `ratelimiter.tokenbucket.Main`; walkthrough and critique live beside the code.
  Reference example, not an independent solve. Next: monotonic ticker, then safe idle eviction.
- ✅ `concurrency.ScopedLock` — demonstrated AutoCloseable guard for ReentrantLock;
  verified normal/exception cleanup, nested holds, duplicate close, and owner enforcement.
  Study after basic lock/try/finally; this is a reference example, not an independent solve.

## Source material
- `docs/coding-guidance-handover.md` — Okta Round 2 prep handover (copied from Downloads 2026-10-10).
- `C:\Users\abhis\Downloads\array-window-practice.md` — origin of the A/B/C problem set.
- Heap/PQ problems (D1-D6) were proposed, not sourced from a file.
- `D:\aa-coding-prep-java` (git repo, `com.abhi.*`) — merged 2026-10-10 into `dsa.*` / `lld.*`.
  Source left untouched. Changes made during merge: package lines rewritten; renames
  `dsa/arrays/Main`→`CountSubarrays`, `dsa/Main`→`DpScratchpad` (slf4j logger → `System.out`),
  `netflix/Solution`→`MaxStackDemo`, `tree/dfs/TreeNode.java`→`CountAllPathSum.java`;
  `LinkedListNode` made `public` so `lld.cache.listbased.LRUCache` can import it.
