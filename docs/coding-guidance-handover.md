# Coding Practice Handover
Prepared for Abhishek Mittal | 10 October 2026 | PE – Q and A

## Active session — Atlassian diagnostics, 31 October 2026

Current task: **Group Anagrams**. Learner requested the next problem after the
all-pairs explanation; present the contract and await approach/code. This is a
revisit because prior reference/demo material exists, not an unseen diagnostic.
Do not infer independent mastery from agreement with the all-pairs explanation.

Latest result: learner supplied the single-pair HashMap solution after a complement
hint and reported passing tests. Review found it correct for the exercise bounds;
assistance is hint-assisted and time unmeasured. Critique must stay in chat unless
file edits are explicitly requested. On request, supplied the separate all-index-pairs
class `arrays.ref.TwoSumUnSortedAllPairs`, compiled with Java 21 and passed 11 sample
checks (duplicates, no match, empty/singleton, overflow and input preservation).
All-pairs is demonstrated, not an independent attempt. Original code was unchanged.
These results supersede the pending-implementation descriptions below.

Learner selected a 60-minute DSA diagnostic in Java, then a separate 60-minute
Code Design diagnostic, to inform a four-week Atlassian preparation window.
Start with HashMap, sorting and interval processing; one problem at a time.
Latest refinement: **Two Sum** is the first diagnostic; await independent approach
and Java implementation. Review correctness, Java fluency, edge cases and complexity.
Provisional progression: Group Anagrams → Merge Intervals → Total Busy Time per
Employee, with the combined meeting task deferred until later in Week 1 if ready.
Increase difficulty based on performance; no solutions or next task without the
learner's request. No timer has started and no independent completion is established.
Continue Two Sum rather than the older
fixed-window starting point; retain all prior attempts and the general DSA sequence.
Do not present the Code Design task until the learner requests continuation.

Two Sum support supplied: expected-time comparison of sorting versus hashing and
a complement/map hint. On request, added `main` and eight sample checks in
`arrays.ref.TwoSumUnSorted`, leaving the algorithm unimplemented. Java 21 compile
and launch succeeded; output correctly reports implementation pending. No sample
pass or independent solve is claimed. Record the approach attempt as hint-assisted
if it uses the supplied hint; timing remains unmeasured.

## Current workspace context — 21 October 2026

Current sequence preference — 25 October 2026 (latest user override): arrays →
queues → priority queues/heaps → graphs, explicitly covering general-graph BFS
and DFS before dependency ordering/topological sort. This overrides strict Grokking
chapter order and the historical graph-first queue. Grokking remains the primary
reference and supplies lesson order within compatible selected patterns. Source path and
full ordered chapter list are in `learning-plan.md` under Default DSA sequence.
Explain the concrete prerequisite or interview-driven reason before proposing any
departure; do not silently reorder. Existing topic bank and attempt history remain.
Maximum Sum Subarray of Size K was presented on 22 October; no attempt is recorded.
Folder/lesson inventory and selected HTML navigation reviewed, not all solutions.

Use `learning-plan.md` for current preparation preferences: flexible six weeks,
learner-selected problems, no imposed daily/weekly allocation. This supersedes
historical four-week scheduling passages below. Preserve the practice protocol,
attempt log; use the latest sequence above for next-problem selection. Coding/DSA bank: `interview-kb/README.md`;
workflow: `$java-coding-prep`. Practical component study has its own direct-solution
mode and context in `C:/Users/abhis/IdeaProjects/Test`. System-design problems and
learning have moved to `D:/java-projects/docs/interview-kb/README.md`.
This context update records no new practice, completion or mastery.

## 1. Purpose and scope

This note enables another model to continue coding preparation without restarting the session. It records the interview requirements, learning preferences, observed progress, category sequence, and practice method.

This document intentionally contains no actual coding problem statements, sample inputs, worked solutions, or implementations. Categories are preparation recommendations, not a prediction of Okta's questions.

