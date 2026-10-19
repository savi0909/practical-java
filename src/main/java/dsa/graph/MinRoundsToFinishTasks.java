package dsa.graph;

import java.util.List;

/**
 * Okta Round 2 practice — graph traversal / dependency ordering.
 *
 * <h2>Problem: Minimum Rounds to Finish Tasks</h2>
 * There are {@code n} tasks labeled {@code 0 .. n-1}. Each entry {@code {a, b}} in
 * {@code dependencies} means task {@code a} must finish in a STRICTLY EARLIER round than task
 * {@code b}.
 *
 * <p>In each round you may run any number of tasks, provided all of their prerequisites finished
 * in earlier rounds. Return the minimum number of rounds needed to finish every task, or
 * {@code -1} if it is impossible.
 *
 * <h2>Notation</h2>
 * n = number of tasks, E = {@code dependencies.length}.
 *
 * <h2>Constraints</h2>
 * <ul>
 *   <li>1 &lt;= n &lt;= 10^5</li>
 *   <li>0 &lt;= E &lt;= 2 * 10^5</li>
 *   <li>0 &lt;= a, b &lt; n, a != b</li>
 *   <li>The same pair may appear more than once.</li>
 * </ul>
 *
 * <h2>Your turn</h2>
 * <ol>
 *   <li>State the approach, data structures, invariant, and time/space in terms of n and E
 *       BEFORE coding.</li>
 *   <li>Implement {@link #minRounds(int, int[][])}.</li>
 *   <li>Run {@link #main}; every scenario should print PASS.</li>
 * </ol>
 *
 * <p>Critique (complexity, edge cases, scale, alternatives): fill in here after solving.
 */
public class MinRoundsToFinishTasks {

    /** Implement me. */
    public static int minRounds(int n, int[][] dependencies) {
        // TODO: implement in IDE
        throw new UnsupportedOperationException("TODO");
    }

    // ---------------------------------------------------------------- test harness

    private record Scenario(String name, int n, int[][] dependencies, int expected) {}

    private static int[][] chain(int n) {
        int[][] edges = new int[n - 1][];
        for (int i = 0; i < n - 1; i++) {
            edges[i] = new int[]{i, i + 1};
        }
        return edges;
    }

    private static final List<Scenario> SCENARIOS = List.of(
            new Scenario("example: diamond", 4,
                    new int[][]{{0, 1}, {0, 2}, {1, 3}, {2, 3}}, 3),
            new Scenario("example: 3-cycle", 3,
                    new int[][]{{0, 1}, {1, 2}, {2, 0}}, -1),
            new Scenario("example: no dependencies", 5,
                    new int[][]{}, 1),

            new Scenario("single task", 1,
                    new int[][]{}, 1),
            new Scenario("straight chain", 5,
                    new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}}, 5),
            new Scenario("chain labeled in reverse (label order != execution order)", 4,
                    new int[][]{{3, 2}, {2, 1}, {1, 0}}, 4),
            new Scenario("duplicate pairs", 3,
                    new int[][]{{0, 1}, {0, 1}, {1, 2}}, 3),
            new Scenario("many prerequisites into one task", 5,
                    new int[][]{{0, 4}, {1, 4}, {2, 4}, {3, 4}}, 2),
            new Scenario("two routes, longer one wins", 5,
                    new int[][]{{0, 1}, {1, 2}, {2, 4}, {0, 3}, {3, 4}}, 4),
            new Scenario("disconnected pieces plus isolated task", 6,
                    new int[][]{{0, 1}, {1, 2}, {3, 4}}, 3),

            new Scenario("cycle in one component, others fine", 5,
                    new int[][]{{0, 1}, {1, 2}, {3, 4}, {4, 3}}, -1),
            new Scenario("cycle reachable from a source, with a tail", 4,
                    new int[][]{{0, 1}, {1, 2}, {2, 1}, {2, 3}}, -1),

            new Scenario("large chain n=100000 (deep recursion / perf)", 100_000,
                    chain(100_000), 100_000)
    );

    public static void main(String[] args) {
        int passed = 0;
        for (Scenario s : SCENARIOS) {
            String status;
            try {
                int actual = minRounds(s.n(), s.dependencies());
                if (actual == s.expected()) {
                    status = "PASS";
                    passed++;
                } else {
                    status = "FAIL (expected " + s.expected() + ", got " + actual + ")";
                }
            } catch (Throwable t) {
                status = "ERROR (" + t.getClass().getSimpleName() + ": " + t.getMessage() + ")";
            }
            System.out.printf("%-60s %s%n", s.name(), status);
        }
        System.out.printf("%n%d/%d scenarios passed%n", passed, SCENARIOS.size());
    }
}
