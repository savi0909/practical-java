# Reusable problem-solving skills — proposal for review

Date: 2026-10-23. Status: approved and implemented for coding, LLD and system design.
Canonical repository: `D:/Codex Skills`; installed at `C:/Users/abhis/.codex/skills`.

## Review scope and findings

Reviewed the visible history of the Google Chat space “Claude Best”: 19 shared
resources (17 LinkedIn posts and two GitHub repositories), plus the linked ADHD
repository. Cross-chat search remained on its loading screen; this is not an
exhaustive review of all Google Chat conversations. Repository reviews covered
their rendered indexes/READMEs, not implementation audits or benchmark validation.

The useful recurring techniques are problem reframing, materially different
alternatives, explicit assumptions, observable correctness evidence and focused
project context. Promotional performance claims and prompts that invent personal
experience do not become skill instructions.

## 1. frame-problem

**Definition:** Analyze an ambiguous problem before solutioning. Separate the
desired outcome, observed symptoms, constraints, assumptions and unresolved
questions; produce a precise, testable problem statement.

**Use when:** A request is unclear, a proposed fix may address a symptom, or the
success criteria are missing.

**Instructions to include:**
- Read available project context before asking questions.
- Distinguish facts from assumptions and symptoms from underlying problems.
- Define success criteria and explicit exclusions.
- Ask only questions whose answers materially change the approach.

**Output:** Concise problem brief, important uncertainties and acceptance criteria.

**Example:** “Frame this cache problem: what behavior must we guarantee, and what remains ambiguous?”

**Existing-skill boundary:** Build on the existing problem-dissector where useful;
avoid duplicating its coding checklist. Keep this skill focused on framing.

## 2. explore-problem-variants

**Definition:** Explore useful variations and genuinely different approaches to a
framed problem, showing what each variation adds and costs.

**Use when:** The learner asks for alternatives, follow-ups or scope expansion.

**Instructions to include:**
- Preserve the original problem as the baseline.
- Vary meaningful dimensions: concurrency, persistence, ordering, scale, failures
  or API behavior.
- Separate deeper analysis from additional functionality.
- Explain the assumption each alternative changes, its benefits and costs.
- Recommend extensions without silently adding them to the assignment.
- Separate generation from evaluation; parallel agents are optional and require
  applicable authorization, not a default prerequisite.

**Output:** Comparison of baseline, alternatives and optional extensions.

**Example:** “Expand an in-memory scheduler into useful interview variants, keeping each contract separate.”

## 3. design-solution

**Definition:** Turn a clear problem contract into a detailed solution approach,
connecting data structures, algorithms or components to correctness, complexity
and tradeoffs.

**Use when:** A problem is framed and the learner requests a solution approach or
implementation blueprint.

**Instructions to include:**
- Establish the simplest correct baseline and compare relevant alternatives.
- Define state, invariants and complete operation boundaries.
- Trace a concrete example.
- Explain edge cases, complexity and failure behavior.
- Produce pseudocode, interfaces and a focused validation plan at the requested depth.
- Honor project mode: practice preserves the learner's reasoning opportunity;
  explicit requests for solutions or code receive them.

**Output:** Implementation-ready solution blueprint.

**Example:** “Design the Java solution for a bounded task queue, explaining blocking, interruption and shutdown.”

## 4. stress-test-solution

**Definition:** Critically assess a proposed approach or implementation using
counterexamples, assumption checks and observable validation.

**Use when:** The learner asks to challenge, critique or verify a solution.

**Instructions to include:**
- Identify concrete failure scenarios and relevant concurrency interleavings.
- Examine boundary conditions and unsupported assumptions.
- Rank concerns by impact and supporting evidence.
- Separate confirmed defects from unresolved questions.
- Recommend the smallest useful correction.
- Name tests, measurements or external evidence that could validate the result.
- Do not treat self-review, agreement or small stress checks as proof.

**Output:** Prioritized findings, counterexamples and verification steps.

**Example:** “Stress-test this rate limiter: can cleanup racing with admission grant extra quota?”

## 5. prepare-task-context

**Definition:** Assemble the minimum sufficient context for a task and rewrite a
vague request into a clear, executable prompt.

**Use when:** Preparing a complex task, improving a prompt or handing work to a
later chat.

**Instructions to include:**
- State the goal, current state, constraints and requested output.
- Identify relevant source files, raw data and authoritative references.
- Preserve decisions, unresolved questions and evidence limits.
- Specify how success will be checked.
- Reference maintained documents instead of duplicating them.
- Produce a reusable handover when requested; do not copy private transcripts
  into general-purpose skills.

**Output:** Task brief and improved prompt.

**Example:** “Prepare the context and prompt for continuing our scheduler study in another chat.”

## Shared behavior and proposed installation

- Plain language and detail proportional to the task; Java where applicable.
- Preserve the selected project's practice/study mode and durable preferences.
- Keep skills small and independently useful; do not mandate the full sequence.
- Current project state and attempt history stay in project context documents.
- Prefer adapting existing overlapping skills where that avoids duplication.

The five approved skills are installed at `C:/Users/abhis/.codex/skills/<skill-name>/`,
each with `SKILL.md` and `agents/openai.yaml`. Canonical sources are maintained in
`D:/Codex Skills/skills/`. Run `D:/Codex Skills/scripts/install.ps1` after editing
canonical sources; use `-Check` to verify installed copies without changing them.
The implemented skills explicitly support system-design requirements, capacity,
consistency, partitioning, data flows and failure/recovery scenarios as relevant.
The definitions above retain the reviewed proposal; inspect the canonical files
for the final instructions.

## Selected source references

- [Problem framing and decision prompts](https://www.linkedin.com/posts/obaloluwaolajosephisaiah_good-consultants-do-not-just-give-you-answers-share-7466080974615818242-RB2Y/)
- [Depth rather than larger scope](https://lnkd.in/p/dpaV-VG5)
- [ADHD: generation and evaluation separation](https://github.com/uditakhourii/adhd)
- [Open Code Review](https://github.com/alibaba/open-code-review)
- [Matt Pocock's composable skills](https://github.com/mattpocock/skills)

These are sources of techniques, not endorsements of all their claims, tool
installations or workflows. This proposal does not install software or authorize
external communications.
