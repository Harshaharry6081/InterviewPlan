# 📘 Day 1 — Core Java 8+ (May 2nd)

> Each item below has the **question** + **answer**. Check it off once you understand it.

---

## 🔷 JVM Internals

- [ ] **What is JVM vs JRE vs JDK?** — JVM: runs `.class` bytecode on any OS. JRE: JVM + standard libraries (to *run* Java). JDK: JRE + compiler (`javac`) + tools (to *develop* Java).

- [ ] **What is bytecode?** — Platform-independent intermediate code compiled from `.java` by `javac`. The JVM interprets/JIT-compiles it to native machine code at runtime.

- [ ] **What are the JVM memory zones?** — **Heap**: all objects; **Stack**: one per thread, holds method frames & local vars; **Metaspace**: class metadata; **Code Cache**: JIT-compiled native code; **PC Register**: current instruction pointer per thread.

- [ ] **What is Heap vs Stack?** — Heap: shared, stores objects, GC-managed, larger. Stack: per-thread, stores primitives & references, automatically freed when method returns, faster but smaller (StackOverflowError if too deep).

- [ ] **What is Garbage Collection?** — Automatic memory management. GC identifies objects with no references and frees their heap memory. You cannot explicitly free memory in Java (no `delete`).

- [ ] **Name 4 GC algorithms and when to use each.** — **Serial GC**: single-thread, small apps. **Parallel GC**: multi-thread, max throughput. **G1 GC** (default Java 9+): balanced latency & throughput, region-based. **ZGC/Shenandoah**: ultra-low pause (<10ms), Java 15+, large heaps.

- [ ] **What is OutOfMemoryError vs StackOverflowError?** — OOM: Heap is full, no space for new objects. StackOverflow: thread's call stack is too deep (usually infinite recursion).