## 2. Recruiter guidance: confirmed requirements

Source: the guidance pasted by Abhishek from Manasa Kola, Senior Recruiter, Okta. The email's original date was not supplied.

Round 2 is titled **Programming Ability & Collaboration** and lasts approximately 60 minutes. It involves a live coding challenge: designing, coding, and solving a medium–hard problem, in the style of, but not copied from, a typical LeetCode medium.

The recruiter identifies these evaluation criteria:

- Solve a medium-difficulty programming challenge through clear reasoning.
- Improve the solution's efficiency, or brainstorm how to improve it.
- Demonstrate a clear problem-solving approach and communicate while coding.
- Demonstrate collaboration and engagement.

No specific algorithms, question bank, Java version, coding environment, or language restrictions were confirmed. Do not represent recommended categories as recruiter-confirmed topics.

Round 3 is a separate system design interview. Keep this practice focused on implementation, algorithmic correctness, complexity, and collaboration. Discuss architecture only when it is relevant to the coding contract or an explicit follow-up.

## 3. Candidate context

Abhishek is an experienced Principal/Staff-level backend engineer with Java and distributed systems experience. Use Java as the default practice language, based on his background and the language used in this session.

The immediate practice objective is to convert an understandable approach into correct, readable code with reliable boundary handling. Do not assume architectural experience establishes current fluency in every algorithm category. Equally, do not infer broad weakness from a request for an explanation.

Explain algorithm mechanisms in plain, precise language. Familiar enterprise terminology can provide context, but should not complicate the underlying algorithm.

### Project-wide preparation update — 18 October 2026

The candidate's priorities extend beyond this Okta round: Java coding interviews at
Staff / Senior Staff / Principal level, plus Java language depth in concurrency,
the JVM, performance, and modern Java. Use a four-week preparation horizon with
roughly **2 hours/day for Java coding + concurrency** as the primary block.

Bias exercise selection toward algorithms combined with practical Java engineering:
thread-safe components, rate limiters, ID generators, caches, queues, executors,
synchronization, and performance-sensitive implementation. Retain core algorithm
coverage and the next queued graph traversal / dependency-ordering exercise.

System design / distributed systems is a parallel secondary track, emphasizing IAM,
gateways, Kafka, and gRPC. GraphQL is limited to architecture discussion; Go / Rust
is deferred unless a specific role requires it. These are candidate preferences,
not additional recruiter-confirmed requirements. Preserve the practice modes and
assistance tracking below; no new attempt or mastery is implied by this update.

### Company targeting update — 18 October 2026

Primary targets are **Atlassian, Google, Meta, and comparable coding interviews**;
secondary targets are **Adobe, Intuit, Target, Visa, Amazon**. These groups express
candidate priorities, not difficulty tiers. Use the company-focus section in
`learning-plan.md` for the four-week schedule and source boundaries.

Within the two-hour daily block, allocate roughly 75 minutes to algorithms,
30 to Java code design / concurrency, and 15 to retrieval or targeted Java depth.
Keep graph traversal / dependency ordering next; bring trees and binary search
forward, and cover basic DP/backtracking within the month. Preserve the existing
Okta protocol when practicing for Okta. For other sessions, name the selected
target or use a shared coding round; do not automatically introduce it as Okta.
Company-specific drills are training recommendations unless recruiter guidance
confirms the format. No new attempt or mastery is recorded by this planning update.

## 4. Explicit learning preferences

Repository update (2026-10-19): use `C:\Users\abhis\hello` as the active IDE
workspace and `savi0909/practical-java` as its publishing destination. Prefer
separate modules, such as Rate Limiter Lab, for performance-testing runs and
benchmarks, keeping workloads, configuration, instructions, and results together.

The following preferences were expressed in this session:

