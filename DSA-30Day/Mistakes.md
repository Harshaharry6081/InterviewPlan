# ❌ Mistakes Journal

> Track every mistake here. This is your most valuable file.

---

## How to use this file

When you get a problem wrong, note:
1. What you tried
2. Why it failed
3. The correct approach
4. The pattern to remember

---

## Template

```
### Problem: [Problem Name]
**Date**: 
**LeetCode**: 
**Pattern**: 

**What I tried**:
...

**Why it failed**:
...

**Correct Approach**:
...

**Key Pattern to Remember**:
...

**Java Pitfall**:
...
```

---

## Common Java Mistakes to Watch

| Mistake | Fix |
|---------|-----|
| `int` overflow in sum | Use `long` |
| `Integer.MAX_VALUE + 1` overflow | Cast to `long` |
| ConcurrentModificationException | Don't modify list while iterating |
| HashMap null key exception | Check null before get |
| Stack vs Deque | Use `Deque<Integer> stack = new ArrayDeque<>()` (not `Stack` class) |
| Off-by-one in binary search | Carefully define `lo`, `hi`, `mid` |
| Forgetting base case in recursion | Always define base case first |
| Not resetting visited array | Reset or create fresh for each test |

---

## My Mistakes Log

<!-- Add mistakes below as you encounter them -->