- [ ] **What is a memory leak in Java?** — Objects are still referenced (so GC can't collect them) but never used again. Common cause: static collections holding objects, unclosed streams, cache without eviction.

---

## 🔷 Core Language

- [ ] **Difference between primitive types and reference types?** — Primitives (`int`, `boolean`, `char` etc.) store values directly on the stack. Reference types store a *pointer* to the object on the heap. Primitives have wrapper classes (`Integer`, `Boolean`).

- [ ] **What are all 8 primitive types?** — `byte`(1B), `short`(2B), `int`(4B), `long`(8B), `float`(4B), `double`(8B), `char`(2B), `boolean`(1bit).

- [ ] **What is autoboxing and unboxing? Pitfalls?** — Autoboxing: auto-converting `int` → `Integer`. Unboxing: `Integer` → `int`. Pitfall: `Integer a = null; int b = a;` throws NullPointerException. Also causes performance overhead in loops.

- [ ] **`==` vs `.equals()`?** — `==` compares **references** (memory addresses). `.equals()` compares **values**. For Strings always use `.equals()`. Example: `new String("hi") == new String("hi")` is `false`.

- [ ] **What is `hashCode()` and its contract with `equals()`?** — If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` MUST be true. Used by HashMap to find the right bucket. Always override both together.

- [ ] **Why is String immutable?** — Thread-safe (no synchronization needed for sharing), String Pool efficiency (can cache literals), security (can't modify after passing to methods), hashCode caching (stable key for HashMap).

- [ ] **What is the String Pool?** — A cache in the Heap for String literals. `String a = "hello"` reuses the pooled object. `new String("hello")` always creates a new object bypassing the pool. `intern()` puts a string into the pool.

- [ ] **String vs StringBuilder vs StringBuffer?** — `String`: immutable, creates new object on every concat. `StringBuilder`: mutable, not thread-safe, fast (use in single-thread loops). `StringBuffer`: mutable, thread-safe (synchronized), slower.

- [ ] **What is `final`, `finally`, `finalize`?** — `final`: prevents re-assignment (variable), overriding (method), inheritance (class). `finally`: block that always runs after try-catch. `finalize()`: deprecated GC hook called before object is collected — do NOT rely on it.

- [ ] **`abstract` class vs `interface`?** — Abstract class: can have state, constructors, concrete methods; use when classes share common implementation. Interface: all-abstract by default, supports multiple inheritance; use to define a contract. From Java 8: interfaces can have `default` and `static` methods.

- [ ] **Checked vs Unchecked exceptions?** — Checked: must be handled or declared (`IOException`, `SQLException`), signals recoverable conditions. Unchecked: extend `RuntimeException`, don't need to be declared (`NullPointerException`, `IllegalArgumentException`), signal programming errors.

- [ ] **What is try-with-resources?** — Ensures `AutoCloseable` resources (streams, connections) are closed automatically. `try (FileReader fr = new FileReader(f)) { ... }` — `fr.close()` is called even if exception occurs.

---

## 🔷 Java 8+ Features

- [ ] **What is a Lambda expression?** — Anonymous function: `(params) -> body`. Enables passing behaviour as data. Example: `list.sort((a, b) -> a.compareTo(b))`. Replaces anonymous inner classes for single-method interfaces.

- [ ] **What is a Functional Interface?** — Interface with exactly ONE abstract method (annotated `@FunctionalInterface`). The 4 key built-ins: `Function<T,R>` (transform), `Predicate<T>` (test/filter), `Consumer<T>` (side-effect/no return), `Supplier<T>` (produce/no input).

- [ ] **What is a Method Reference?** — Shorthand for a lambda that calls an existing method. 4 types: `String::toUpperCase` (instance method), `System.out::println` (specific instance), `String::new` (constructor), `Integer::parseInt` (static method).

- [ ] **What is `Optional<T>`?** — A container that may or may not hold a value. Prevents NPE by forcing callers to handle the empty case. Key methods: `of()`, `ofNullable()`, `isPresent()`, `orElse()`, `orElseGet()`, `map()`, `flatMap()`, `ifPresent()`.

- [ ] **Stream API: intermediate vs terminal operations?** — Intermediate: lazy, return a new Stream (`filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`). Terminal: trigger execution, return result (`collect`, `forEach`, `reduce`, `count`, `findFirst`, `anyMatch`).

- [ ] **`flatMap()` vs `map()`?** — `map()` transforms each element (1-to-1). `flatMap()` transforms each element to a Stream and flattens all streams into one (1-to-many). Use `flatMap` to flatten `List<List<T>>` to `List<T>`.

- [ ] **`Collectors.groupingBy()` example?** — `orders.stream().collect(Collectors.groupingBy(Order::getStatus))` → `Map<String, List<Order>>`. With downstream: `groupingBy(Order::getStatus, Collectors.counting())` → `Map<String, Long>`.

- [ ] **`Stream` vs `ParallelStream`?** — `parallelStream()` splits data across multiple CPU cores using ForkJoinPool. Fast for large datasets with CPU-intensive ops, BUT ordering is not guaranteed and has overhead for small datasets. Use carefully.

- [ ] **What is `CompletableFuture`?** — Non-blocking async programming. `supplyAsync()` runs in background. `thenApply()` transforms result. `thenCompose()` chains another async call. `allOf()` waits for multiple. `exceptionally()` handles errors. Unlike `Future.get()`, it doesn't block.

---

## 🔷 Collections Deep Dive

- [ ] **`ArrayList` vs `LinkedList`?** — ArrayList: backed by array, O(1) get-by-index, O(n) insert-in-middle. LinkedList: doubly-linked list, O(n) get-by-index, O(1) insert/remove at ends. **Use ArrayList by default** (better cache performance).

- [ ] **`HashMap` internals — how does `put()` work?** — 1) Compute `key.hashCode()`. 2) Apply hash function → bucket index. 3) If bucket empty, insert. 4) If collision, check `.equals()` — update if key exists, else append to linked list (TreeMap if >8 entries in Java 8+).

- [ ] **HashMap default capacity (16) and load factor (0.75)?** — When `size > capacity × 0.75` (i.e., 12 entries), HashMap **rehashes**: doubles capacity, recomputes all bucket indexes. This is expensive — set initial capacity if you know the size.

- [ ] **`HashMap` vs `Hashtable` vs `ConcurrentHashMap`?** — HashMap: not thread-safe, allows null keys/values. Hashtable: synchronized (whole map), legacy. ConcurrentHashMap: thread-safe via CAS + segment locks (no null keys/values), best for concurrent use.

- [ ] **`Comparable` vs `Comparator`?** — `Comparable`: implemented by the class itself (`compareTo`), defines natural ordering. `Comparator`: external, defines custom ordering. `Collections.sort(list, comparator)`. Java 8+: `Comparator.comparing(Employee::getSalary).reversed()`.

- [ ] **What is `PriorityQueue`?** — Min-heap by default (smallest element at head). Use `Collections.reverseOrder()` for max-heap. `add()` is O(log n), `poll()` (remove head) is O(log n), `peek()` is O(1). Elements must be `Comparable` or pass a `Comparator`.

---

## 🔷 Multithreading & Concurrency

- [ ] **Thread lifecycle states?** — NEW (created, not started) → RUNNABLE (running or ready) → BLOCKED (waiting for monitor lock) → WAITING (indefinitely waiting: `wait()`, `join()`) → TIMED_WAITING (`sleep(ms)`, `wait(ms)`) → TERMINATED.

- [ ] **`synchronized` keyword?** — Ensures only one thread executes the block/method at a time by acquiring the object's monitor lock. Method-level: `synchronized void foo()`. Block-level: `synchronized(this) { ... }` (more granular, preferred).

- [ ] **`volatile` keyword?** — Guarantees **visibility**: reads/writes go directly to main memory, not thread's local cache. Does NOT guarantee atomicity. Use for simple flags: `volatile boolean running = true`. For compound operations, use `AtomicInteger` or `synchronized`.

- [ ] **What is a deadlock?** — Thread A holds Lock 1, waits for Lock 2. Thread B holds Lock 2, waits for Lock 1. Both wait forever. Prevention: always acquire locks in the **same order**, use `tryLock()` with timeout.

- [ ] **`ExecutorService` — thread pool types?** — `newFixedThreadPool(n)`: fixed n threads. `newCachedThreadPool()`: grows/shrinks dynamically (careful with unbounded growth). `newSingleThreadExecutor()`: single thread, sequential. `newScheduledThreadPool(n)`: for delayed/periodic tasks.

- [ ] **`AtomicInteger` vs `synchronized`?** — `AtomicInteger` uses CAS (Compare-And-Swap) CPU instruction — lock-free, faster under low contention. `synchronized` uses OS-level mutex — better when holding lock for long operations or protecting complex state.

- [ ] **`CountDownLatch` vs `CyclicBarrier`?** — `CountDownLatch`: one-time latch, N threads count down, main thread waits at `await()`. **Cannot be reset**. `CyclicBarrier`: all N threads wait at `await()` until all arrive, then all proceed together. **Reusable**.

- [ ] **What is `ThreadLocal`?** — Provides each thread its own isolated copy of a variable. Common use: storing `HttpServletRequest` or DB connections per-thread. Risk: memory leaks in thread pools (always `remove()` after use).

---

## 📝 Key Code to Write from Memory

```java
// Streams: group orders by status and count
Map<String, Long> countByStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

// CompletableFuture: chain async calls with fallback
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenApply(user -> enrichWithOrders(user))
    .exceptionally(ex -> User.guest())
    .thenAccept(System.out::println);

// Custom Comparator: salary desc, then name asc
employees.sort(Comparator.comparingDouble(Employee::getSalary)
    .reversed().thenComparing(Employee::getName));

// AtomicInteger: thread-safe counter (no synchronized)
AtomicInteger counter = new AtomicInteger(0);
int current = counter.incrementAndGet();  // returns new value

// Optional: safe chain
String city = Optional.ofNullable(user)
    .map(User::getAddress)
    .map(Address::getCity)
    .orElse("Unknown");
```

---

## 🔗 Study References
- [enhorse/java-interview — OOP](https://github.com/enhorse/java-interview/blob/master/oop.md)
- [enhorse/java-interview — Collections](https://github.com/enhorse/java-interview/blob/master/collections.md)
- [enhorse/java-interview — Multithreading](https://github.com/enhorse/java-interview/blob/master/multithreading.md)
- [Baeldung — Java 8 Streams](https://www.baeldung.com/java-8-streams)
- [Baeldung — CompletableFuture](https://www.baeldung.com/java-completablefuture)
