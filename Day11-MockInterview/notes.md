# 📘 Day 11 – Mock Interview & Final Review (May 12th)

> **Goal:** Simulate a real interview and identify weak spots before tomorrow.

---

## ✅ Checklist
- [ ] Complete a full mock interview (1 hour)
- [ ] Review your own project (be ready to deep-dive)
- [ ] Read through all your cheatsheets
- [ ] Prepare your setup (camera, quiet room, stable internet)
- [ ] Get 8 hours of sleep!

---

## Mock Interview Script (Ask a friend, or answer out loud to yourself)

### Round 1: Technical Screening (15 min)

1. Walk me through your experience with Spring Boot.
2. What is the difference between `@Service` and `@Repository`?
3. Explain how `@Transactional` works.
4. What is the N+1 problem and how do you fix it?
5. What HTTP status code do you return when creating a resource?
6. How is `HashMap` different from `ConcurrentHashMap`?

### Round 2: System Design (20 min)

**Design a URL Shortener (like bit.ly)**

Think out loud:
- **API**: `POST /shorten` (returns short URL), `GET /{code}` (redirects)
- **Database**: Store mapping in PostgreSQL. Unique index on short_code.
- **Short Code Generation**: MD5 hash of URL (take first 7 chars), or auto-increment ID converted to Base62
- **Caching**: Redis to cache code→URL mapping (high read, low write). TTL = 24h
- **Scale**: 
  - 100M URLs: partitioned DB
  - 10B clicks/day: CDN edge caching for redirect responses
- **HA**: Multiple app instances behind load balancer, DB read replicas

### Round 3: Coding (15 min)

**Problem:** Given a list of integers, find all pairs that sum to a target value.

```java
// O(n) time, O(n) space using HashSet
public List<int[]> findPairs(int[] nums, int target) {
    Set<Integer> seen = new HashSet<>();
    List<int[]> result = new ArrayList<>();

    for (int num : nums) {
        int complement = target - num;
        if (seen.contains(complement)) {
            result.add(new int[]{complement, num});
        }
        seen.add(num);
    }
    return result;
}

// Test cases to mention:
// [1,2,3,4,5], target=6 → [1,5], [2,4]
// [], target=5 → []
// [3,3], target=6 → [3,3]
```

**Problem:** Reverse words in a sentence.

```java
public String reverseWords(String sentence) {
    String[] words = sentence.trim().split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (int i = words.length - 1; i >= 0; i--) {
        sb.append(words[i]);
        if (i > 0) sb.append(" ");
    }
    return sb.toString();
}
// "Hello World Java" → "Java World Hello"
```

---

## Final Checklist Before the Interview

### Night Before (May 12th)
- [ ] Charge your laptop
- [ ] Test your camera and microphone
- [ ] Find a quiet room with good lighting
- [ ] Have water nearby
- [ ] Review the company's tech stack (check their job listings/website)
- [ ] Sleep by 10:30 PM

### Day Of (May 13th)
- [ ] Wake up at a comfortable time (1.5-2 hours before)
- [ ] Light breakfast
- [ ] Review your Day 1 cheatsheet (Java basics) — quick refresh
- [ ] Join the call 5 minutes early
- [ ] Have pen and paper ready for diagrams

---

## Interview Tips

1. **Think out loud.** Interviewers want to see your thought process, not just the answer.
2. **Ask clarifying questions** before coding: "Should I handle null inputs?" "What's the expected scale?"
3. **If you don't know**, say: "I don't have that memorized, but my approach would be..." — shows problem-solving.
4. **Pace yourself.** Don't rush. A correct, clear answer is better than a fast, wrong one.
5. **For every technical answer**, try to connect it to a real example: "In my previous project, we used X because..."

---

## Confidence Boosters

> You have 3-6 years of experience. You have **lived through** these concepts in real systems.
>
> The interviewer is not looking for a human encyclopedia. They want to see:
> - Clear communication
> - Structured thinking
> - Awareness of trade-offs
> - Genuine enthusiasm for engineering
>
> **You are ready. Good luck! 🚀**
