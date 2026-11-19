# Java interview problem knowledge base

## LinkedIn source inventory and current ownership — 2 November 2026

[Rajat/Aryan coding source catalog](linkedin-prep/index.md) and [handover prompt](linkedin-prep/HANDOVER-PROMPT.md) now live here. These are source-linked candidates and variants, not 171 new assignments or additions to the 256-drill bank. Reconcile by contract before adding actual gaps; preserve stable group IDs, tiers and current practice history.

The user placed practical LLD in [JavaPracticalCoding](<C:/Users/abhis/IdeaProjects/Test/docs/interview-kb/linkedin-prep/index.md>) and HLD/API contracts in [System-Design](<D:/AA-DesignAndArch-Prep/System Design-RajatG/question-bank/index.md>). Existing mixed bank entries and code are preserved as reference; new canonical source material follows these destinations.

Study-order preference updated 2026-10-25: arrays → queues → priority queues/heaps
→ graphs (including general-graph BFS and DFS). This latest user override takes
precedence over strict Grokking chapter order; Grokking remains the primary reference. The ordered list, current starting point
and conditional prerequisite exceptions are maintained in
[the living learning plan](../learning-plan.md#default-dsa-sequence--2026-10-25).
This catalog's group IDs remain stable; catalog order does not override study order.
Explain any proposed departure to the learner before changing the plan.

Updated 2026-10-21. Scope: coding, DSA and LLD for Staff / Senior Staff / Principal Java rounds.

Project routing: this bank and `../learning-plan.md` hold coding preparation;
`C:/Users/abhis/IdeaProjects/Test` holds practical component study and its own
memory/context. System-design problems and learning now live in
[the D:/java-projects knowledge base](D:/java-projects/docs/interview-kb/README.md).
Reusable workflows: `$java-coding-prep` and `$practical-java-lld`. Skills point to
these current documents; they do not duplicate problem or attempt inventories.

The user selected arrays, hashing, windows, intervals, heaps, graphs, scheduling, schedulers and caches as priority families. The bank contains **19 groups and 256 candidate problems / drills**. IDs are stable references. Each topic is grouped into **Basic, Intermediate and Complex**. These are practice tiers, not an assigned sequence or employer frequency ranking. All implementation work is in **Java**; Python-, JavaScript- and other language-specific exercises are excluded.

Use a **flexible six-week horizon**, with a personal target of roughly **100–200 selected problems**. There is **no cap on the knowledge base**. The user chooses problems, categories, depth and timing. There is no day-by-day or week-by-week assignment and no requirement to finish the whole bank. This replaces the earlier three-week proposal and four-week allocation as the current preparation preference. No new attempt, completion or mastery is recorded.

[Source notes and access limits](sources.md). The Ishan sent-message review and the LeetCode / NeetCode comparison are integrated here. “Adapted” marks exercise details introduced during consolidation. Named problems and source membership do not verify employer interview frequency or code quality.

## Resource navigation

| Resource | Use |
|---|---|
| [Broader NeetCode problem directory](https://neetcode.io/solutions/course-schedule) | Use the searchable left sidebar as a reserve inventory. Scanned 798 titled entries across 19 source categories; 30 JavaScript entries are outside this Java-focused bank. Relevant gaps are integrated below with links for later solution study. |
| [LeetCode Top Interview 150](https://leetcode.com/studyplan/top-interview-150/) | Broad statement/editorial index; all 150 titles and linked editorial destinations were reviewed as an inventory. Selected gaps are merged below. Not a six-week completion mandate. |
| [NeetCode Blind 75](https://neetcode.io/practice?tab=blind75) | Reviewed all 75 listed problems across 18 source categories. They map into the groups below; equivalent variants are noted rather than treated as unrelated patterns. If the old link opens Core Skills, select NeetCode 150, reopen its list picker, then choose Blind 75. |
| [Course Schedule explanation](https://neetcode.io/solutions/course-schedule) | Direct reference for DFS cycle detection, Kahn's topological sort, complexity and common pitfalls. Pair with P6 Course Schedule II and P8 dependency execution. |
| [Minimum Window Substring explanation](https://neetcode.io/solutions/minimum-window-substring) | Direct reference for brute force vs sliding window, duplicate target characters and validity/shrinking rules; Java code is available. |
| [LLD solution index](https://github.com/prasadgujar/low-level-design-primer/blob/master/solutions.md) | Reference implementations after an attempt; language and completeness vary. |
| [InterviewReady Java LLD](https://github.com/InterviewReady/Low-Level-Design) | Inventory, cache, rate limiter, event bus, service orchestrator and load-balancer examples. |
| [Machine-coding collections](https://github.com/kumaransg/LLD) | Calendars, ride sharing, stock exchange, cricket dashboard and other problem statements/examples. |

The shared DSA-handbook link resolves to a [Topmate product page](https://topmate.io/tharun_polu/1632874). Only its public description was reviewed; the handbook contents were not obtained. NeetCode solution pages were sampled for the two references above; the review did not watch all 75 videos or audit every implementation. At the user's request, subsequent sidebar work used problem names and categories only; study the solutions when attempting those problems.

## Practice tiers

- **Basic:** warm-ups, collection operations and direct applications of one technique.
- **Intermediate:** choosing and maintaining an invariant, combining standard structures, or implementing a bounded component.
- **Complex:** harder algorithmic variants, several interacting invariants, or substantial concurrency/lifecycle requirements.

Visible NeetCode Easy/Medium/Hard labels inform these tiers where titles match. Java component tiers and modified contracts are our scope estimates; they are not official LeetCode ratings. Choose freely across tiers based on your gaps. The larger reference directory contains **768 algorithm entries** after excluding its JavaScript section; it overlaps this 256-entry bank and is not an additional 768 assignments. Its topic/tier counts are in the [source ledger](sources.md#broader-neetcode-problem-directory).

## Group index

| ID | Group | Scope | Basic | Intermediate | Complex | Total |
|---|---|---|---:|---:|---:|---:|
| P1 | [Arrays and two pointers](#p1) | Priority | 5 | 11 | 2 | 18 |
| P2 | [Hashing and prefix sums](#p2) | Priority | 7 | 6 | 1 | 14 |
| P3 | [Sliding windows and deques](#p3) | Priority | 1 | 13 | 4 | 18 |
| P4 | [Intervals and calendars](#p4) | Priority | 1 | 11 | 2 | 14 |
| P5 | [Heaps, top K and streaming order](#p5) | Priority | 1 | 8 | 5 | 14 |
| P6 | [Graphs: traversal, dependencies and paths](#p6) | Priority | 0 | 18 | 2 | 20 |
| P7 | [Scheduling algorithms and resource allocation](#p7) | Priority | 0 | 7 | 3 | 10 |
| P8 | [Java schedulers, executors and coordination](#p8) | Priority | 0 | 4 | 6 | 10 |
| P9 | [Caches and in-memory stores](#p9) | Priority | 0 | 5 | 6 | 11 |
| J1 | [Practical Java coding and data processing](#j1) | Supporting | 4 | 7 | 1 | 12 |
| B1 | [Linked lists](#b1) | Secondary | 5 | 10 | 1 | 16 |
| B2 | [Stacks and monotonic stacks](#b2) | Secondary | 2 | 8 | 1 | 11 |
| B3 | [Trees and tries](#b3) | Secondary | 6 | 9 | 5 | 20 |
| B4 | [Binary search](#b4) | Secondary | 1 | 6 | 1 | 8 |
| B5 | [Dynamic programming](#b5) | Secondary | 1 | 15 | 0 | 16 |
| B6 | [Backtracking](#b6) | Secondary | 0 | 9 | 1 | 10 |
| L1 | [Domain LLD and machine coding](#l1) | Supporting | 0 | 13 | 8 | 21 |
| B7 | [Bit manipulation](#b7) | Secondary | 5 | 3 | 0 | 8 |
| B8 | [Matrices and simulation](#b8) | Secondary | 0 | 5 | 0 | 5 |

<a id="p1"></a>

## P1. Arrays and two pointers

In-place updates, prefix/suffix passes, pointer invariants, overflow.

Sources: R1, R2, R3, G2, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

- Move Zeroes
- Best Time to Buy and Sell Stock
- Missing Number
- Valid Palindrome
- Merge Sorted Array

### Intermediate

- Maximum Subarray
- Product of Array Except Self
- Container With Most Water
- Rotate Array
- Maximum Product Subarray
- 3Sum Closest
- 3Sum
- Jump Game
- Gas Station
- Maximum Sum Circular Subarray
- Jump Game II

### Complex

- Trapping Rain Water
- First Missing Positive

<a id="p2"></a>

## P2. Hashing and prefix sums

Key design, frequency maps, prefix sums, ordered buckets, exact membership.

Sources: A1, A2, R1, R2, G2, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

- Two Sum
- Valid Anagram
- First Unique Character in a String
- Contains Duplicate
- Distinct visitors per store (Map + Set)
- Isomorphic Strings
- Word Pattern

### Intermediate

- Group Anagrams
- Subarray Sum Equals K
- Longest Consecutive Sequence
- Insert Delete GetRandom O(1)
- Longest Subarray with Sum K (signed integers)
- Ransom Note

### Complex

- All O(1) Data Structure (inc/dec/min/max key)

<a id="p3"></a>

## P3. Sliding windows and deques

Window validity, shrinking rules, monotonic deques; do not assume sum windows work with negative numbers.

Sources: A2, R1, R2, R3, G2, LC150 / NC75 where applicable, NC-DIR; see [source ledger](sources.md).

### Basic

- Maximum Sum Subarray of Size K

### Intermediate

- Longest Substring Without Repeating Characters
- Find All Anagrams in a String
- Longest Continuous Subarray With Absolute Diff <= Limit
- Longest Substring With At Most K Distinct Characters
- Longest Repeating Character Replacement
- Max Consecutive Ones III
- Subarray Product Less Than K
- Minimum Size Subarray Sum (positive integers)
- Substring with Concatenation of All Words
- [Maximum Sum of Distinct Subarrays With Length K](https://neetcode.io/solutions/maximum-sum-of-distinct-subarrays-with-length-k)
- [Frequency of the Most Frequent Element](https://neetcode.io/solutions/frequency-of-the-most-frequent-element)
- [Binary Subarrays with Sum](https://neetcode.io/solutions/binary-subarrays-with-sum)
- [Count Number of Nice Subarrays](https://neetcode.io/solutions/count-number-of-nice-subarrays)

### Complex

- Minimum Window Substring
- Sliding Window Maximum
- [Shortest Subarray with Sum at Least K](https://neetcode.io/solutions/shortest-subarray-with-sum-at-least-k)
- [Subarrays with K Different Integers](https://neetcode.io/solutions/subarrays-with-k-different-integers)

<a id="p4"></a>

## P4. Intervals and calendars

Endpoint conventions, overlap rules, sweep lines, ordered maps.

Sources: R1, R2, G2, G3, LC150 / NC75 where applicable, NC-DIR; see [source ledger](sources.md).

### Basic

- Meeting Rooms (check for any conflict)

### Intermediate

- Merge Intervals
- Insert Interval
- Non-overlapping Intervals
- Meeting Rooms II
- Employee Free Time
- Interval List Intersections
- Car Pooling
- Minimum Number of Arrows to Burst Balloons
- My Calendar I [adapted calendar drill]
- Summary Ranges
- [Remove Covered Intervals](https://neetcode.io/solutions/remove-covered-intervals)

### Complex

- Data Stream as Disjoint Intervals
- [Minimum Interval to Include Each Query](https://neetcode.io/solutions/minimum-interval-to-include-each-query)

<a id="p5"></a>

## P5. Heaps, top K and streaming order

Bounded heaps, two heaps, k-way merge, ties, stale entries.

Sources: R1, R2, G2, LC150 / NC75 where applicable, NC-DIR; see [source ledger](sources.md).

### Basic

- Kth Largest Element in a Stream

### Intermediate

- Kth Largest Element in an Array
- Top K Frequent Elements
- K Closest Points to Origin
- Find K Closest Elements
- Reorganize String
- Top K Frequent Words
- Find K Pairs with Smallest Sums
- [Furthest Building You Can Reach](https://neetcode.io/solutions/furthest-building-you-can-reach)

### Complex

- Find Median from Data Stream
- Merge K Sorted Lists
- Smallest Range Covering Elements from K Lists
- IPO (capital-constrained project selection)
- [Minimum Cost to Hire K Workers](https://neetcode.io/solutions/minimum-cost-to-hire-k-workers)

<a id="p6"></a>

## P6. Graphs: traversal, dependencies and paths

Visited-state invariants, cycle detection, topological ordering, BFS vs weighted paths, union-find. Compare DFS cycle detection with Kahn's algorithm. Define prerequisite-edge direction, include isolated nodes, and reject cycles. Course Schedule II returns an order; P8.3 extends dependencies into execution.

Sources: A2, R1, R2, G2, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

No separate entry in this tier yet; use the linked source inventory for optional extensions.

### Intermediate

- Number of Islands
- Clone Graph
- Course Schedule II
- Network Delay Time
- [Course Schedule (cycle detection)](https://neetcode.io/solutions/course-schedule)
- Rotting Oranges
- Cheapest Flights Within K Stops
- Bus Routes
- Min Cost to Connect All Points
- Is Graph Bipartite?
- Redundant Connection
- Number of Closed Islands
- Top-rated similar movies in a graph [feed adaptation]
- Pacific Atlantic Water Flow
- Graph Valid Tree
- Number of Connected Components in an Undirected Graph
- Surrounded Regions
- Evaluate Division

### Complex

- Word Ladder
- Alien Dictionary

<a id="p7"></a>

## P7. Scheduling algorithms and resource allocation

Greedy vs DP vs feasibility search; distinguish arranging jobs from executing jobs.

Sources: R1, R2, G2, G3, LC150 / NC75 where applicable, NC-DIR; see [source ledger](sources.md).

### Basic

No separate entry in this tier yet; use the linked source inventory for optional extensions.

### Intermediate

- Task Scheduler (cooldown)
- Capacity To Ship Packages Within D Days
- Minimum Cost For Tickets
- Minimum railway platforms [same interval/heap family as Meeting Rooms II in P4]
- Maximum Length of Pair Chain
- [Single-Threaded CPU](https://neetcode.io/solutions/single-threaded-cpu)
- [Process Tasks Using Servers](https://neetcode.io/solutions/process-tasks-using-servers)

### Complex

- Maximum Profit in Job Scheduling
- Cinema scheduler: fit, replace and maximize revenue [feed adaptation]
- [Meeting Rooms III (assign rooms with delays and ties)](https://neetcode.io/solutions/meeting-rooms-iii)

<a id="p8"></a>

## P8. Java schedulers, executors and coordination

Contracts, bounded resources, visibility, cancellation, interruption, shutdown, failure semantics.

Sources: R1, R2, G3, local, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

No separate entry in this tier yet; use the linked source inventory for optional extensions.

### Intermediate

- Priority job scheduler
- Bounded blocking queue
- Periodic scheduler: fixed rate vs fixed delay [adapted]
- Retry scheduler with backoff and retry limits [adapted]

### Complex

- Delayed job scheduler with cancellation
- Dependency-aware parallel task executor
- Bounded thread pool without ExecutorService
- CompletableFuture fan-out/fan-in with timeout and partial failure
- Fair per-tenant scheduler [adapted]
- Idempotent task submission and duplicate suppression [adapted]

<a id="p9"></a>

## P9. Caches and in-memory stores

Eviction vs expiry, capacity invariants, atomic compound operations, time and loader failure.

Sources: R1, R2, G3, G4, local, LC150 / NC75 where applicable, NC-DIR; see [source ledger](sources.md).

### Basic

No separate entry in this tier yet; use the linked source inventory for optional extensions.

### Intermediate

- LRU Cache
- TTL key-value store with injectable time
- Weighted-capacity cache [adapted]
- Negative caching with bounded TTL [adapted]
- [Time Based Key-Value Store (historical timestamp lookup; distinct from TTL expiry)](https://neetcode.io/solutions/time-based-key-value-store)

### Complex

- LFU Cache
- Single-flight cache loader [adapted stampede drill]
- Cache with runtime-swappable eviction policy
- Versioned cache invalidation under concurrent updates [adapted]
- Refresh-ahead cache serving stale data under a defined policy [adapted]
- Expiry jitter and stampede stress lab [adapted]

<a id="j1"></a>

## J1. Practical Java coding and data processing

Collections, Streams, comparators, parsing, immutable state and exact money.

Sources: A3, R1, R2, G3, LC150 / NC75 where applicable, Ishan sent-resource review; see [source ledger](sources.md).

### Basic

- Employee queries: group/count/highest salary by department
- Multi-field Comparator with explicit tie rules
- Immutable snapshot with defensive copies [adapted Java drill]
- Generic collection-copy utility with bounded wildcards [adapted generics drill]

### Intermediate

- Highest average score per student (including negative averages)
- Top N file collections in a nested directory model
- Stock Price Fluctuation (timestamp corrections + min/max/latest)
- Split money exactly with BigDecimal and deterministic remainder allocation [adapted]
- CSV parser with quoted fields and malformed-input handling [adapted details]
- Paginated API client with bounded retries and error handling [adapted details]
- Encode and Decode Strings

### Complex

- Restricted JSON parser with validation

<a id="b1"></a>

## B1. Linked lists

Pointer ownership, cycle safety, mutation and restoration.

Sources: A2, A4, R1, G2, LC150 / NC75 where applicable, Ishan sent-resource review; see [source ledger](sources.md).

### Basic

- Reverse Linked List
- Intersection of Two Linked Lists
- Palindrome Linked List
- Middle of the Linked List
- Merge Two Sorted Lists

### Intermediate

- Linked List Cycle II (find entry; first implement boolean cycle detection)
- Remove Nth Node From End
- Reorder List
- Swap Nodes in Pairs
- Partition List
- Design Linked List
- Copy List with Random Pointer
- Add Two Numbers
- Reverse Linked List II
- Sort List

### Complex

- Reverse Nodes in K-Group

<a id="b2"></a>

## B2. Stacks and monotonic stacks

Stack invariants, amortized cost, numeric overflow and nested parsing.

Sources: A2, R1, G2, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

- Implement Queue Using Stacks
- Valid Parentheses

### Intermediate

- Min Stack
- Daily Temperatures
- Decode String
- Longest Valid Parentheses
- Max Stack
- Simplify Path
- Evaluate Reverse Polish Notation
- Basic Calculator

### Complex

- Largest Rectangle in Histogram

<a id="b3"></a>

## B3. Trees and tries

Traversal state, recursive depth, serialization contracts and prefix search.

Sources: A2, R1, R2, G2, LC150 / NC75 where applicable, Ishan sent-resource review; see [source ledger](sources.md).

### Basic

- Diameter of Binary Tree
- Invert Binary Tree
- Symmetric Tree
- Maximum Depth of Binary Tree
- Same Tree
- Subtree of Another Tree

### Intermediate

- Binary Tree Right Side View
- Implement Trie
- Validate Binary Search Tree
- Binary Tree Zigzag Level Order Traversal
- Construct Binary Tree from Preorder and Inorder
- Merge two N-ary trees by key with stable child order [feed adaptation]
- Binary Tree Level Order Traversal
- Kth Smallest Element in a BST
- Design Add and Search Words Data Structure

### Complex

- Lowest Common Ancestor of a Binary Tree
- Serialize and Deserialize Binary Tree
- Word Search II
- Lowest Common Ancestor of a Binary Search Tree
- Binary Tree Maximum Path Sum

<a id="b4"></a>

## B4. Binary search

Boundary invariants and a monotonic predicate; answer-search practice also appears in P7.

Sources: R1, R2, G2, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

- Search Insert Position

### Intermediate

- Search in Rotated Sorted Array
- Find First and Last Position in Sorted Array
- Search a 2D Matrix
- Koko Eating Bananas
- Find Minimum in Rotated Sorted Array
- Find Peak Element

### Complex

- Median of Two Sorted Arrays

<a id="b5"></a>

## B5. Dynamic programming

State meaning, transitions, base cases, reconstruction and memory reduction.

Sources: A2, R1, R2, G2, LC150 / NC75 where applicable, Ishan sent-resource review; see [source ledger](sources.md).

### Basic

- Climbing Stairs

### Intermediate

- House Robber II
- Coin Change
- Longest Increasing Subsequence
- Word Break
- Decode Ways
- Longest Common Subsequence
- Partition Equal Subset Sum [curriculum addition]
- Minimum Path Sum
- House Robber
- Longest Palindromic Substring
- Palindromic Substrings
- Unique Paths
- 0/1 Knapsack
- Unbounded Knapsack
- Edit Distance

### Complex

No separate entry in this tier yet; use the linked source inventory for optional extensions.

<a id="b6"></a>

## B6. Backtracking

Choice/undo invariants, duplicate elimination and pruning.

Sources: A2, R1, G2, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

No separate entry in this tier yet; use the linked source inventory for optional extensions.

### Intermediate

- Word Search
- Combination Sum II
- Permutations
- Palindrome Partitioning
- Sudoku Solver
- Subsets
- Combination Sum
- Generate Parentheses
- Letter Combinations of a Phone Number

### Complex

- N-Queens

<a id="l1"></a>

## L1. Domain LLD and machine coding

Domain invariants, explicit APIs, state transitions, testability and requirement changes.

Sources: A5, R1, R2, G3, G4, LC150 / NC75 where applicable, Ishan sent-resource review, NC-DIR; see [source ledger](sources.md).

### Basic

No separate entry in this tier yet; use the linked source inventory for optional extensions.

### Intermediate

- Parking Lot
- Snake Game with wraparound and input rules
- Vending Machine
- Library Management
- Per-user rate limiter (fixed/sliding/token bucket variants)
- Chess move validator
- Tic-Tac-Toe
- Inventory management with atomic reservations [adapted contract]
- Configuration manager with change subscriptions
- Load balancer with pluggable selection policy [adapted contract]
- Ride-sharing / cab-booking application
- Cricket match dashboard
- [Design Twitter (in-memory feed component)](https://neetcode.io/solutions/design-twitter)

### Complex

- Splitwise / expense sharing
- Jira-style task and workflow engine
- Ticket booking with expiring seat reservations
- In-memory Pub/Sub with independent consumer progress
- Configurable synchronous/asynchronous logger
- Service orchestrator [adapted Java contract]
- In-memory database with secondary indexes
- Stock exchange / limit order book

<a id="b7"></a>

## B7. Bit manipulation

XOR cancellation, masks, carries, fixed-width integers and Java signed/unsigned shifts. Missing Number is already cataloged in P1; revisit its XOR solution here.

Sources: LC150, NC75, IM-PATTERNS, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

- Single Number
- Number of 1 Bits
- Counting Bits
- Reverse Bits
- Add Binary

### Intermediate

- Sum of Two Integers
- Single Number II
- Bitwise AND of Numbers Range

### Complex

No separate entry in this tier yet; use the linked source inventory for optional extensions.

<a id="b8"></a>

## B8. Matrices and simulation

Coordinate conventions, traversal bounds, in-place updates and separating old state from new state.

Sources: LC150, NC75, LC150 / NC75 where applicable; see [source ledger](sources.md).

### Basic

No separate entry in this tier yet; use the linked source inventory for optional extensions.

### Intermediate

- Rotate Image
- Spiral Matrix
- Set Matrix Zeroes
- Valid Sudoku
- Game of Life

### Complex

No separate entry in this tier yet; use the linked source inventory for optional extensions.

## How to use the bank

- The user chooses which problems to attempt in each category and when, within the flexible six-week horizon. The catalog and linked source lists provide options, not an assigned workload. A changed contract can be a follow-up rather than a second full assignment.
- Scheduling algorithms choose an arrangement or optimum. Schedulers execute work with time, concurrency and lifecycle contracts. Intervals provide shared prerequisites; do not count railway platforms and meeting-room allocation as unrelated pattern coverage.
- P4 and P7 bridge algorithms; P8 and P9 bridge Java engineering and LLD. L1 contains the remaining domain exercises. Keep distributed system scale discussions for the separate system-design knowledge base.
- For algorithm attempts: explain the invariant, implement in Java, check boundaries, justify time/space and compare one alternative. Probe negatives, duplicates, ties, overflow and empty input as applicable.
- For scheduler/cache/LLD attempts: define APIs and invariants, execute a minimal scenario, handle a requirement change. Probe simultaneous operations, timeout/cancellation, interruption, shutdown, stale state and failure cleanup where relevant.
- In practice mode, present one unseen problem and let the user propose an approach. Record independent / hint-assisted / guided / demonstrated only after an actual attempt in the existing handover log.
- When implementation is requested, follow AGENTS.md: working code, compile/run, and the critique in the source file. Read rate-limiter.md before rate-limiter implementation work.

## Existing local reference material

These are navigation pointers, not a fresh correctness audit or evidence of independent mastery.

| Family | Existing files |
|---|---|
| Windows | [No-repeat substring](../../src/main/java/dsa/slidingwindow/NoRepeatSubstring.java), [bounded spread](../../src/main/java/dsa/slidingwindow/LongestBoundedSpreadSubarray.java), [reference minimum window](../../src/main/java/arrays/ref/MinimumWindowSubstring.java) |
| Hashing | [First unique character](../../src/main/java/dsa/hashing/FirstNotRepeatingCharacterInString.java), [reference prefix sums](../../src/main/java/arrays/ref/SubarraySumEqualsK.java) |
| Intervals | [Merge intervals](../../src/main/java/dsa/arrays/MergeOverlappingIntervals.java), [reference meeting rooms](../../src/main/java/arrays/ref/MeetingRoomsII.java) |
| Heaps | [Median](../../src/main/java/dsa/heap/MedianOfAStream.java), [stream kth largest](../../src/main/java/dsa/heap/KthLargestNumberInStream.java), [top K](../../src/main/java/dsa/heap/TopKFrequentNumbers.java) |
| Graphs | [Clone graph](../../src/main/java/dsa/graph/CloneGraph.java), [task dependencies](../../src/main/java/dsa/graph/MinRoundsToFinishTasks.java) |
| Caches | [LRU](../../src/main/java/lld/cache/LRUCache.java), [LFU](../../src/main/java/lld/cache/lfu/LFUCache.java) |
| Java concurrency | [Fan-out/fan-in app](../../src/main/java/temp/FreshUserDataApp.java), [scoped lock](../../src/main/java/concurrency/ScopedLock.java) |
| Rate limiting | [Exercise workflow](../../rate-limiter.md), [sliding window](../../src/main/java/ratelimiter/slidingwindow/README.md) |

## Secondary material retained for later

JVM/JFR/heap and thread dumps; memory retention; pool saturation; OOP/SOLID and patterns applied to the exercises above. Broad Spring/JPA, SQL and distributed-system architecture should not expand the current coding list by default. System-design topics and links are maintained in [the separate system-design KB](D:/java-projects/docs/interview-kb/README.md).

