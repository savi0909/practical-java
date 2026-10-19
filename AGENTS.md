# Practical Java backend interview study

## Purpose and mode
- Read MEMORY.md and CONTEXT.md before continuing study work.
- Latest preference: short, plain language and small chunks. Build each example incrementally, one useful step per turn; then stop. Do not dump the whole lesson at once. This overrides the full response format below when it would overwhelm the learner.
- Improve the learner's existing interface and implementation instead of adding a parallel solution. Keep only a few basic tests; add detailed cases later when needed or requested.
- Help an experienced Staff/Principal backend engineer study correct, practical implementations and explain them under 45–60 minute interview constraints.
- This is study mode, not a mock interview. Show the solution directly; never require the learner to attempt it first.
- Proceed with sensible assumptions and state them briefly. Start small; introduce advanced variants only when requested.
- Complete one topic, update CONTEXT.md, then stop and wait for the next instruction. Do not generate the full collection.

## Required response format for each problem
1. **Contract:** 3–5 bullets defining requirements and assumptions. Explicitly bound single-JVM versus distributed scope, persistence, ordering, expiry, and concurrency where relevant.
2. **High-level approach:** 3–4 sentences explaining the logic. Compare alternatives only where correctness or performance materially differs.
3. **Java implementation:** complete, compilable code with imports and concise comments at key blocks. Prefer plain Java and standard-library classes; avoid frameworks, unnecessary interfaces, and excessive abstraction. State the Java version where relevant.
4. **Interview explanation:** a natural 60–90 second spoken explanation following requirements → approach → correctness → complexity → trade-off. Teach the reasoning, rather than a memorized script.
5. **Follow-ups:** at most three likely questions with short answers covering the most relevant limitation, concurrency issue, or extension.

## Implementation standards
- Identify shared state, its protection, and the atomic operation or critical section.
- Thread-safe collections alone do not make compound operations atomic; verify the complete operation.
- Do not claim lock-free, wait-free, fairness, or performance guarantees without justification. Avoid unsupported throughput estimates.
- Distinguish uniqueness, monotonic allocation, and observed return order.
- Prefer a monotonic clock for elapsed time. Address overflow, expiry boundaries, and cleanup when relevant.
- Define complexity variables and separate time from space; account for contention and retries.
- Keep production extensions brief and separate from the base solution. Never silently expand single-JVM scope into distributed design.

## Workspace and validation
- Each problem and its solution gets its own descriptive Java package under `src/study/<topic>/`, with the directory matching the package declaration (for example, `study.idgenerator`). Keep README.md (contract, approach, explanation, follow-ups), implementation, and focused checks together in that package directory.
- Preserve existing exercise behavior when organizing files. Use separate topic packages and no build dependencies for these small components. Do not create future lessons before requested.
- Compile every supplied implementation. Add only focused checks of meaningful behavior; use a small concurrent check when useful.
- A stress check is not proof of concurrency correctness. Explain correctness from the atomic operation and invariants.
- Record actual compilation/check results and their limits in the topic README.md and CONTEXT.md.
- CONTEXT.md tracks candidate context, current problem, completed topics, decisions, clarifications, and next topic. Supplied study material does not establish independent implementation or mastery.

## Sequence
1. Thread-safe, fast ID generator.
2. Fixed-window rate limiter per client.
3. Token-bucket rate limiter.
4. Expiring in-memory key-value store.
5. Idempotency-key registry.
6. Concurrent inventory reservation.
7. Bounded blocking queue.
8. Simple delayed-task scheduler.

## First topic constraints
One JVM and multiple threads; unique increasing long IDs in allocation order; no persistence or multi-machine uniqueness; minimal per-call allocation. Explain synchronized versus AtomicLong, why LongAdder cannot allocate unique sequence IDs, restart behavior, and overflow.
