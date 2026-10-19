package dsa.slidingwindow;

import java.util.HashSet;
import java.util.Set;

/**
 * Practice: return the length of the longest contiguous substring with no repeated characters.
 * Input is non-null ASCII, with length 0 through 100,000. Return only the length.
 * Examples: "abcabcbb" -> 3, "bbbbb" -> 1, "pwwkew" -> 3, "" -> 0.
 *
 * Review after implementation: time/space complexity, empty input, duplicate handling,
 * repeated calls, performance at the input limit, and alternative approaches/tradeoffs.
 * This is a preparation exercise, not a confirmed Okta question.
 */
public class LongestSubstringWithoutRepeating {

    public int lengthOfLongestSubstring(String s) {
        // TODO: implement after discussing your approach.
        Set<Character> window = new HashSet<>();
        int left = 0;
        int maxLength = 0;

        for (int right = 0; right < s.length(); right++) {
            char current = s.charAt(right);
            // Shrink until the earlier occurrence is removed.
            while (window.contains(current)) {
                window.remove(s.charAt(left));
                left++;
            }
            // Extend the window with the current character.
            window.add(current);
            int windowLength = right - left + 1;
            maxLength = Math.max(maxLength, windowLength);
        }
        return maxLength;
    }

    private record Scenario(String input, int expected) {}

    public static void main(String[] args) {
        Scenario[] scenarios = {
                new Scenario("abcabcbb", 3),
                new Scenario("bbbbb", 1),
                new Scenario("pwwkew", 3),
                new Scenario("", 0),
                new Scenario("a", 1),
                new Scenario("abcdef", 6),
                new Scenario("abba", 2),
                new Scenario("dvdf", 3),
                new Scenario("a b!a", 4)
        };

        LongestSubstringWithoutRepeating solution = new LongestSubstringWithoutRepeating();
        int passed = 0;
        for (Scenario scenario : scenarios) {
            try {
                int actual = solution.lengthOfLongestSubstring(scenario.input());
                boolean correct = actual == scenario.expected();
                if (correct) {
                    passed++;
                }
                System.out.printf("%s input=\"%s\", expected=%d, actual=%d%n",
                        correct ? "PASS" : "FAIL", scenario.input(), scenario.expected(), actual);
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
