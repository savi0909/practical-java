# Study preferences

- Performance organization (2026-10-19): prefer separate modules, such as Rate Limiter Lab, for performance-testing runs and benchmarking. Keep workloads, run configuration, instructions, and results with the relevant lab. This preference does not request creating or migrating a module during the documentation update.

- Rate-limiter clarification (2026-10-19): for the current practice, each user is a tenant and gets two requests per current five-second window; idle/missed windows grant no accumulated quota. This is fixed-window behavior. Implementation continues in rate-limiter-lab.

- Latest rate-limiter direction (2026-10-19): implement incrementally in Java 21+, with concise explanations and interview-level design decisions. Discussion has covered Fixed Window, Sliding Window Counter, Token Bucket briefly, and ConcurrentHashMap concurrency; coverage does not establish mastery. Continue in rate-limiter-lab, one useful step per turn.

- Project focus: practical Java engineering—concurrency, thread-safe components, rate limiters, ID generators, caches, queues, executors, and basic logging framework design. Interview preparation is one use, not the full scope.
- Short, plain language. Small chunks. No information dumps.
- Build each problem step by step. Show one useful step, then wait.
- Study mode: show the answer; do not ask for an attempt first.
- Improve existing code. Avoid duplicate solutions.
- Start with a few basic tests. Add detailed tests later if needed.
- Keep each problem in its own Java package.
- Rate limiter exception: both fixed-window and token-bucket examples now belong directly to the top-level `ratelimiter` package under `src/ratelimiter`, per explicit learner preference. Reuse the common RateLimiter contract when studying client choice per use case.
- For the simple ID-generator comparison, use ordinary platform threads; no virtual-thread runs.
- Keep correctness, but explain only what matters now.
- For new implementations, prefer ReentrantLock and Condition over synchronized. Preserve already completed topic files unless explicitly asked to change them.
- Put clear, concise learning comments at key points in the existing code, explaining why the logic and concurrency protection are needed. Prefer learning from the code; avoid generating separate guides unless requested. Keep required topic README notes minimal.
- Never reset an ID sequence: previously used IDs could be reissued. Comparison variants must clearly use separate namespaces.
- Candidate: experienced Staff/Principal Java backend engineer.
- Track progress in CONTEXT.md. Studying supplied code does not mean mastery.
