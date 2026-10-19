package dsa.slidingwindow;

import java.util.List;

/**
 * Okta Round 2 practice — sliding window, medium.
 *
 * <h2>Problem: Longest Subarray With Bounded Spread</h2>
 * Given an integer array {@code nums} and an integer {@code limit}, return the length of the
 * longest contiguous subarray whose spread — {@code (max element) - (min element)} — is at most
 * {@code limit}.
 *
 * <h2>Notation</h2>
 * n = {@code nums.length}.
 *
 * <h2>Constraints</h2>
 * <ul>
 *   <li>1 &lt;= n &lt;= 10^5</li>
 *   <li>-10^9 &lt;= nums[i] &lt;= 10^9</li>
 *   <li>0 &lt;= limit &lt;= 10^9</li>
 *   <li>Do not modify {@code nums}.</li>
 * </ul>
 *
 * <h2>Your turn</h2>
 * <ol>
 *   <li>Before coding: what must stay true about the window, what does it need to know about
 *       its contents, and what happens when an element leaves it?</li>
 *   <li>State the simplest correct approach and its cost in n. Then say what you would improve.</li>
 *   <li>Implement {@link #longestBoundedSpread(int[], int)} and run {@link #main}.</li>
 * </ol>
 *
 * <p>Critique (complexity, edge cases, scale, alternatives): fill in here after solving.
 */
public class LongestBoundedSpreadSubarray {

    /** Implement me. */
    public static int longestBoundedSpread(int[] nums, int limit) {
        // TODO: implement in IDE
        throw new UnsupportedOperationException("TODO");
    }

    // ---------------------------------------------------------------- test harness

    private record Scenario(String name, int[] nums, int limit, int expected) {}

    private static int[] alternating(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = (i % 2 == 0) ? 1 : 2;
        }
        return a;
    }

    private static final List<Scenario> SCENARIOS = List.of(
            new Scenario("example 1", new int[]{8, 2, 4, 7}, 4, 2),
            new Scenario("example 2", new int[]{10, 1, 2, 4, 7, 2}, 5, 4),
            new Scenario("example 3: limit 0, runs of equal values",
                    new int[]{4, 2, 2, 2, 4, 4, 2, 2}, 0, 3),

            new Scenario("single element", new int[]{5}, 0, 1),
            new Scenario("all equal, limit 0", new int[]{7, 7, 7, 7}, 0, 4),
            new Scenario("whole array fits exactly at the limit", new int[]{1, 3, 2, 4}, 3, 4),
            new Scenario("strictly increasing", new int[]{1, 2, 3, 4, 5, 6}, 2, 3),
            new Scenario("strictly decreasing", new int[]{9, 7, 5, 3, 1}, 4, 3),
            new Scenario("negatives, huge limit", new int[]{5, -5, 10, -10}, 1_000_000_000, 4),
            new Scenario("extreme values, spread 2e9 > limit",
                    new int[]{-1_000_000_000, 1_000_000_000}, 1_000_000_000, 1),

            new Scenario("max slides out of the window", new int[]{100, 1, 2, 3, 4}, 3, 4),
            new Scenario("min slides out of the window", new int[]{1, 50, 51, 52, 53}, 3, 4),
            new Scenario("duplicate max: one copy leaves, one stays",
                    new int[]{4, 4, 1, 3}, 2, 2),

            new Scenario("large n=100000, whole array valid (O(n^2) too slow)",
                    alternating(100_000), 1, 100_000),
            new Scenario("large n=100000, limit 0 (answer 1)",
                    alternating(100_000), 0, 1)
    );

    public static void main(String[] args) {
        int passed = 0;
        for (Scenario s : SCENARIOS) {
            String status;
            try {
                int actual = longestBoundedSpread(s.nums().clone(), s.limit());
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
