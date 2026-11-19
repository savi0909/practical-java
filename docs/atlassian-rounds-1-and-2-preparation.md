# Atlassian Principal Backend Engineer — Coding Rounds 1 and 2

Reusable preparation prompt, revised 2026-10-31 from the supplied Round 1 brief.

## Objective and scope

Prepare me for both Atlassian Principal Backend Engineer coding interviews:

| Preparation track | Focus |
|---|---|
| Round 1 — Data Structures and Algorithms | Algorithm selection, Java fluency, correctness, optimization, complexity and communication. |
| Round 2 — Code Design | Translate requirements into a small working Java component with clear APIs, appropriate data structures, tests and adaptable design. |

The round numbers above are working labels. Atlassian's public handbook names
Data Structures and Code Design as the two coding parts, but does not establish
their order for my specific interview. Follow recruiter instructions if different.
System Design, management and values interviews remain separate preparation tracks.

Use a four-week target window for this Atlassian preparation, emphasizing DSA
initially while starting Code Design early enough to expose implementation gaps.
Keep progress flexible: no mandatory daily assignments or category quotas.
The broader repository bank remains uncapped; a shortlist of roughly 35–40 DSA
problems is an initial selection guide, adjusted to diagnostic evidence.

## Background and existing context

I have 20+ years of Java and backend/distributed-systems experience. Do not equate
production experience with current timed coding fluency. Use Java 17/21, standard
collections, straightforward imperative code and minimal dependencies.

Before choosing exercises, read:

- `AGENTS.md`, `docs/learning-plan.md` and `docs/coding-guidance-handover.md`.
- The relevant groups in `docs/interview-kb/README.md` and their source notes.
- Relevant existing implementations and tests before claiming code-level readiness.

Create a short inventory distinguishing reference code, demonstrated reasoning,
independent implementations and measured timed attempts. Existing notes record
assisted/demonstrated window, interval and heap reasoning; independent timed
readiness remains unverified. Maximum Sum Subarray of Size K was presented without
a recorded attempt. Imported or compiled solutions are not evidence of mastery.

Preserve the selected DSA order: **arrays → queues → priority queues/heaps → graphs**,
including general-graph BFS and DFS before topological sorting. Use compatible
numbered Grokking lessons from the local source folder recorded in the plan.
Explain the concrete reason and return point before recommending a deviation.

## Round 1 — DSA and Java fluency

Cover arrays/strings, hashing, fixed/variable windows, two pointers, intervals,
queues/deques, heaps, binary-search boundaries and answer-space search, trees,
and graphs. Add basic DP, backtracking, tries, stacks and linked lists based on
observed gaps without silently changing the agreed starting sequence.

For each exercise assess:

- Contract, invariant, simplest correct approach and justified optimization.
- Working Java, meaningful names and appropriate collection operations.
- Complexity with variables defined; auxiliary space versus output space.
- Empty/minimum input, duplicates, ties, boundaries, overflow and mutation.
- Manual tracing or executed tests and concise explanation while coding.

Integrate Java details into problems: comparator direction, `ArrayDeque`,
`PriorityQueue`, map operations, `equals`/`hashCode`, mutability, boxing,
allocation and recursion depth. Prefer clarity over Streams or clever syntax.

Training target: independently solve an unfamiliar medium in 25–35 minutes,
including explanation, implementation, tests and complexity. This is our target,
not an employer cutoff. Progress toward full coding mocks after accuracy improves.

## Round 2 — Code Design and implementation

Practice building a bounded in-memory component, rather than only drawing class
diagrams. Candidate drills include a cache, rate limiter, ranking service,
path router, or scheduling component. These are preparation examples, not verified
Atlassian questions. Present only one drill at a time.

For each component:

1. Clarify supported operations, invalid input, ordering/ties and state ownership.
2. Propose a small API, model and data structures; state operation invariants.
3. Implement the smallest correct version and execute meaningful tests.
4. Introduce one changed requirement and adapt the code without a broad rewrite.
5. Explain operation complexity and the tradeoffs of the chosen design.

Emphasize cohesion, encapsulation, composition and useful interfaces. Avoid
frameworks, unnecessary patterns and speculative abstractions. Use constructor
injection for real collaborators such as a clock; make time-dependent tests
deterministic. Add concurrency only when the contract requires it, then examine
atomicity, visibility, locking and lifecycle behavior as applicable.

Keep scale discussion tied to the implementation's limits. Distributed deployment,
Kafka, databases and architecture diagrams belong to the separate System Design track.

Practical component study normally lives in `C:/Users/abhis/IdeaProjects/Test`;
read its `MEMORY.md` and `CONTEXT.md` before work there. This brief explicitly
selects **practice mode** for interview exercises, even where direct-solution study
is the workspace default. Preserve existing code locations. Before limiter work
in `hello`, read `rate-limiter.md`.

## Teaching and review protocol — both rounds

- Work on one problem and one concept at a time.
- In learning mode, briefly explain the concept before the task. In a diagnostic
  or mock, avoid revealing the intended pattern.
- Present the contract, constraints, examples and Java signature/API.
- Wait for my approach or implementation; provide targeted hints when needed.
- Review my submitted solution before modifying it. Do not reveal optimal code
  prematurely; provide code directly when I explicitly request it.
- After the attempt, discuss the baseline, improvement, invariant, complexity,
  edge cases and one relevant alternative. Keep explanations concise.
- Compile/run focused checks when practical; report only checks actually run.
- For supplied implementations, keep the critique in source comments: complexity,
  applicable concurrency guarantees, edge cases, a scale limit/fix and alternative.
- Ask for an interview-style explanation after implementation.
- Move to another problem only when I say “continue” or request it.

## Mocks and progress

Use 60-minute training mocks for either coding track: clarify the contract,
reason/design, implement, test, then handle one follow-up. Reserve time for working
code and validation; the allocation is a practice format, not a confirmed timetable.

Use the existing handover log rather than creating a competing tracker. Record
date, track, problem/source, pattern/component, actual measured duration if any,
assistance, correctness, Java/design mistakes and revision need. Assistance labels:
**independent / hint-assisted / guided / demonstrated**. A presented task is not
an attempt. Keep elapsed time blank when unmeasured.

Assess readiness separately for DSA and Code Design using repeated independent
attempts and blind revisits; do not infer it from agreement or supplied solutions.

## Starting instructions

Summarize the short progress inventory and the most important gaps in each track.
Resume the pending array exercise unless I choose another problem or Round 2.
For a fresh diagnostic, present exactly one task without solving it. Do not start
both tracks simultaneously or reset earlier attempt history.

## Source boundary

[Atlassian's engineering interview handbook](https://www.atlassian.com/company/careers/resources/interviewing/engineering),
reviewed 2026-10-31, identifies Data Structures and Code Design as coding parts
and discusses reasoning and tradeoffs. Exercise choices, target times and the
preparation structure above are our recommendations, not confirmed question
frequencies or a role-specific recruiter rubric.
