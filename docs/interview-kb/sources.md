# Source ledger and review limits

## Combined source catalog — 2 November 2026

The [coding-round source index](linkedin-prep/index.md) and [handover](linkedin-prep/HANDOVER-PROMPT.md) combine discoverable Rajat/Aryan posts with the inherited R1/R2 feed notes below. [Shared coverage and deduplication](<D:/AA-DesignAndArch-Prep/System Design-RajatG/question-bank/ARYAN-RAJAT-REVIEW.md>) distinguish new public-source review from this historical extension review. Full six-month feed enumeration remains incomplete. No new runtime checks, practice attempts or independently verified employer-frequency claims were added.

Reviewed through the Codex extension in the existing signed-in Chrome window on 2026-10-21. This ledger supports the [coding / DSA / LLD problem bank](README.md). It records source discovery and topic extraction, not a code audit.

## Coverage

| Source | What was covered | Boundary |
|---|---|---|
| [Google Chat: Interview Backend](https://mail.google.com/mail/u/0/#chat/space/AAQAkr1Q6k4) | Loaded history from the space's April 16 creation notice through October 21; opened 62 deduplicated shared destinations. | Opening a destination did not always expose its content: removed posts, paywalls and one video-only page are excluded from substantive evidence. |
| [Ishan LinkedIn thread](https://www.linkedin.com/messaging/thread/2-OTkxM2ExNTEtMTBkNy00ODI4LThkNjQtZmI0MDA2MDJlNjUxXzEwMA==/) | Loaded the visible conversation back to its February 24, 2025 beginning; latest visible messages were March 18, 2026. Reviewed embedded resource posts and extracted 35 distinct directly typed external links. | Personal profile links and unrelated material were excluded. Embedded PDF cards were visible, but their complete PDF pages were not reviewed. |
| R1: [Rajat activity](https://www.linkedin.com/in/rajatgajbhiye/recent-activity/all/) | Reviewed recent rendered activity down through August 2, 2026. | Intended range was July 21–October 21. Stopped before completing the oldest portion after the user said the material was sufficient. |
| R2: [Aryan activity](https://www.linkedin.com/in/aryan-raskar-077640253/recent-activity/all/) | Reviewed recent rendered activity down through August 14, 2026. | Same intended range; remaining older activity passed at the user's instruction. |
| R3: [Tauseef activity](https://www.linkedin.com/in/tauseeffayyaz/recent-activity/all/) | Reviewed recent rendered activity down through July 27, 2026. | Same intended range; remaining older activity passed. Reposts and repeated versions were merged by topic. |

These activity feeds can mix reposts with original dates. The dates above describe the ordinary feed entries reached, not a guarantee that every post in a calendar interval was served by LinkedIn. Older posts explicitly shared in the message threads remain useful sources and were not discarded merely because of the activity-feed date filter.

## Selected coding and LLD sources

| ID | Source | Contribution |
|---|---|---|
| A1 | [Coding Interview Pattern Series: hashing](https://dglearning.substack.com/p/the-coding-interview-pattern-series) | Two Sum, duplicates, anagrams, first unique character, prefix-sum counting. |
| A2 | [Tauseef: coding patterns](https://www.linkedin.com/posts/tauseeffayyaz_coding-patterns-for-maang-interviews-activity-7335633860086456322-tz7l) and [Rajat: DSA patterns](https://www.linkedin.com/posts/rajatgajbhiye_dsa-a-day-can-keep-interview-rejections-away-activity-7329851275108397056-I8L3) | Windows, pointers, heaps, intervals, BFS/DFS, dependencies, tries, DP and backtracking. |
| A3 | [Rajat: Java backend questions](https://www.linkedin.com/posts/rajatgajbhiye_i-recently-attended-interviews-for-java-backend-share-7462718118268907520-gJ-J) | Core Java, Streams, synchronization, futures, practical coding. |
| A4 | [Shivam: linked-list patterns](https://www.linkedin.com/posts/shivam-shrivastava-ab6404179_most-people-underestimate-linked-lists-don-activity-7340220511957766144-3uGa) | Cycle entry, reversal, reorder, merge, intersection, palindrome and deletion patterns. |
| A5 | [Rajat: LLD roadmap](https://www.linkedin.com/posts/rajatgajbhiye_i-screwed-up-3-interviews-because-i-didn-activity-7334579228966830081-kCxt) and [LLD examples](https://www.linkedin.com/posts/rajatgajbhiye_never-bet-against-system-design-trust-me-activity-7354359752459436032-Nr0a) | OOP/SOLID, interfaces, patterns, Parking Lot, Splitwise, games, cache and scheduler examples. |

Useful dated feed entries retained in synthesis:

- R1, October 15: cinema scheduling, corrected timestamp prices, word search, character inventory.
- R1, October 14: parallel API calls with CompletableFuture, highest student average including negative scores, circuit breaker.
- R1, October 13: executor queues, rejection, execute vs submit, shutdown.
- R1, September 30: stream statistics, graph-based similar movies, minimum window, N-ary tree merge.
- R1, September 20: Snake, file collections, stock price corrections, All O(1) data structure.
- R1, September 2: bounded thread pool without ExecutorService, dependency execution, blocking/coordination exercises, pub/sub and file operations.
- R1, August 27: array/string/list/tree/graph/search/DP question list.
- R2, October 20: windows, heaps, intervals, graph traversal and code-design components.
- R2, October 12 and September 3: cache, median stream, Word Ladder, merge K, directed cycles, top K, LIS and Word Break.
- R3, October 11 and August 20: coding pattern taxonomy; September 4: GitHub resource directory.

Use the linked activity feed and the date to locate these entries. Canonical post permalinks were not extracted for every activity card. A date and prompt are provenance, not independent confirmation of an employer's interview process.

## GitHub links followed

The shared [Tauseef repository directory](https://www.linkedin.com/posts/tauseeffayyaz_if-you-want-to-elevate-your-coding-and-system-share-7518590673462444032-OEd3/) led through resource landing pages to original repositories. The original repository/index is the preferred reference.

| ID | Repository or index | Reviewed scope / use |
|---|---|---|
| G1 | [Arpit: DSA, LLD and HLD](https://github.com/arpit20adlakha/Data-Structure-Algorithms-LLD-HLD) | README resource index; followed selected coding/Java/LLD links. Broad directory, not a single syllabus. |
| G2 | [neerazz/FAANG topic index](https://github.com/neerazz/FAANG/blob/master/Top_LeetCode_Questions_By_Topic.md) | Actual categorized problem table, plus repository README. Java solutions/resources are reference material; historical acceptance rates and difficulty labels were not used to rank the bank. |
| G3 | [LLD primer](https://github.com/prasadgujar/low-level-design-primer) and [question bank](https://github.com/prasadgujar/low-level-design-primer/blob/master/questions.md) | Read README and detailed question list: scheduler, queue, JSON/CSV parsers, inventory, calendar, cache, pub/sub, logger, bookings and state machines. Some prompts are underspecified or dated; narrow the contract before implementation. |
| G4 | [Java Design Patterns](https://github.com/iluwatar/java-design-patterns) | Repository/module inventory and README-level reference. Consult selected patterns only when they improve an exercise. |
| G4 | [Cache LLD](https://github.com/anomaly2104/cache-low-level-system-design) | README, Java/Maven project and problem-statement pointer. No fresh implementation correctness review. |
| G4 | [Ticket-booking LLD](https://github.com/anomaly2104/ticket-booking-low-level-system-design) | README, scope and explicit further-improvement list; not a production-ready guarantee. |
| G4 | [Cab-booking LLD](https://github.com/anomaly2104/lld-cab-booking-ola-uber-grab-lyft) | README; Java 8-era example, not current Java 21 guidance. |
| G4 | [Logger LLD](https://github.com/arpit20adlakha/Logger-LLD) | README: Chain of Responsibility implementation example. |
| G4 | [Vehicle rental](https://github.com/gopalbala/carrental) | README requirements for vehicle inventory, booking, return, cancellation and invoicing. |
| G4 | [Distributed ID generator](https://github.com/gopalbala/distributed-idgen) | README discussion of UUID/sequence/Snowflake choices; architecture extension, not the default coding exercise. |
| — | [Tech Interview Handbook](https://github.com/yangshun/tech-interview-handbook) | Original README and preparation directory overview. |
| — | [kdn251/interviews](https://github.com/kdn251/interviews) | README/algorithm taxonomy and Java solution directory overview. |
| — | [Coding Interview University](https://github.com/jwasham/coding-interview-university) | Broad README/contents review; its much larger curriculum does not become the three-week plan. |
| — | [Sefism Library](https://github.com/tauseeffayyaz/sefism-library) | Large resource directory, partial README review; used to find primary repositories rather than copy its full curriculum. |
| — | [BattleShip](https://github.com/arpit20adlakha/BattleShip) | README/project overview; optional game-design example. |
| — | [Snake and Ladder Java](https://github.com/championswimmer/low-level-design-problem/tree/main/solutions/java/03-snakeladder), [Wordle Java](https://github.com/championswimmer/low-level-design-problem/tree/main/solutions/java/02-wordle) | Confirmed Java example folders; implementations not audited. |
| — | [Design Patterns for Humans](https://github.com/kamranahmedse/design-patterns-for-humans) | Conceptual reference; examples are not a Java implementation reference. |
| SD | [Transferred system-design reference](D:/java-projects/docs/interview-kb/README.md#transferred-resource-and-review-scope) | System Design Notes chapter/topic-index review is preserved in the destination KB. System-design learning is now owned by D:/java-projects; this row retains cross-track provenance only. |

This was targeted link-following, not an exhaustive recursive crawl of every link in every repository. Remaining broad directories, promotional resources and unrelated languages were passed when the user asked to consolidate.

## Exclusions and confidence

- Two Google Chat LinkedIn destinations were removed: [Amazon DSA post](https://www.linkedin.com/posts/omkar-bulbule_amazon-rejected-a-candidate-in-the-dsa-round-share-7461772192804974592-4y55) and [Razorpay post](https://www.linkedin.com/posts/omkar-bulbule_razorpay-interviewer-tell-me-how-you-stop-share-7462500705333903360-U-Jd). Their missing contents were not inferred.
- Several Medium member-only articles exposed previews; full content was not reviewed. The YouTube destination without useful transcript/body text was not treated as watched.
- Company names, salaries, interview percentages, and claims of “most asked” or “exact questions” are anecdotes/marketing here. They do not establish current employer requirements.
- Confidence is high that the named topics/problems appeared in the reviewed material; confidence in employer-frequency claims is insufficient. The shortlist order is a preparation judgment aligned with the user's priorities.
- Adapted labs make vague advice executable. For example, cache-breakdown discussions become a single-flight loader drill; retries become a bounded retry scheduler. These are our exercise designs.
- No GitHub repository was cloned, installed, built or executed. Reading a README or directory does not certify its code.

## Technical review notes

Do not carry source simplifications into implementation contracts:

- State HashMap costs with their assumptions; avoid presenting average constant-time lookup as an unconditional worst-case guarantee.
- Separate All O(1) key-count buckets from Insert/Delete/GetRandom; a feed conflated their APIs.
- For Min Stack encoding, test integer overflow. For average scores, define rounding for negatives. For interval problems, define whether touching endpoints overlap.
- For caches and schedulers, define what happens on failure, expiry, cancellation and concurrent requests before coding.
- For idempotency, require an atomic claim and a defined stored outcome/recovery path; a read-then-write cache check is not sufficient.
- Inventory decrement alone is not a complete reservation or payment protocol. Model conditional allocation, expiry and stale writers.
- A Bloom filter is an approximate prefilter; positive membership in permissions/likes requires an exact source of truth.
- Do not assume ordinary random UUID collisions explain duplicates at modest traffic; investigate generation and request-handling bugs.
- JVM/virtual-thread behavior is JDK-version-dependent. Verify current primary Java documentation when implementing or explaining it; these LinkedIn snippets are not the specification.

## Ishan sent-message resource review

Rechecked on 2026-10-21 using the supplied [Ishan conversation](https://www.linkedin.com/messaging/thread/2-OTkxM2ExNTEtMTBkNy00ODI4LThkNjQtZmI0MDA2MDJlNjUxXzEwMA==/?filter=inmail). Reached the conversation-start profile card, February 24, 2025, and the latest visible message dated March 18, 2026. The loaded history contained 54 message-event rows, with sender grouping identifying the user's outgoing messages, and 35 distinct directly shared non-profile destinations. This recheck did not reveal a later visible date range than the original review.

Much of the material was already represented. The changes fill omissions and broaden reference coverage; they do not establish new practice progress. No raw personal-message transcript or shared personal profiles are copied into the KB.

| ID | Resource actually checked | Integration |
|---|---|---|
| IM-DSA | [Neha: selected DSA questions](https://www.linkedin.com/posts/neha-jain-279b80118_%F0%9D%97%97%F0%9D%97%A6%F0%9D%97%94-%F0%9D%97%B6%F0%9D%98%80%F0%9D%97%BB%F0%9D%98%81-%F0%9D%97%AE%F0%9D%97%AF%F0%9D%97%BC%F0%9D%98%82%F0%9D%98%81-%F0%9D%98%80%F0%9D%97%BC%F0%9D%97%B9%F0%9D%98%83%F0%9D%97%B6%F0%9D%97%BB-activity-7380805140456361984-BLKN) | Longest Subarray with Sum K is distinct from counting such subarrays; 3Sum Closest, Invert/Symmetric Tree and Climbing Stairs were omissions from the first bank. |
| IM-LISTS | [Shivam: linked-list patterns](https://www.linkedin.com/posts/shivam-shrivastava-ab6404179_most-people-underestimate-linked-lists-don-activity-7340220511957766144-3uGa) | Middle, pair swaps, merging two lists, stable partitioning and designing a list. Repeated prompts merged. Competitive-programming variants were not automatically made core drills. |
| IM-PATTERNS | [Tauseef: coding patterns](https://www.linkedin.com/posts/tauseeffayyaz_coding-patterns-for-maang-interviews-activity-7335633860086456322-tz7l), [Rajat: pattern resources](https://www.linkedin.com/posts/rajatgajbhiye_dsa-a-day-can-keep-interview-rejections-away-activity-7329851275108397056-I8L3), [Ashish: templates](https://www.linkedin.com/posts/ashishmisal_dsa-datastructures-algorithms-activity-7354701390570545155-VNSG) | Bit/XOR and knapsack coverage, plus links for windows, graph traversal and backtracking. Template references are not a substitute for understanding the invariant. |
| IM-JAVA | [Aman: Java revision resources](https://www.linkedin.com/posts/sahniaman_java-springboot-systemdesign-activity-7331702226601570304-piKZ) | Generics, multithreading and Streams. The generic collection-copy exercise is our adaptation, not a verbatim problem in the post. Videos were not treated as watched. |
| IM-LLD | [Rajat: LLD roadmap](https://www.linkedin.com/posts/rajatgajbhiye_i-screwed-up-3-interviews-because-i-didn-activity-7334579228966830081-kCxt), [LLD examples](https://www.linkedin.com/posts/rajatgajbhiye_never-bet-against-system-design-trust-me-activity-7354359752459436032-Nr0a) | Chess validator, Tic-Tac-Toe and explicit LLD repository references. |
| IM-REPO1 | [kumaransg/LLD](https://github.com/kumaransg/LLD) | README/problem-directory review: event calendar, ride sharing, stock exchange, cricket dashboard and other machine-coding statements. Employer labels are unverified anecdotes. |
| IM-REPO2 | [InterviewReady/Low-Level-Design](https://github.com/InterviewReady/Low-Level-Design) | README/root inventory: Java inventory system, cache, event bus, limiter, service orchestrator and load-balancer examples. Implementations were not audited. |
| IM-SOLUTIONS | [LLD primer solution index](https://github.com/prasadgujar/low-level-design-primer/blob/master/solutions.md) | Actual solution table: games, configuration subscriptions, in-memory database/indexing, queue and booking examples. |
| LC150 | [LeetCode Top Interview 150](https://leetcode.com/studyplan/top-interview-150/) | Loaded the actual 150-entry list and its editorial links. Used the problem inventory to fill gaps; did not read all editorials. |
| NC75 | [NeetCode Blind 75](https://neetcode.io/practice?tab=blind75) | Selected Blind 75 through the current UI and expanded all 18 categories: 75 problem rows and their problem/solution links. The old query link initially opened Core Skills; the list picker reached Blind 75. |
| NC-DIR | [NeetCode solution directory](https://neetcode.io/solutions/course-schedule) | Scrolled the searchable left sidebar to its end and extracted 798 titled entries across 19 source categories. Excluded its 30 JavaScript-specific entries, leaving 768 algorithm entries as a broader reference inventory. |
| — | [Tharun's shared resource post](https://www.linkedin.com/posts/polu-tharun-kumar-reddy_faang-bigtech-techinterviews-activity-7351253048737890308-wuUl) | Resolved the three requested links. The handbook goes to [a Topmate product page](https://topmate.io/tharun_polu/1632874); its public description was read, not the handbook contents. No purchase or download claim. |

The LLD short links in the roadmap resolved to the LLD primer, its questions/solutions files, kumaransg/LLD and InterviewReady/Low-Level-Design. Those original GitHub destinations are retained above. No repositories were executed.

Representative explanation checks before the user asked to defer solution study:

- [Course Schedule](https://neetcode.io/solutions/course-schedule): DFS cycle detection and Kahn's topological sort, complexity sections and pitfalls. The user explicitly endorsed this reference.
- [Minimum Window Substring](https://neetcode.io/solutions/minimum-window-substring): brute-force/sliding-window structure, Java code selector and pitfalls.
- [Time Based Key-Value Store](https://neetcode.io/solutions/time-based-key-value-store): floor lookup at a historical timestamp; distinguish this contract from TTL expiry.
- The Shortest Subarray with Sum at Least K destination was opened; only its available description was seen. It was selected from the problem index, not treated as a fully reviewed solution.

After the user's clarification, the broader sidebar work used **problem names, source difficulty labels, categories and links**, not solution-by-solution study. Python-, JavaScript- and other language-specific exercises are out of scope; language-neutral DSA problems are retained for Java implementation. A page displaying Python by default does not make its algorithm Python-specific.

<a id="broader-neetcode-problem-directory"></a>

## Broader NeetCode problem directory

Reference inventory as rendered on 2026-10-21. Basic / Intermediate / Complex below map to the source's Easy / Medium / Hard labels. These are **source-index counts**, not 768 additional assignments or a claim that all 768 solutions were read. There is substantial overlap with the main bank, Blind 75 and Top Interview 150.

Use the searchable sidebar on the linked [directory page](https://neetcode.io/solutions/course-schedule) to choose additional names. Keep actual solution study until the relevant attempt. The main bank is an expandable selection, not a cap of 100.

| Source topic | Basic | Intermediate | Complex | Total |
|---|---:|---:|---:|---:|
| Arrays & Hashing | 63 | 60 | 5 | 128 |
| Two Pointers | 17 | 18 | 1 | 36 |
| Sliding Window | 5 | 26 | 5 | 36 |
| Stack | 9 | 18 | 6 | 33 |
| Binary Search | 6 | 21 | 4 | 31 |
| Linked List | 8 | 23 | 3 | 34 |
| Trees | 18 | 48 | 3 | 69 |
| Heap / Priority Queue | 4 | 15 | 8 | 27 |
| Backtracking | 1 | 25 | 4 | 30 |
| Tries | 2 | 4 | 3 | 9 |
| Graphs | 3 | 43 | 8 | 54 |
| Advanced Graphs | 0 | 9 | 19 | 28 |
| 1-D Dynamic Programming | 3 | 31 | 11 | 45 |
| 2-D Dynamic Programming | 0 | 26 | 21 | 47 |
| Greedy | 7 | 48 | 7 | 62 |
| Intervals | 1 | 12 | 3 | 16 |
| Math & Geometry | 21 | 29 | 4 | 54 |
| Bit Manipulation | 11 | 18 | 0 | 29 |
| **Total** | **179** | **474** | **115** | **768** |

The separate JavaScript category (30 entries) was excluded. Source topic boundaries differ from our bank: graph categories and tree/trie categories are consolidated; practical Java schedulers, caches and LLD are separate. User-selected priorities determine practice, not source category counts.



## Local Grokking sequence review — 2026-10-25

Source: `D:\one-drive\OneDrive\AbhishekMittal\Coding\Grokking the Coding Interview by educative.io (z-lib.org)\Educative.io - Grokking the Coding Interview - Patterns for Coding Questions`.
Reviewed all immediate chapter/lesson directories and selected HTML exports'
embedded course navigation. Found 16 pattern chapters (folders 2–17), with
introduction, miscellaneous and conclusion folders outside that count. Full
solution implementations and unnamed challenge bodies were not exhaustively read.
Multiple HTML exports and solution-review folders are not extra unique problems.

Learner selected numeric chapter/lesson order as the default. See
[the learning plan](../learning-plan.md#default-dsa-sequence--2026-10-25) for the
order, current exercise and explanations required before departing. This review
records source organization and a preference, not code validation or mastery.
