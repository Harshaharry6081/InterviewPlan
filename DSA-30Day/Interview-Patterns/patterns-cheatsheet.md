# 🔍 DSA Interview Patterns — Java Cheatsheet

> Recognize the pattern → Apply the template → Solve the problem

---

## 1. Two Pointer Pattern

**When to use**: Sorted array, palindrome, pair sum, Dutch flag  
**Time**: O(n), **Space**: O(1)

```java
// Basic two pointer
int left = 0, right = arr.length - 1;
while (left < right) {
    if (condition) left++;
    else right--;
}
```

**Key Problems**: Two Sum II, 3Sum, Container With Most Water, Valid Palindrome

---

## 2. Sliding Window Pattern

**When to use**: Subarray/substring of size k, longest/shortest subarray

### Fixed Window
```java
int sum = 0;
for (int i = 0; i < k; i++) sum += arr[i];
int maxSum = sum;
for (int i = k; i < arr.length; i++) {
    sum += arr[i] - arr[i - k];
    maxSum = Math.max(maxSum, sum);
}
```

### Variable Window
```java
int left = 0, result = 0;
Map<Character, Integer> map = new HashMap<>();
for (int right = 0; right < s.length(); right++) {
    map.merge(s.charAt(right), 1, Integer::sum);
    while (/* window invalid */) {
        map.merge(s.charAt(left), -1, Integer::sum);
        if (map.get(s.charAt(left)) == 0) map.remove(s.charAt(left));
        left++;
    }
    result = Math.max(result, right - left + 1);
}
```

**Key Problems**: Longest Substring Without Repeating, Minimum Window Substring, Fruit Into Baskets

---

## 3. Binary Search Pattern

**When to use**: Sorted array, search in range, "minimum/maximum possible answer"

```java
// Standard binary search
int lo = 0, hi = arr.length - 1;
while (lo <= hi) {
    int mid = lo + (hi - lo) / 2;   // avoid overflow!
    if (arr[mid] == target) return mid;
    else if (arr[mid] < target) lo = mid + 1;
    else hi = mid - 1;
}

// Binary search on answer
int lo = min_possible, hi = max_possible;
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (feasible(mid)) hi = mid;     // find minimum
    else lo = mid + 1;
}
```

**Key Problems**: Search in Rotated Array, Koko Eating Bananas, Capacity to Ship Packages

---

## 4. HashMap / Hashing Pattern

**When to use**: Frequency count, duplicates, complement, anagram

```java
// Frequency count
Map<Integer, Integer> freq = new HashMap<>();
for (int n : arr) freq.merge(n, 1, Integer::sum);

// Complement (Two Sum)
Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    int complement = target - nums[i];
    if (seen.containsKey(complement)) return new int[]{seen.get(complement), i};
    seen.put(nums[i], i);
}
```

**Key Problems**: Two Sum, Group Anagrams, Subarray Sum Equals K, Longest Consecutive Sequence

---

## 5. Fast & Slow Pointer (Floyd's)

**When to use**: Linked list cycle, middle of list, palindrome LL

```java
// Find middle
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
}
// slow is now at middle

// Detect cycle
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
    if (slow == fast) return true;
}
```

**Key Problems**: Middle of LL, Linked List Cycle, Palindrome LL, Reorder List

---

## 6. Tree DFS Pattern

**When to use**: Tree traversal, path, height, subtree problems

```java
// Recursive DFS
int dfs(TreeNode node) {
    if (node == null) return 0;           // base case
    int left = dfs(node.left);
    int right = dfs(node.right);
    // process current node
    return /* result */;
}
```

**Key Problems**: Max Depth, Diameter, Balanced Tree, Path Sum, LCA

---

## 7. Tree BFS / Level Order Pattern

**When to use**: Level-by-level processing, shortest path in tree

```java
Queue<TreeNode> queue = new LinkedList<>();
queue.offer(root);
while (!queue.isEmpty()) {
    int size = queue.size();
    List<Integer> level = new ArrayList<>();
    for (int i = 0; i < size; i++) {
        TreeNode node = queue.poll();
        level.add(node.val);
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
    result.add(level);
}
```

**Key Problems**: Level Order Traversal, Right Side View, Zigzag Traversal

---

## 8. Monotonic Stack Pattern

**When to use**: Next greater/smaller element, span, histogram

```java
// Next Greater Element
Deque<Integer> stack = new ArrayDeque<>();
int[] result = new int[nums.length];
Arrays.fill(result, -1);
for (int i = 0; i < nums.length; i++) {
    while (!stack.isEmpty() && nums[stack.peek()] < nums[i]) {
        result[stack.pop()] = nums[i];
    }
    stack.push(i);
}
```

**Key Problems**: Next Greater Element, Daily Temperatures, Largest Rectangle in Histogram

---

## 9. Backtracking Pattern

**When to use**: All subsets, permutations, combinations, N-Queens

