# 📘 Day 1 – Core Java 8+ (May 2nd)

> **Goal:** Solidify your Java fundamentals. These are asked in almost every round.

---

## ✅ Checklist
- [ ] Java 8 Features (Streams, Lambdas, Optional)
- [ ] Collections deep-dive (HashMap internals)
- [ ] Concurrency (ExecutorService, CompletableFuture)
- [ ] JVM Internals (GC, Memory Zones)
- [ ] String Pool, Immutability

---

## 1. Java 8+ Features

### Lambdas & Functional Interfaces
```java
// Before Java 8
Runnable r1 = new Runnable() {
    public void run() { System.out.println("Old way"); }
};

// Java 8+ Lambda
Runnable r2 = () -> System.out.println("Lambda way");

// Custom Functional Interface
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}
Calculator add = (a, b) -> a + b;
System.out.println(add.calculate(5, 3)); // 8
```

### Streams API
```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6);

// Filter even, square them, collect to list
List<Integer> result = nums.stream()
    .filter(n -> n % 2 == 0)         // [2, 4, 6]
    .map(n -> n * n)                  // [4, 16, 36]
    .collect(Collectors.toList());

// Find sum using reduce
int sum = nums.stream().reduce(0, Integer::sum);

// groupingBy
Map<Boolean, List<Integer>> grouped = nums.stream()
    .collect(Collectors.groupingBy(n -> n % 2 == 0));
```

### Optional — Avoid NullPointerException
```java
Optional<String> name = Optional.ofNullable(getNameFromDB());

// BAD: name.get() without isPresent check
// GOOD:
String result = name
    .map(String::toUpperCase)
    .orElse("UNKNOWN");

name.ifPresent(n -> System.out.println("Hello, " + n));
```

---

## 2. Collections — HashMap Internals

**How HashMap works:**
1. `put(key, value)` → calls `key.hashCode()` → determines bucket index
2. If two keys have the same hash → **collision** → stored as LinkedList (Java 8+: TreeMap if > 8 entries)
3. `get(key)` → compute hash → find bucket → check `equals()` to find exact entry

**Interview Questions:**
- What is the default initial capacity? → **16**
- What is the load factor? → **0.75** (resize at 12 elements)
- What happens on resize? → **Rehashing** (doubles capacity, recomputes bucket indexes)
- `HashMap` vs `Hashtable` → Hashtable is synchronized, HashMap is not
- `HashMap` vs `ConcurrentHashMap` → ConcurrentHashMap uses segment-level locking (Java 8: CAS operations)

---

## 3. Concurrency

### ExecutorService
```java
ExecutorService executor = Executors.newFixedThreadPool(4);

// Submit a task
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(1000);
    return 42;
});

Integer result = future.get(); // Blocks until done
executor.shutdown();
```

### CompletableFuture (Non-blocking async)
```java
CompletableFuture<String> cf = CompletableFuture
    .supplyAsync(() -> fetchFromDB())           // runs async
    .thenApply(data -> transform(data))          // process result
    .thenCompose(data -> callAnotherAPI(data))   // chain async
    .exceptionally(ex -> "Fallback Value");      // handle errors

// Combine two futures
CompletableFuture<String> combined = CompletableFuture
    .allOf(future1, future2)
    .thenApply(v -> future1.join() + future2.join());
```

---

## 4. JVM Memory Model

| Zone           | What's Stored                          |
|----------------|----------------------------------------|
| **Heap**       | Objects (Eden, Survivor, Old Gen)      |
| **Stack**      | Method frames, local variables          |
| **Metaspace**  | Class metadata (replaced PermGen)       |
| **Code Cache** | JIT compiled bytecode                  |

**GC Algorithms (know these):**
- **Serial GC** → Single-threaded, small apps
- **Parallel GC** → Multi-threaded, throughput-focused
- **G1 GC** → Default since Java 9, low-pause, region-based
- **ZGC / Shenandoah** → Ultra-low pause (<10ms), Java 15+

---

## 5. Key Interview Q&A

| Question | Answer |
|---|---|
| String vs StringBuilder? | String is immutable, StringBuilder is mutable. Use StringBuilder for loops |
| `==` vs `.equals()`? | `==` checks reference, `.equals()` checks value |
| What is String Pool? | Literal strings are cached in the String Pool in the Heap |
| Checked vs Unchecked exceptions? | Checked = must handle (IOException), Unchecked = runtime (NPE, IAE) |
| `final`, `finally`, `finalize`? | final=variable/method/class lock, finally=runs after try-catch, finalize=GC hook (deprecated) |
| What are method references? | Shorthand for lambdas: `String::toUpperCase` = `s -> s.toUpperCase()` |
