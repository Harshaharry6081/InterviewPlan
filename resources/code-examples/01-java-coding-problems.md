# 💻 Coding Problems — Java Fundamentals & Collections

> Practice these by hand (no IDE first!). Then verify in IDE.

---

## 🔵 Arrays & Two Pointers

### 1. Two Sum — Find pair summing to target
```java
// O(n) — HashMap approach
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[]{map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    return new int[]{};
}
// Test: [2,7,11,15], target=9 → [0,1]
// Test: [3,3], target=6 → [0,1]
```

### 2. Maximum Subarray (Kadane's Algorithm)
```java
// O(n) — Track running max
public int maxSubArray(int[] nums) {
    int maxSum = nums[0];
    int currentSum = nums[0];

    for (int i = 1; i < nums.length; i++) {
        currentSum = Math.max(nums[i], currentSum + nums[i]);
        maxSum = Math.max(maxSum, currentSum);
    }
    return maxSum;
}
// Test: [-2,1,-3,4,-1,2,1,-5,4] → 6 (subarray [4,-1,2,1])
```

### 3. Move Zeroes to End (in-place)
```java
// O(n) time, O(1) space
public void moveZeroes(int[] nums) {
    int insertPos = 0;
    for (int num : nums) {
        if (num != 0) nums[insertPos++] = num;
    }
    while (insertPos < nums.length) {
        nums[insertPos++] = 0;
    }
}
// Test: [0,1,0,3,12] → [1,3,12,0,0]
```

### 4. Find Duplicates in Array
```java
// Using Set — O(n) time, O(n) space
public List<Integer> findDuplicates(int[] nums) {
    Set<Integer> seen = new HashSet<>();
    List<Integer> duplicates = new ArrayList<>();
    for (int num : nums) {
        if (!seen.add(num)) duplicates.add(num);
    }
    return duplicates;
}
// Test: [4,3,2,7,8,2,3,1] → [2,3]
```

---

## 🟢 Strings

### 5. Reverse Words in a String
```java
public String reverseWords(String s) {
    String[] words = s.trim().split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (int i = words.length - 1; i >= 0; i--) {
        sb.append(words[i]);
        if (i > 0) sb.append(" ");
    }
    return sb.toString();
}
// Test: "  Hello World  " → "World Hello"
// Test: "sky is blue" → "blue is sky"
```

### 6. Check Anagram
```java
public boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    int[] count = new int[26];
    for (char c : s.toCharArray()) count[c - 'a']++;
    for (char c : t.toCharArray()) {
        if (--count[c - 'a'] < 0) return false;
    }
    return true;
}
// Test: "anagram", "nagaram" → true
// Test: "rat", "car" → false
```

### 7. Longest Substring Without Repeating Characters
```java
// Sliding Window — O(n)
public int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> map = new HashMap<>();
    int maxLen = 0, left = 0;

    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        if (map.containsKey(c)) {
            left = Math.max(left, map.get(c) + 1);
        }
        map.put(c, right);
        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}
// Test: "abcabcbb" → 3 ("abc")
// Test: "bbbbb" → 1
// Test: "pwwkew" → 3 ("wke")
```

### 8. Valid Parentheses
```java
public boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    for (char c : s.toCharArray()) {
        if (c == '(' || c == '{' || c == '[') {
            stack.push(c);
        } else {
            if (stack.isEmpty()) return false;
            char top = stack.pop();
            if (c == ')' && top != '(') return false;
            if (c == '}' && top != '{') return false;
            if (c == ']' && top != '[') return false;
        }
    }
    return stack.isEmpty();
}
// Test: "()" → true
// Test: "()[]{}" → true
// Test: "(]" → false
// Test: "([)]" → false
```

---

## 🟡 Java 8 Streams — Coding Practice

### 9. Find Second Largest in List
```java
public Optional<Integer> secondLargest(List<Integer> list) {
    return list.stream()
        .distinct()
        .sorted(Comparator.reverseOrder())
        .skip(1)
        .findFirst();
}
// Test: [3, 1, 4, 1, 5, 9, 2, 6] → Optional[6]
```

### 10. Group Employees by Department & Find Highest Salary Each
```java
Map<String, Optional<Employee>> highestPaidPerDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.maxBy(Comparator.comparing(Employee::getSalary))
    ));
```

### 11. Count Frequency of Each Character
```java
public Map<Character, Long> charFrequency(String s) {
    return s.chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
}
// Test: "hello" → {h=1, e=1, l=2, o=1}
```

### 12. Flatten a List of Lists
```java
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
List<Integer> flat = nested.stream()
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
// Result: [1, 2, 3, 4, 5]
```

---

## 🔴 Linked List

### 13. Reverse a Linked List
```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null;
    ListNode curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;  // prev is the new head
}
// 1→2→3→4→5 becomes 5→4→3→2→1
```

### 14. Detect Cycle in Linked List (Floyd's Algorithm)
```java
public boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;  // Cycle detected
    }
    return false;
}
```

---

## ⭐ Must Know: FizzBuzz (Classic)
```java
public List<String> fizzBuzz(int n) {
    List<String> result = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
        if (i % 15 == 0) result.add("FizzBuzz");
        else if (i % 3 == 0) result.add("Fizz");
        else if (i % 5 == 0) result.add("Buzz");
        else result.add(String.valueOf(i));
    }
    return result;
}

// Streams version (to impress!)
IntStream.rangeClosed(1, n)
    .mapToObj(i -> i % 15 == 0 ? "FizzBuzz"
                 : i % 3 == 0  ? "Fizz"
                 : i % 5 == 0  ? "Buzz"
                 : String.valueOf(i))
    .forEach(System.out::println);
```
