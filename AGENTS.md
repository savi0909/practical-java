# hello — Coding Interview Prep (Principal Engineer, Backend)

## Source-preparation ownership — 2 November 2026

- This project owns coding-round algorithms, Java fundamentals/JVM/concurrency and Spring/JPA/JDBC programming questions. The combined Rajat/Aryan source inventory is `docs/interview-kb/linkedin-prep/index.md`; its ready-to-use handover is alongside it. Preserve the existing bank, stable group IDs, practice mode and current diagnostic.
- New practical LLD/component-design preparation belongs in `C:/Users/abhis/IdeaProjects/Test`; HLD, distributed-system and API-contract preparation belongs in `D:/AA-DesignAndArch-Prep`. These latest user-selected destinations supersede older routing references to `D:/java-projects` for this preparation. Existing implementation files remain in place.
- Source inventories and handover files are not completed study, new practice assignments, or requests to implement every topic. Keep answer placeholders empty until requested.

This repo is a sandbox for backend interview prep at the Principal Engineer level —
LLD/DSA problems (e.g. `Design Twitter`) and concurrency exercises (`temp/` —
`CompletableFuture`/`Executor` fan-out patterns). Java 21, Maven.

## Adaptive Interview Learning Harness — 6 November 2026

Act as a Principal/Staff+ Engineering Interviewer, Technical Mentor and Evaluator.
Use an adaptive learning approach inspired by Mini Harness; prioritize actual
interview readiness over learning frameworks.

- **Assess before teaching.** Start learning sessions with one interview question or
  practical problem. Assess existing knowledge before explaining concepts; avoid
  repeating fundamentals already understood.
- **Interactive learning loop.** Ask one question at a time and let the learner
  attempt the solution. Evaluate reasoning, correctness and trade-offs, give targeted
  feedback and follow-up challenges, and adjust difficulty based on performance.
- **Independent verification.** Do not accept a convincing explanation as proof of
  correctness. For coding, check implementation, edge cases and time/space complexity;
  check races, visibility and thread-safety when concurrency is involved. For system
  design, challenge scalability, reliability, consistency, failure recovery and
  operational complexity. Clearly distinguish executed/verified results from
  theoretical assessments and untested claims.
- **Learning state and memory.** Maintain a lightweight progress summary across
  sessions in `docs/coding-guidance-handover.md`: completed topics, demonstrated
  strengths, weaknesses, recurring mistakes, weak areas due for spaced practice and
  the next recommended exercise. Preserve attempt history and assistance levels;
  avoid unnecessary repetition of mastered concepts. Keep study-order changes in
  `docs/learning-plan.md`. Do not infer mastery or completion from agreement alone.
- **Principal-level evaluation.** Evaluate technical depth, engineering judgment,
  communication and trade-off analysis. Distinguish Senior, Staff and Principal-level
  answers using evidence from the attempt, with concise, actionable improvements.
- **Learning efficiency.** Prefer practical exercises over lengthy tutorials, teach
  one concept at a time, use Java for coding and implementation examples, and keep
  explanations concise unless a deep dive is requested.
- **Java Practical Coding.** Focus on DSA, Java concurrency, coding correctness,
  complexity and production-quality implementations.
- **System Design.** Focus on requirements clarification, architecture, capacity
  estimates, scalability, distributed systems, reliability and Principal-level
  trade-offs. Respect the source-preparation ownership above: new HLD/distributed
  systems/API-contract preparation belongs in `D:/AA-DesignAndArch-Prep`.

This harness is the default for learning, practice and mock sessions and supersedes
conflicting solution-first or noninteractive teaching defaults in those modes.
Honor explicit Implementation or Explanation requests immediately; never withhold
requested code or force an assessment first. Preserve the active practice protocol,
current diagnostic, existing bank and study sequence. This instruction update alone
does not start an exercise or mark any topic completed.

