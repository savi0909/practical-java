package dsa.arrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Practice: merge all overlapping intervals and return them sorted by start time.
 * Input may be unsorted or empty; each interval has start <= end.
 * Shared endpoints count as overlapping. There are at most 100,000 intervals.
 * Example: [[1,3], [2,6], [8,10], [15,18]] -> [[1,6], [8,10], [15,18]].
 * Input mutation is unspecified: state your choice when discussing the approach.
 *
 * Review after implementation: time/space complexity, empty input, duplicates, nesting,
 * touching endpoints, integer extremes, repeated calls, scale, and alternative tradeoffs.
 * This is a preparation exercise, not a confirmed Okta question.
 */
public class MergeOverlappingIntervals {

    public int[][] merge(int[][] intervals) {
        if (intervals.length == 0) {
            return new int[0][2];
        }

        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        List<int[]> result = new ArrayList<>();

        int start = intervals[0][0];
        int end = intervals[0][1];

        for (int i = 1; i < intervals.length; i++) {
            int[] next = intervals[i];

            if (next[0] <= end) {
                // Overlap: extend the current interval.
                end = Math.max(end, next[1]);
            } else {
                // Gap: save the current interval.
                result.add(new int[]{start, end});
                start = next[0];
                end = next[1];
            }
        }

        // Save the final interval.
        result.add(new int[]{start, end});

        return result.toArray(new int[result.size()][]);
    }

    private record Scenario(String name, int[][] input, int[][] expected) {}

    public static void main(String[] args) {
        Scenario[] scenarios = {
                new Scenario("overlapping groups",
                        new int[][]{{1, 3}, {2, 6}, {8, 10}, {15, 18}},
                        new int[][]{{1, 6}, {8, 10}, {15, 18}}),
                new Scenario("shared endpoint",
                        new int[][]{{1, 4}, {4, 5}}, new int[][]{{1, 5}}),
                new Scenario("unsorted input",
                        new int[][]{{5, 7}, {1, 3}, {2, 4}}, new int[][]{{1, 4}, {5, 7}}),
                new Scenario("empty input", new int[0][2], new int[0][2]),
                new Scenario("single point", new int[][]{{3, 3}}, new int[][]{{3, 3}}),
                new Scenario("nested and duplicate intervals",
                        new int[][]{{1, 10}, {2, 3}, {1, 10}, {4, 8}}, new int[][]{{1, 10}}),
                new Scenario("chain of touching intervals",
                        new int[][]{{3, 5}, {1, 2}, {2, 3}}, new int[][]{{1, 5}}),
                new Scenario("disjoint intervals and integer extremes",
                        new int[][]{{Integer.MAX_VALUE, Integer.MAX_VALUE}, {-3, -1},
                                {Integer.MIN_VALUE, Integer.MIN_VALUE}},
                        new int[][]{{Integer.MIN_VALUE, Integer.MIN_VALUE}, {-3, -1},
                                {Integer.MAX_VALUE, Integer.MAX_VALUE}})
        };

        MergeOverlappingIntervals solution = new MergeOverlappingIntervals();
        int passed = 0;
        for (Scenario scenario : scenarios) {
            // Keep the displayed input intact even if the implementation mutates its argument.
            int[][] input = Arrays.stream(scenario.input()).map(int[]::clone).toArray(int[][]::new);
            try {
                int[][] actual = solution.merge(input);
                boolean correct = Arrays.deepEquals(actual, scenario.expected());
                if (correct) {
                    passed++;
                }
                System.out.printf("%s %s: input=%s, expected=%s, actual=%s%n",
                        correct ? "PASS" : "FAIL", scenario.name(),
                        Arrays.deepToString(scenario.input()),
                        Arrays.deepToString(scenario.expected()), Arrays.deepToString(actual));
            } catch (UnsupportedOperationException e) {
                System.out.println("Implementation pending: " + e.getMessage());
                return;
            }
        }
        System.out.printf("%d/%d scenarios passed%n", passed, scenarios.length);
        if (passed != scenarios.length) {
            throw new AssertionError("Some scenarios failed");
        }
    }
}