- Present one new problem at a time.
- Initially provide the statement, constraints, examples, and Java signature.
- Do not show how the solution works unless asked.
- Allow Abhishek to describe his approach before giving feedback.
- When he asks for documentation, provide simple explanation and code.
- When he asks for implementation, provide readable Java directly.
- Clarify notation rather than assuming variables are understood.
- Avoid dumping many problems or solutions into one response.

For future practice, honor the requested mode:

| Mode | Model behavior |
|---|---|
| Practice | Present one task; wait for the approach; do not reveal the solution. |
| Guided reasoning | Correct the specific gap with a small hint or explanation. |
| Implementation request | Provide a simple Java implementation, then essential complexity details. |
| Explanation request | Explain the invariant, state changes, and boundary handling. |
| Mock interview | Preserve independent reasoning; review after the attempt. |

Do not withhold requested code to force an exercise. After explaining, independent implementation can be suggested, but it must not replace the user's requested help.

## 5. Observed progress and limits

These observations describe the visible session. They do not establish independent implementation mastery.

| Category | Observed reasoning | Clarification provided | Remaining evidence needed |
|---|---|---|---|
| Variable sliding window with a set | Recognized the sliding-window pattern and expansion until a duplicate. | Shrinking may require multiple removals; maintain a uniqueness invariant; track indexes without rebuilding substrings. | Independently implement and explain boundary movement and amortized complexity. |
| Interval processing after sorting | Asked for help with the approach and subsequently requested Java code. | Sort by the relevant boundary, keep current state, distinguish overlap from a gap, and save final state. | Independently derive and implement a related interval pattern. |
| Frequency aggregation and ranking | Proposed a map of counts followed by descending sorting and selecting a bounded result. | Distinguished total occurrences from distinct keys and separated counting cost from sorting cost. | Independently implement the counting and comparator logic. |
| Bounded heap selection | Proposed filling the heap, then comparing subsequent candidates before replacing retained entries. | Compare using non-destructive inspection; remove only when replacement is justified; rejected candidates require no heap mutation. | Independently implement and verify heap direction, capacity, and tie behavior. |

Assistant-provided implementations were shown for the categories above. No candidate-authored code, compilation result, timed completion, or independently verified implementation was recorded.

A useful positive observation: Abhishek recognized that unconditional insertion followed by removal can be replaced by comparison followed by conditional replacement. Preserve this optimization reasoning in subsequent heap practice.

## 6. Technical concepts to reinforce

### Invariants and state

Ask the candidate to state what must remain true after each iteration. Connect the invariant to the data structure and update order. Avoid explaining a technique solely through pattern names.

Useful state questions include: what is retained, what can be discarded, when a result becomes final, and whether inspecting state changes it.

### Complexity notation

Define variables before using them:

- n: total input elements or occurrences.
- m: distinct keys or aggregated entries, when relevant.
- k: retained result size or selection bound.
- V and E: vertices and edges, when graph categories begin.

Distinguish input size, intermediate state, auxiliary space, and returned output. Avoid using n and m interchangeably.

For nested loops, count total movements or operations rather than concluding quadratic time from syntax. For heaps, distinguish constant-time root inspection from logarithmic insertion/removal. For bounded heaps, explain that rejected candidates can be inspected without mutation.

### Java collection behavior

Reinforce these through relevant practice rather than an isolated API lecture:

- HashMap and HashSet operations have expected constant-time behavior under normal hashing assumptions.
- PriorityQueue root direction is determined by its comparator; it does not provide globally sorted iteration.
- peek inspects; poll removes; offer inserts.
- Use Integer.compare or Long.compare instead of subtraction-based comparators.
- HashMap iteration order is not a ranking guarantee.
- Updating priorities in-place inside a heap can violate ordering; treat retained priorities as stable unless explicitly managed.
- Declare whether sorting or other operations mutate the supplied input.
- Use long when constraints can overflow integer arithmetic.
- State the character model for string tasks; Java char represents a UTF-16 code unit.

Discuss these details only when they affect correctness or the current task.