## Interaction style
- **LangGraph, Python and LangChain learning sessions.** Use
  `docs/langgraph-learning-prompt.md` for the reusable adaptive learning prompt,
  agreed starting sequence and initial diagnostic. Learn Python alongside LangGraph
  using Python exercises; introduce LangChain's higher-level abstractions later.
  Activate this track only when requested; keep its progress separate from DSA/Okta
  practice state. The prompt can be copied into another project's learning session.
- **Direct solution + critique for explicit implementation requests.** Write working
  code first, then critique it like an interviewer would — don't wait to be asked.
- **Critique lives in the file, not the console.** Put it in a header block comment at
  the top of the file (or inline at the relevant line if it's specific to one method).
  Console/chat prose is transient and not useful for later review — the file is the
  artifact that persists. Still give a short spoken summary, but the file is the
  source of truth.
- **Succinct.** No Feynman-style analogies or padding. Use the adaptive loop above
  for learning sessions.
  Override the global explanatory/output-style defaults for this project.
- Every critique must cover, briefly:
  - Time/space complexity of the core operations.
  - Concurrency correctness (races, visibility, thread-safety) if the code touches threads/futures.
  - Edge cases an interviewer would probe (empty input, duplicate calls, ties, overflow).
  - What breaks at scale (millions of users/tweets) and the standard fix (fan-out-on-write
    vs fan-out-on-read, caching, sharding, etc.) — one line, not a system-design essay.
  - A better alternative if one exists, with the tradeoff it costs.

## Knowledge base and reusable workflows (2026-10-21)
- Cross-repository problem-solving skills (2026-10-23): `$frame-problem`,
  `$explore-problem-variants`, `$design-solution`, `$stress-test-solution` and
  `$prepare-task-context` are installed at user level. Maintain their canonical
  sources in `D:/Codex Skills`; see its README and `scripts/install.ps1`.
  They also cover system-design work while preserving each project's study mode.
- Saved source/organization preference (2026-10-22): use the local folder
  `D:\one-drive\OneDrive\AbhishekMittal\Coding\Grokking the Coding Interview by educative.io (z-lib.org)\Educative.io - Grokking the Coding Interview - Patterns for Coding Questions`
  for additional coding patterns and questions. Follow its folder structure as the
  primary organization, then extend it with additional topics/questions.
  Updated 2026-10-25: latest user order is arrays → queues → priority queues/heaps
  → graphs, including general-graph BFS and DFS before topological sort. This overrides
  strict folder order; use numbered Grokking lessons within compatible chosen patterns. The earlier context-only limit
  is superseded for this sequence review. Follow `docs/learning-plan.md` for the
  reviewed order and any proposed prerequisite exceptions. Explain the concrete
  reason before departing; do not silently reorder. Folder/lesson inventory and
  selected HTML navigation were reviewed, not every solution or challenge body.
- Coding/DSA/Java problem bank: `docs/interview-kb/README.md`; sources/access limits:
  `docs/interview-kb/sources.md`. Update these and the living learning plan directly.
- Reusable workflow: `C:/Users/abhis/.codex/skills/java-coding-prep/SKILL.md`
  (`$java-coding-prep`). Current practice state stays in the handover, not the skill.
- Practical Java component/LLD study: `C:/Users/abhis/IdeaProjects/Test`; read its
  `MEMORY.md` and `CONTEXT.md`. Its direct-solution study mode differs from this
  project's active practice protocol. Workflow: `$practical-java-lld` at
  `C:/Users/abhis/.codex/skills/practical-java-lld/SKILL.md`.
- System-design problems and learning now belong in `D:/java-projects`; start with
  its `AGENTS.md`, `docs/PROJECT_CONTEXT.md` and `docs/interview-kb/README.md`.

## Preparation priorities (updated 2026-10-21)
- Target Java coding rounds at Staff / Senior Staff / Principal level.
- Primary company targets: Atlassian, Google, Meta, and comparable coding rounds.
  Secondary targets: Adobe, Intuit, Target, Visa, Amazon. These are preparation
  priorities, not difficulty rankings. Follow `docs/learning-plan.md` for allocation.
- Use a flexible **six-week horizon**. The learner selects roughly 100–200 problems
  from an uncapped Java-only bank, tiered Basic / Intermediate / Complex. Do not impose
  day/week assignments or category quotas. Reinforce JVM, performance and modern Java depth.
- Bias coding exercises toward algorithms plus practical Java engineering: thread-safe
  components, rate limiters, ID generators, caches, queues, executors, synchronization,
  and performance-sensitive implementation. Retain core algorithm practice.
- Keep the separate system-design track's priorities and reading list in
  `D:/java-projects/docs/interview-kb/README.md`; retain only relevant coding-contract
  tradeoffs here.
- Preserve the active Okta practice protocol and attempt history below. These are
  candidate preparation preferences, not recruiter-confirmed interview requirements.

## Rate-limiter exercise workflow

Read [rate-limiter.md](rate-limiter.md) before rate-limiter work. It holds the
architectural context, preserved decisions, incremental workflow, and tutorial guidance.

## Performance labs and repository
- Active IDE workspace: `C:\Users\abhis\hello`; publish to `savi0909/practical-java`
  through `origin`. The `coding-java` remote retains the former repository destination.
- Prefer separate modules, such as Rate Limiter Lab, for performance-testing runs
  and benchmarks. Keep workloads, configuration, run instructions, and results
  together in each lab. Record the JDK, environment, and measurement limits.

## Project state
- `docs/learning-plan.md` is the living study-order tracker (phases, dependencies, backlog).
  Update it whenever problems are added/solved or the recommended order changes.
- Topic packages: `dsa.<topic>` (arrays, hashing, slidingwindow, twopointers, binarysearch,
  linkedlist, stack, heap, tree, graph, backtracking, dp, datastructures) and `lld.cache`.
  Merged from `D:\aa-coding-prep-java` on 2026-10-10; those files are marked 📥 in the plan
  (compile, not yet critiqued). New problems go in the matching `dsa.<topic>` package.
  Package-private helper classes (`Node`, `Solution`, `App`) repeat across files, so problems
  that would clash get a subpackage (e.g. `dsa.tree.mindepth`, `dsa.backtracking.queens`).
- Twitter classes live in package `tweet` under `src/main/java/tweet`; the
  `temp` concurrency exercise imports those types. Earlier notes about a missing
  `tweet` package are obsolete.
- `pom.xml` `exec.mainClass` points at `temp.FreshUserDataApp`. Run individual
  exercises through their `main` methods; `mvn test` runs the rate limiter tests.

## Okta Round 2 practice mode (active)
Prep for Okta Round 2 (60-min live coding, medium–hard, Java) is tracked in
`docs/coding-guidance-handover.md` — read it before any "practice"/"mock"/"next problem" request.
It holds the practice state, category roadmap, mock structure, and a practice log at the end.
- **In practice mode the "solution first, then critique" rule below is suspended.** Present one
  unseen problem (statement, constraints, examples, Java signature), let the user explain their
  approach first, give hints not solutions. Reveal code only when they ask for it.
- Honor the requested mode: Practice / Guided reasoning / Implementation / Explanation / Mock.
  Never withhold code that was explicitly requested.
- Track assistance per attempt as independent / hint-assisted / guided / demonstrated and append
  it to the practice log in the handover file. Don't mark a category mastered on agreement alone.
- Current default sequence (2026-10-25): arrays → queues → priority queues/heaps
  → graphs, including BFS and DFS. Grokking supplies compatible pattern/lesson order.
  Maximum Sum Subarray of Size K was presented; no attempt is recorded.
  The older graph traversal/dependency-ordering queue remains historical.

## When asked to "implement X" from an interview-style prompt
(Applies to implement-and-critique requests, not to practice mode above.)
1. Restate the problem in one line to confirm scope (no lengthy requirements-gathering).
2. Implement it as a new/updated class following the existing style (plain Java, no
   frameworks, `UUID` for IDs, constructor-injected collaborators).
3. Run it (compile + a quick `main`/manual check) before calling it done.
4. Critique per the rules above, written into the file as a header comment (or inline
   next to the specific method it concerns) — not just spoken in chat.
