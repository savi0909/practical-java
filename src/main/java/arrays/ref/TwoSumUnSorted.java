package arrays.ref;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Practice: return zero-based indices of two distinct elements summing to target.
 * Exactly one pair exists; preserve the input. Either index order is accepted.
 * Scaffolding only: implementation and algorithm critique await your attempt.
 */
public class TwoSumUnSorted {

    public static int[] twoSum(int[] nums, int target){

        Map<Integer,Integer> pairs =  new HashMap<>(2*nums.length);
        int i = 0;
        for(int number : nums){
            if(pairs.containsKey(target - number)){
                int index  = pairs.get(target - number);
                return new int[]{index,i};
            }
            else {
                pairs.put(number,i);
            }
            i++;
        }
        return new int[]{-1,-1};
    }

    public static void main(String[] args) {
        try {
            runSampleChecks();
            System.out.println("arrays.ref.TwoSumUnSorted: all checks passed");
        } catch (UnsupportedOperationException e) {
            System.out.println("Implementation pending: " + e.getMessage());
        }
    }

    private static void runSampleChecks() {
        check(new int[]{2, 7, 11, 15}, 9, new int[]{0, 1});
        check(new int[]{3, 2, 4}, 6, new int[]{1, 2});
        check(new int[]{3, 3}, 6, new int[]{0, 1});              // duplicate values
        check(new int[]{-3, 4, 3, 90}, 0, new int[]{0, 2});     // mixed signs
        check(new int[]{-8, -3, -5, -1}, -9, new int[]{0, 3});  // negative target
        check(new int[]{0, 4, 3, 0}, 0, new int[]{0, 3});       // zero pair
        check(new int[]{8, 3, 2, 11}, 10, new int[]{0, 2});     // original indices
        check(new int[]{1_000_000_000, -1_000_000_000, 7}, 0,
                new int[]{0, 1});                             // value boundaries
    }

    private static void check(int[] nums, int target, int[] expected) {
        int[] original = nums.clone();
        int[] actual = twoSum(nums, target);
        boolean matches = actual != null && actual.length == 2
                && ((actual[0] == expected[0] && actual[1] == expected[1])
                || (actual[0] == expected[1] && actual[1] == expected[0]));
        if (!matches) {
            throw new AssertionError("input=" + Arrays.toString(original)
                    + ", target=" + target + ", expected=" + Arrays.toString(expected)
                    + " (either order), actual=" + Arrays.toString(actual));
        }
        if (!Arrays.equals(nums, original)) {
            throw new AssertionError("Input modified: " + Arrays.toString(original)
                    + " became " + Arrays.toString(nums));
        }
        System.out.println("PASS input=" + Arrays.toString(original)
                + ", target=" + target + ", indices=" + Arrays.toString(actual));
    }

}