## 7. Category roadmap

The ordering below is a suggested preparation sequence. Adjust using observed performance, not assumptions about the employer's question distribution.

| Priority | Category | Skills to assess |
|---|---|---|
| Revisit | Sliding windows and two pointers | Window invariants, boundary updates, amortized analysis, fixed versus variable windows. |
| Revisit | Sorting and intervals | Comparator correctness, overlap semantics, final-state handling, mutation. |
| Revisit | Hash-based aggregation and bounded heaps | Counting, distinct cardinality, heap direction, conditional replacement, ties. |
| Next | Graph traversal and dependency ordering | Adjacency representation, indegrees, BFS/DFS, cycle detection, disconnected components. |
| Next | Composite data structures | Coordinating indexes and ordering structures, operation invariants, update/removal consistency. |
| Core | Binary search | Search boundaries, termination, monotonic conditions, off-by-one errors. |
| Core | Stacks and queues | State tracking, processing order, appropriate use of ArrayDeque. |
| Core | Trees | Recursive and iterative traversal, traversal state, depth and breadth trade-offs. |
| Core | Prefix accumulation | Incremental aggregates, lookup-based reasoning, numeric bounds. |
| Later | Greedy methods | Local decisions, ordering assumptions, justification and counterexamples. |
| Later | Basic dynamic programming | State definitions, transitions, base cases, memoization versus tabulation. |
| Later | Backtracking | Choice generation, state restoration, pruning, exponential search cost. |

Do not introduce advanced topics merely to make practice appear comprehensive. Increase difficulty after the candidate can implement a category with limited help.

## 8. Recommended practice interaction

For each new exercise:

1. Present one unseen medium-level task with a precise contract.
2. Ask for the simplest correct approach, data structures, and complexity.
3. Identify what is correct before correcting a specific gap.
4. Use a targeted hint if requested or if the candidate is stuck.
5. Ask for implementation when the approach is settled.
6. Review correctness, boundary conditions, readability, and complexity.
7. Add one follow-up only after the base solution is understood.
8. Record assistance level and the next skill to revisit.

If the user asks for code before implementing, switch to demonstration mode. Record that the solution was demonstrated, not independently completed. On a later visit, use a different task within the category to assess transfer.

Do not label a topic mastered solely because the candidate agrees with an explanation.

## 9. Sixty-minute mock structure

This is a practice allocation, not a confirmed Okta timetable.

| Minutes | Focus |
|---|---|
| 0–5 | Contract clarification, constraints, boundary cases. |
| 5–15 | Correct initial approach, invariant, complexity, possible improvement. |
| 15–40 | Implementation with concise explanation of important choices. |
| 40–50 | Testing, manual tracing, correction of errors. |
| 50–60 | Optimization or one follow-up; final complexity explanation. |

Preserve time to validate code. Narration should explain decisions and state changes rather than every line of syntax.

When the solution is unclear, prefer establishing a correct baseline before attempting an optimization. When the baseline is clearly unsuitable for the stated constraints, explain the limiting cost and then derive an alternative.

## 10. Review criteria and progress tracking

Use a small qualitative record after each attempt:

| Dimension | Evidence to record |
|---|---|
| Contract | Clarified meaningful ambiguity; respected assumptions. |
| Reasoning | Derived a correct approach and articulated its invariant. |
| Implementation | Produced readable, consistent code without substantial assistance. |
| Validation | Selected relevant edge cases and detected incorrect behavior. |
| Efficiency | Explained costs using defined variables and justified an improvement. |
| Collaboration | Responded to feedback, considered alternatives, explained changes. |

Track assistance as **independent**, **hint-assisted**, **guided**, or **demonstrated**. Track elapsed time only if actually measured. Record concrete errors rather than assigning unsupported scores.

General validation dimensions include empty or smallest permitted input, repeated values, ties, boundary equality, input order, capacity extremes, disconnected structure, cycles where relevant, and arithmetic limits. Select only cases applicable to the contract.

