# Atlassian coding practice — short handover

Updated 2026-10-31. Workspace: `C:/Users/abhis/hello`; Java 17/21.

- Goal: Atlassian Principal Backend coding preparation, DSA and Code Design.
  Four-week target-specific window; diagnostics inform allocation, no forced quotas.
  Round numbering is provisional. Full brief: `atlassian-rounds-1-and-2-preparation.md`.
- Practice mode: one problem at a time. Let the learner explain and implement;
  no solution unless asked, no next problem without permission. Give concise feedback.
- **Critique in chat. Do not edit the learner's code unless explicitly requested.**
  This direct preference overrides the older source-comment critique convention.
- Current selected progression: Two Sum → Group Anagrams → Merge Intervals →
  Total Busy Time per Employee. Meeting task is deferred; Code Design diagnostic later.
- Two Sum: learner wrote a correct HashMap solution after a complement hint;
  learner reported passing samples. Hint-assisted, timing unmeasured. Reviewed
  map initialization, single get versus containsKey/get, and {-1,-1} fallback.
- `src/main/java/arrays/ref/TwoSumUnSorted.java` is the learner's implementation.
  Preserve it. A shorter version was demonstrated in chat afterward.
- On request, supplied `arrays.ref.TwoSumUnSortedAllPairs`: map value to all earlier
  indices, return each (i,j) once with i<j; no match returns {{-1,-1}}.
  Java 21 compile and 11 sample checks passed. Expected O(n+p) time/space including
  p output pairs; p may be quadratic. Demonstrated, not independently solved.
- **Current problem: Group Anagrams. Presented; no approach or implementation yet.**
  Signature: `public static List<List<String>> groupAnagrams(String[] words)`.
  Up to 10,000 non-null strings, length 0–100, lowercase a–z. Preserve duplicates
  and input; any group/member order. Empty input → []; [""] → [[""]].
  Example: ["eat","tea","tan","ate","nat","bat"] →
  [["eat","tea","ate"],["tan","nat"],["bat"]].
  Prior reference/demo exists: call this a revisit, not unseen independent evidence.
- Next action: invite the learner's grouping/key approach and complexity, then
  their Java implementation. Do not reveal the key technique or existing solution.
- Added five optional unsolved `arrays.ref` scaffolds at learner request:
  `ValidAnagramPractice`, `GroupAnagramsPractice`, `GroupShiftedStringsPractice`,
  `TopKFrequentWordsPractice`, `FindAnagramStartsPractice`. Contracts, examples and
  TODO bodies only. Current problem remains Group Anagrams; no new attempt recorded.
- Read project AGENTS.md. Detailed history: `coding-guidance-handover.md`;
  current order: `learning-plan.md`. Older graph-first/fixed-window starting points
  are superseded for this session. No timed readiness or category mastery established.
