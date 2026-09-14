// Problem     : Largest Element in Array
// LeetCode    : https://leetcode.com/problems/find-maximum-in-array/ (custom)
// Pattern     : Linear Scan / Traversal
// Difficulty  : Easy
// Date        :
//
// UNDERSTAND:
//   - Input  : int[] arr
//   - Output : largest element
//   - Edge cases: single element, all same
//
// BRUTE FORCE:
//   Sort the array and return last element
//   Time : O(n log n), Space: O(1)
//
// OPTIMAL:
//   Single pass — track max
//   Time : O(n), Space: O(1)
//
// WHY IT WORKS:
//   We visit every element once and track the running max
//
// JAVA CONCEPTS USED:
//   Arrays, for-loop
//
// WHAT I GOT WRONG:
//   (fill after first attempt)
// ============================================================

package dsa.arrays;

public class LargestElement {

    // ── BRUTE FORCE ─────────────────────────────────────────
    public int largestBrute(int[] arr) {
        java.util.Arrays.sort(arr);
        return arr[arr.length - 1];
    }

    // ── OPTIMAL ─────────────────────────────────────────────
    public int largestOptimal(int[] arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) max = arr[i];
        }
        return max;
    }

    // ── TEST ─────────────────────────────────────────────────
    public static void main(String[] args) {
        LargestElement sol = new LargestElement();

        System.out.println(sol.largestOptimal(new int[]{3, 1, 4, 1, 5, 9, 2})); // 9
        System.out.println(sol.largestOptimal(new int[]{-5, -3, -1}));           // -1
        System.out.println(sol.largestOptimal(new int[]{7}));                    // 7
    }
}