## 11. Next-session instructions

The latest substantive topic was bounded heap selection. The candidate proposed the optimization of comparing against the retained minimum before replacing it; the assistant clarified inspection versus removal and showed a short implementation fragment.

Current override (2026-10-25): follow the Grokking sequence in `learning-plan.md`; resume Maximum Sum Subarray of Size K unless the learner selects otherwise. The former graph-first queue is historical. Present one exercise without revealing the algorithm or implementation. If he asks to revisit heaps, continue from conditional replacement rather than repeating the entire counting explanation.

Begin with a brief statement that practice is continuing for Okta Round 2. Do not repeat this whole handover or begin with a broad syllabus.

Suggested instruction to the next model:

> Continue Abhishek's Okta Round 2 coding preparation in Java. Use this handover as the current practice state. Present one unseen exercise at a time and let him explain his approach. Do not reveal solutions unless he asks. Provide simple Java code and precise explanations when requested. Define complexity variables. Distinguish demonstrated understanding from independent coding. Follow the latest Grokking chapter/lesson sequence in learning-plan.md instead of the historical graph-first queue.

## 12. Source boundary

Recruiter requirements and candidate preferences above come from the visible conversation. The roadmap, mock schedule, review rubric, and readiness evidence are preparation recommendations. They are not claims about Okta's internal interview rubric or actual questions.


## 13. Practice log

Append one row per attempt. Assistance: independent / hint-assisted / guided / demonstrated.

| Date | Category | Task (one line) | Assistance | Concrete errors / gaps | Next to revisit |
|---|---|---|---|---|---|
| 2026-10-31 | Hashing/grouping revisit | Group Anagrams | — (presented, awaiting approach/code; prior demo exists) | Not assessed; time unmeasured | Review key choice, grouping and complexity |
| 2026-10-31 | Hashing diagnostic | Two Sum | hint-assisted | Correct submitted code; learner reports samples pass; time unmeasured. Discussed single lookup, initialization and fallback. | Blind independent revisit |
| 2026-10-31 | Hashing follow-up | Two Sum: all index pairs | demonstrated | Assistant implementation requested; Java 21 compile and 11 samples passed; no learner attempt | Explain storing multiple indices and output-sensitive complexity |
| 2026-10-31 | Atlassian DSA diagnostic: hashing, sorting, intervals | Total busy duration per employee; original adaptation | — (presented, awaiting approach/code; timing not started) | Not assessed | Review approach, implementation and tests |
| 2026-10-10 | Sliding window revisit | Longest substring without repeating characters; scaffold `dsa.slidingwindow.LongestSubstringWithoutRepeating` | — (scaffold and sample checks only; awaiting approach) | Implementation not attempted yet | Review approach, then implement |
| 2026-10-10 | Sliding window | Longest substring without repeating characters (easy) | unrecorded (self-reported done) | — | Blind re-solve of a related variant |
| 2026-10-10 | Sorting and intervals | Merge overlapping intervals (easy) | unrecorded (self-reported done) | — | Related interval pattern (insert / min removals) |
| 2026-10-10 | Sliding window (extremum invariant) | Longest subarray with bounded spread (medium); scaffold `dsa.slidingwindow.LongestBoundedSpreadSubarray` | — (presented, awaiting approach; implementing in IDE) | — | — |
| 2026-10-10 | Graph traversal / dependency ordering | Minimum rounds to finish tasks with prerequisites (-1 on cycle); scaffold `dsa.graph.MinRoundsToFinishTasks` | — (presented, awaiting approach; implementing in IDE) | — | — |
| 2026-10-10 | Sorting and intervals revisit | Merge overlapping intervals; scaffold `dsa.arrays.MergeOverlappingIntervals` | — (scaffold and sample checks only; awaiting approach) | Implementation not attempted yet | Review approach, then implement |
