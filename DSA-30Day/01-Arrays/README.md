# 📚 Arrays — Study Notes

**Days**: 1, 2, 7  
**Striver A2Z**: Step 3 — Arrays  

---

## Key Concepts

### 1. Traversal
- Single pass: O(n)
- Know `arr.length` in Java

### 2. Prefix Sum
```java
int[] prefix = new int[n + 1];
for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + arr[i];
// Range sum [l, r] = prefix[r+1] - prefix[l]
```

### 3. Dutch National Flag (3-way partition)
```java
int lo = 0, mid = 0, hi = arr.length - 1;
while (mid <= hi) {
    if (arr[mid] == 0) swap(arr, lo++, mid++);
    else if (arr[mid] == 1) mid++;
    else swap(arr, mid, hi--);
}
```

### 4. Kadane's Algorithm
```java
int maxSum = arr[0], currentSum = arr[0];
for (int i = 1; i < arr.length; i++) {
    currentSum = Math.max(arr[i], currentSum + arr[i]);
    maxSum = Math.max(maxSum, currentSum);
}
```

---

## Problems Checklist

| # | Problem | Difficulty | Done |
|---|---------|------------|------|
| 1 | Largest Element | Easy | [ ] |
| 2 | Second Largest | Easy | [ ] |
| 3 | Check if Sorted | Easy | [ ] |
| 4 | Remove Duplicates | Easy | [ ] |
| 5 | Rotate Array | Medium | [ ] |
| 6 | Move Zeros | Easy | [ ] |
| 7 | Missing Number | Easy | [ ] |
| 8 | Max Consecutive Ones | Easy | [ ] |
| 9 | Single Number | Easy | [ ] |
| 10 | Kadane's Max Subarray | Medium | [ ] |
| 11 | Sort Colors | Medium | [ ] |
| 12 | Best Time to Buy Stock | Easy | [ ] |
| 13 | Product Except Self | Medium | [ ] |
| 14 | Trapping Rain Water | Hard | [ ] |

---

## My Notes
<!-- Add your own notes here as you learn -->