```java
void backtrack(/* params */, List<List<Integer>> result, List<Integer> current) {
    if (/* base condition */) {
        result.add(new ArrayList<>(current));
        return;
    }
    for (int i = start; i < candidates.length; i++) {
        // skip duplicates if needed
        current.add(candidates[i]);
        backtrack(/* next state */);
        current.remove(current.size() - 1);  // undo choice
    }
}
```

**Key Problems**: Subsets, Permutations, Combination Sum, N-Queens

---

## 10. BFS on Graph Pattern

**When to use**: Shortest path, level order, connected components

```java
// BFS template
boolean[] visited = new boolean[n];
Queue<Integer> queue = new LinkedList<>();
queue.offer(start);
visited[start] = true;
while (!queue.isEmpty()) {
    int node = queue.poll();
    for (int neighbor : graph.get(node)) {
        if (!visited[neighbor]) {
            visited[neighbor] = true;
            queue.offer(neighbor);
        }
    }
}
```

**Key Problems**: Number of Islands, Shortest Path, Clone Graph, Rotting Oranges

---

## 11. Dynamic Programming Pattern

### 1D DP
```java
int[] dp = new int[n + 1];
dp[0] = base;
for (int i = 1; i <= n; i++) {
    dp[i] = /* transition using dp[i-1], dp[i-2], etc. */;
}
```

### 2D DP (Grid)
```java
int[][] dp = new int[m + 1][n + 1];
for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        dp[i][j] = /* transition */;
    }
}
```

**Key Problems**: Climbing Stairs, House Robber, Coin Change, LCS, Edit Distance

---

## 12. Heap / Priority Queue Pattern

**When to use**: Top K, Kth largest/smallest, merge sorted lists

```java
// Min Heap (default in Java)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();

// Max Heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

// Custom comparator
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);

// Top K frequent
PriorityQueue<Map.Entry<Integer,Integer>> pq = 
    new PriorityQueue<>((a,b) -> a.getValue() - b.getValue());
```

**Key Problems**: Kth Largest, Top K Frequent, K Closest Points, Merge K Sorted Lists

---

## 13. Prefix Sum Pattern

**When to use**: Subarray sum, range sum queries

```java
// Build prefix sum
int[] prefix = new int[n + 1];
for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + arr[i];

// Range sum [l, r]
int sum = prefix[r + 1] - prefix[l];

// Subarray sum equals K
Map<Integer, Integer> map = new HashMap<>();
map.put(0, 1);
int count = 0, prefixSum = 0;
for (int num : nums) {
    prefixSum += num;
    count += map.getOrDefault(prefixSum - k, 0);
    map.merge(prefixSum, 1, Integer::sum);
}
```

**Key Problems**: Range Sum Query, Subarray Sum Equals K, Product Except Self

---

## ⚡ Java Quick Reference

### Collections Complexity

| Structure | Add | Remove | Get | Contains |
|-----------|-----|--------|-----|----------|
| ArrayList | O(1)* | O(n) | O(1) | O(n) |
| LinkedList | O(1) | O(1) | O(n) | O(n) |
| HashMap | O(1)* | O(1)* | O(1)* | O(1)* |
| TreeMap | O(log n) | O(log n) | O(log n) | O(log n) |
| HashSet | O(1)* | O(1)* | — | O(1)* |
| PriorityQueue | O(log n) | O(log n) | O(1) poll | O(n) |
| ArrayDeque | O(1) | O(1) | O(1) | O(n) |

### Most Used Snippets

```java
// Sort array
Arrays.sort(arr);

// Sort 2D array by first element
Arrays.sort(arr, (a, b) -> a[0] - b[0]);

// Reverse sort
Arrays.sort(arr, Collections.reverseOrder());  // for Integer[]

// String to char array
char[] chars = s.toCharArray();

// Char to int
int digit = c - '0';

// Char comparison
Character.isDigit(c);
Character.isAlphabetic(c);
Character.toLowerCase(c);

// Integer limits
Integer.MAX_VALUE;   // 2^31 - 1
Integer.MIN_VALUE;   // -2^31

// Math
Math.max(a, b);
Math.min(a, b);
Math.abs(x);

// String Builder
StringBuilder sb = new StringBuilder();
sb.append("hello");
sb.reverse();
sb.toString();
```

---

## 🎯 Pattern Recognition Quick Guide

| Problem says... | Think... |
|----------------|----------|
| "sorted array" + "find target" | Binary Search |
| "subarry" + "sum/length" | Sliding Window or Prefix Sum |
| "all combinations/subsets" | Backtracking |
| "shortest path" | BFS |
| "connected components" | BFS/DFS |
| "kth largest/smallest" | Heap |
| "next greater element" | Monotonic Stack |
| "linked list" + "cycle/middle" | Fast & Slow Pointer |
| "count pairs" | Two Pointer or HashMap |
| "maximize/minimize with choices" | DP or Greedy |
| "tree traversal" | DFS (recursive) |
| "level by level" | BFS |
| "anagram/frequency" | HashMap |
| "in-place sort" | Dutch National Flag |
