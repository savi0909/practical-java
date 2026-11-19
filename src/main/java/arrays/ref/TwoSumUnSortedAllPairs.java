package arrays.ref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns every zero-based index pair (i, j) with i < j and nums[i] + nums[j] = target.
 * Pairs are ordered by j, then i. Input is preserved; null input is unsupported.
 * Returns {{-1, -1}} when no pair exists, including empty or singleton input.
 */
public class TwoSumUnSortedAllPairs {

    public static int[][] twoSum(int[] nums, int target) {
        Map<Long, List<Integer>> seen = new HashMap<>();
        List<int[]> pairs = new ArrayList<>();

        for (int j = 0; j < nums.length; j++) {
            List<Integer> matches = seen.get((long) target - nums[j]);
            if (matches != null) {
                for (int i : matches) {
                    pairs.add(new int[]{i, j});
                }
            }
            seen.computeIfAbsent((long) nums[j], key -> new ArrayList<>()).add(j);
        }

        return pairs.isEmpty() ? new int[][]{{-1, -1}} : pairs.toArray(int[][]::new);
    }

    public static void main(String[] args) {
        runSampleChecks();
        System.out.println("arrays.ref.TwoSumUnSortedAllPairs: all checks passed");
    }

    private static void runSampleChecks() {
        check(new int[]{2, 7, 11, 15}, 9, new int[][]{{0, 1}});
        check(new int[]{1, 4, 2, 3}, 5, new int[][]{{0, 1}, {2, 3}});
        check(new int[]{3, 3, 3}, 6, new int[][]{{0, 1}, {0, 2}, {1, 2}});
        check(new int[]{1, 4, 1, 4}, 5,
                new int[][]{{0, 1}, {1, 2}, {0, 3}, {2, 3}});
        check(new int[]{0, 0, 0}, 0, new int[][]{{0, 1}, {0, 2}, {1, 2}});
        check(new int[]{-3, 3, -3, 3}, 0,
                new int[][]{{0, 1}, {1, 2}, {0, 3}, {2, 3}});
        check(new int[]{1, 2, 3}, 10, new int[][]{{-1, -1}});
        check(new int[]{}, 0, new int[][]{{-1, -1}});
        check(new int[]{3}, 6, new int[][]{{-1, -1}});
        check(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}, -1,
                new int[][]{{0, 1}});
        check(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}, -2,
                new int[][]{{-1, -1}}); // Overflow must not create a false match.
    }

    private static void check(int[] nums, int target, int[][] expected) {
        int[] original = nums.clone();
        int[][] actual = twoSum(nums, target);
        if (!Arrays.deepEquals(actual, expected)) {
            throw new AssertionError("input=" + Arrays.toString(original)
                    + ", target=" + target + ", expected=" + Arrays.deepToString(expected)
                    + ", actual=" + Arrays.deepToString(actual));
        }
        if (!Arrays.equals(nums, original)) {
            throw new AssertionError("Input was modified");
        }
        System.out.println("PASS input=" + Arrays.toString(original)
                + ", target=" + target + ", pairs=" + Arrays.deepToString(actual));
    }
}
