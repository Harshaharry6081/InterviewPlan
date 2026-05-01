# 📘 Day 1 — Core Java 8+ (May 2nd)
> **Format:** Question → Detailed Answer → 🏭 Real-World Use → 💻 Code

---

## 🔷 JVM Internals

- [ ] **What is JVM vs JRE vs JDK?**

  **JVM** (Java Virtual Machine) — Executes compiled `.class` bytecode. It's what makes Java "write once, run anywhere" — the same bytecode runs on Windows, Linux, Mac.
  **JRE** (Java Runtime Environment) — JVM + Java standard libraries (`java.util`, `java.io` etc.). Needed to *run* Java apps.
  **JDK** (Java Development Kit) — JRE + compiler (`javac`) + tools (debugger, jar, javadoc). Needed to *develop* Java apps.

  🏭 **Real World:** Your Spring Boot app is compiled into a `.jar` file by the JDK. On the production server (Docker container), only the JRE/JVM is needed to run it. That's why Docker images use `FROM eclipse-temurin:17-jre` (not `jdk`) to keep images smaller.

---

- [ ] **What are the JVM memory zones?**

  | Zone | What it stores | Managed by |
  |---|---|---|
  | **Heap** | All objects (`new Order()`, `new ArrayList()`) | Garbage Collector |
  | **Stack** | One per thread. Method calls, local variables, primitives | Auto-freed when method returns |
  | **Metaspace** | Class metadata (class names, methods, fields) | GC (since Java 8, replaced PermGen) |
  | **Code Cache** | JIT-compiled native machine code | JVM |
  | **PC Register** | Current instruction pointer for each thread | JVM |

  🏭 **Real World:** You get `OutOfMemoryError: Java heap space` when your Spring Boot app creates too many objects (e.g., loading 1 million DB records without pagination). Fix: add `@PageableDefault(size=20)` to your controller, or increase heap with `-Xmx512m`.

  You get `StackOverflowError` when you accidentally create infinite recursion — e.g., a `toString()` method that calls itself.

---

- [ ] **What is Garbage Collection? Name 4 GC types.**

  GC automatically finds and frees memory for objects with **no references**. You don't `delete` in Java — GC does it.

  | GC | Best For | How |
  |---|---|---|
  | **Serial GC** | Small apps, single-core | Single thread, stop-the-world |
  | **Parallel GC** | Batch processing, max throughput | Multiple threads, stop-the-world |
  | **G1 GC** (Java 9+ default) | Web apps (balanced) | Divides heap into regions, concurrent |
  | **ZGC / Shenandoah** | Low-latency APIs (<10ms pause) | Concurrent compaction |

  🏭 **Real World:** For your Accenture Spring Boot microservice handling REST APIs, G1 GC is fine. If it were a real-time trading platform needing sub-millisecond response, you'd use ZGC with `-XX:+UseZGC`.

---

## 🔷 Core Language

- [ ] **Why is String immutable? What does this give us?**

  Once a `String` is created, its value **cannot be changed**. Any operation like `str + "hello"` creates a **new** String object, not modify the existing one.

  **Benefits:**
  1. **Thread-safe** — Multiple threads can share the same String without synchronization
  2. **String Pool** — JVM can cache String literals and reuse them (saves memory)
  3. **Stable HashMap keys** — hashCode is cached and never changes
  4. **Security** — Database passwords passed as String can't be modified by malicious code mid-flight

  🏭 **Real World:** In Spring Boot, configuration values (`@Value("${db.url}")`) are Strings. They're safely shared across all threads handling concurrent HTTP requests without locks.

  ```java
  // ❌ This creates 1000 new String objects in memory!
  String result = "";
  for (int i = 0; i < 1000; i++) result += i;

  // ✅ Use StringBuilder — mutable, single object
  StringBuilder sb = new StringBuilder();
  for (int i = 0; i < 1000; i++) sb.append(i);
  String result = sb.toString();
  ```

---

- [ ] **What is `==` vs `.equals()`?**

  `==` compares **memory addresses** (are these the same object in RAM?).
  `.equals()` compares **values** (do these objects represent the same thing?).

  ```java
  String a = new String("hello");
  String b = new String("hello");
  
  System.out.println(a == b);        // false — different objects in heap
  System.out.println(a.equals(b));   // true  — same characters
  
  // String pool example:
  String c = "hello";
  String d = "hello";
  System.out.println(c == d);        // true! — both point to same pool object
  ```

  🏭 **Real World:** Classic Spring Boot bug — comparing status strings in a service:
  ```java
  // ❌ WRONG — will work sometimes (pool), fail other times (new object from DB)
  if (order.getStatus() == "COMPLETED") { ... }

  // ✅ CORRECT — always works
  if ("COMPLETED".equals(order.getStatus())) { ... }
  // Note: put literal first to avoid NPE if status is null
  ```

---

- [ ] **What is `final`, `finally`, `finalize`? (Classic trap question!)**

  **`final`** — Prevents change:
  - `final int x = 5;` → can't reassign x
  - `final void process()` → can't override in subclass
  - `final class String` → can't extend String (that's why String is immutable!)

  **`finally`** — Block that **always runs** after try-catch, even if exception thrown or `return` called. Used for cleanup.

  **`finalize()`** — Deprecated! GC called this before deleting an object. **Never rely on it** — GC timing is unpredictable.

  ```java
  // finally always runs — used for guaranteed cleanup
  Connection conn = null;
  try {
      conn = dataSource.getConnection();
      // do DB work
  } catch (SQLException e) {
      log.error("DB error", e);
  } finally {
      if (conn != null) conn.close(); // always closes, even if exception
  }

  // Modern way: try-with-resources (preferred in Spring Boot)
  try (Connection conn = dataSource.getConnection()) {
      // conn.close() called automatically
  }
  ```

  🏭 **Real World:** Spring's `JdbcTemplate` uses try-finally internally to guarantee DB connections are returned to the pool (HikariCP). You never have to manage it manually in Spring.

---

- [ ] **`abstract` class vs `interface` — when to use each?**

  | | Abstract Class | Interface |
  |---|---|---|
  | Can have state (fields) | ✅ Yes | ❌ No (only constants) |
  | Can have constructors | ✅ Yes | ❌ No |
  | Multiple inheritance | ❌ One only | ✅ Multiple |
  | Default methods (Java 8+) | ✅ Yes | ✅ Yes |
  | Best for | Shared implementation | Defining a contract |

  🏭 **Real World in Spring Boot:**

  ```java
  // Interface — defines WHAT (contract), not HOW
  public interface PaymentService {
      PaymentResult process(PaymentRequest request);
  }

  // Implementations — different HOW for same WHAT
  @Service("stripe")
  public class StripePaymentService implements PaymentService { ... }

  @Service("razorpay")
  public class RazorpayPaymentService implements PaymentService { ... }

  // Abstract class — shared logic between related classes
  public abstract class BaseNotificationService {
      private final EmailClient emailClient;  // shared state
      
      protected abstract String buildMessage(Event event); // each subclass implements
      
      public void notify(Event event) {
          emailClient.send(buildMessage(event)); // shared implementation
      }
  }
  ```

---

## 🔷 Java 8+ Features

- [ ] **What is the Stream API? What are intermediate vs terminal operations?**

  Streams let you process collections **declaratively** (what to do) vs **imperatively** (how to do it). They're lazy — no work happens until a terminal operation is called.

  **Intermediate** (lazy, returns Stream): `filter()`, `map()`, `flatMap()`, `sorted()`, `distinct()`, `limit()`, `peek()`
  **Terminal** (triggers execution): `collect()`, `forEach()`, `reduce()`, `count()`, `findFirst()`, `anyMatch()`, `min()`, `max()`

  ```java
  // Real Spring Boot scenario: Process orders from DB
  List<Order> orders = orderRepository.findAll();

  // Get top 5 completed orders above ₹500, sorted by amount desc
  List<OrderDTO> result = orders.stream()
      .filter(o -> "COMPLETED".equals(o.getStatus()))      // intermediate
      .filter(o -> o.getAmount() > 500)                    // intermediate
      .sorted(Comparator.comparingDouble(Order::getAmount).reversed()) // intermediate
      .limit(5)                                             // intermediate
      .map(o -> new OrderDTO(o.getId(), o.getAmount()))    // intermediate
      .collect(Collectors.toList());                        // TERMINAL — executes everything
  ```

  🏭 **Real World:** In your Accenture project, instead of writing 3 for-loops to filter/transform/sort a list from the DB, one stream chain does it all. Interviewers love asking you to replace a nested for-loop with streams.

---

- [ ] **`map()` vs `flatMap()` — what's the difference?**

  `map()` → one input produces **one output** (1:1 transformation)
  `flatMap()` → one input produces **many outputs** which are flattened into one stream (1:N, then flatten)

  ```java
  // map() — transform each order to its ID (1:1)
  List<Long> orderIds = orders.stream()
      .map(Order::getId)
      .collect(Collectors.toList());

  // flatMap() — each order has multiple items, get ALL items from ALL orders (1:N)
  List<OrderItem> allItems = orders.stream()
      .flatMap(order -> order.getItems().stream()) // each order → many items
      .collect(Collectors.toList());

  // Another example: split sentences into words
  List<String> sentences = List.of("Hello World", "Java Streams");
  List<String> words = sentences.stream()
      .flatMap(s -> Arrays.stream(s.split(" ")))
      .collect(Collectors.toList());
  // Result: ["Hello", "World", "Java", "Streams"]
  ```

---

- [ ] **`Collectors.groupingBy()` — how does it work?**

  Groups stream elements into a `Map` by a classifier function. Most useful for analytics/reporting.

  ```java
  // Basic: group orders by status
  Map<String, List<Order>> byStatus = orders.stream()
      .collect(Collectors.groupingBy(Order::getStatus));
  // {"PENDING": [order1, order3], "COMPLETED": [order2, order4]}

  // Count per status
  Map<String, Long> countByStatus = orders.stream()
      .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
  // {"PENDING": 2, "COMPLETED": 2}

  // Sum of amount per customer
  Map<Long, Double> totalByCustomer = orders.stream()
      .collect(Collectors.groupingBy(
          Order::getCustomerId,
          Collectors.summingDouble(Order::getAmount)
      ));
  ```

  🏭 **Real World:** Building a dashboard endpoint that shows order stats per status, or calculating total revenue per customer — both done with `groupingBy` instead of writing SQL aggregate queries for every variation.

---

- [ ] **What is `CompletableFuture`? When do you use it?**

  Makes async programming readable. Unlike `Future.get()` which **blocks** the thread, `CompletableFuture` lets you define what to do *when* the result arrives, without blocking.

  ```java
  // PROBLEM: Call 3 external APIs. Sequentially = 900ms total
  UserProfile user = userApi.getUser(id);      // 300ms
  List<Order> orders = orderApi.getOrders(id); // 300ms
  CreditScore credit = creditApi.getScore(id); // 300ms

  // SOLUTION: Call all 3 in parallel = ~300ms total
  CompletableFuture<UserProfile> userFuture =
      CompletableFuture.supplyAsync(() -> userApi.getUser(id));
  
  CompletableFuture<List<Order>> ordersFuture =
      CompletableFuture.supplyAsync(() -> orderApi.getOrders(id));
  
  CompletableFuture<CreditScore> creditFuture =
      CompletableFuture.supplyAsync(() -> creditApi.getScore(id));

  // Wait for all 3 to complete
  CompletableFuture.allOf(userFuture, ordersFuture, creditFuture).join();

  UserProfile user = userFuture.get();
  List<Order> orders = ordersFuture.get();

  // Chain operations:
  CompletableFuture.supplyAsync(() -> fetchOrder(id))
      .thenApply(order -> applyDiscount(order))   // transform result
      .thenApply(order -> toDTO(order))
      .exceptionally(ex -> OrderDTO.empty())       // fallback on error
      .thenAccept(dto -> log.info("Done: {}", dto));
  ```

  🏭 **Real World:** Any Spring Boot microservice that needs to call multiple downstream services (User service + Inventory service + Payment service) should call them in parallel using `CompletableFuture` to reduce API response time.

---

## 🔷 Collections Deep Dive

- [ ] **How does `HashMap` work internally?**

  `HashMap` is a **array of linked lists** (or trees in Java 8+).

  **How `put("key", value)` works:**
  1. Compute `"key".hashCode()` → e.g., 1234567
  2. `bucketIndex = hashCode % arrayLength` (e.g., 1234567 % 16 = 7)
  3. Go to bucket[7]:
     - **Empty?** → Insert directly
     - **Has entries?** → Walk the list, check `.equals()`. Update if key exists, append if new
  4. If bucket list grows > 8 entries → convert to **Red-Black Tree** (O(log n) instead of O(n))
  5. If total entries > `capacity × 0.75` (load factor) → **resize to double** (expensive rehash!)

  ```java
  Map<String, Order> cache = new HashMap<>(1000); // pre-size if you know approximate count
  cache.put("order-123", order);
  Order o = cache.get("order-123"); // O(1) average

  // GOTCHA: custom objects as keys MUST override both hashCode AND equals
  public class OrderKey {
      private Long id;
      
      @Override
      public int hashCode() { return Objects.hash(id); }
      
      @Override
      public boolean equals(Object o) {
          if (!(o instanceof OrderKey)) return false;
          return this.id.equals(((OrderKey)o).id);
      }
  }
  ```

  🏭 **Real World:** Redis is essentially a distributed HashMap. Spring's `@Cacheable` stores method results in an in-memory HashMap (or Redis). Understanding HashMap internals helps you understand why cache keys need proper `hashCode`/`equals`.

---

- [ ] **`HashMap` vs `ConcurrentHashMap` — which to use when?**

  `HashMap` — **not thread-safe**. Two threads writing simultaneously can corrupt the map (data loss, infinite loop during resize in Java 7!).

  `ConcurrentHashMap` — **thread-safe without locking the entire map**. Uses CAS (Compare-And-Swap) operations and locks only the individual bucket being written to. Much better throughput than synchronized `Hashtable`.

  ```java
  // WRONG for multi-threaded Spring Bean (singleton scope!)
  @Service
  public class ProductCacheService {
      private Map<Long, Product> cache = new HashMap<>(); // ❌ Not thread-safe!
      
      public Product getProduct(Long id) {
          return cache.get(id); // concurrent reads+writes = data corruption
      }
  }

  // CORRECT
  @Service
  public class ProductCacheService {
      private Map<Long, Product> cache = new ConcurrentHashMap<>(); // ✅
      // OR better: use Spring's @Cacheable with Redis
  }
  ```

  🏭 **Real World:** Spring Boot singleton beans are shared across all threads (one bean, many HTTP requests). Any mutable state in a singleton (like a cache map) **must** use `ConcurrentHashMap` or be managed by Spring's cache abstraction.

---

## 🔷 Multithreading & Concurrency

- [ ] **What is `volatile` and when is it not enough?**

  CPUs have **local caches** per thread. Thread A writes `running = false`, but Thread B may still read the old cached `true`. `volatile` forces reads/writes to go to **main memory**, ensuring all threads see the latest value.

  BUT `volatile` only guarantees **visibility**, NOT **atomicity**. `counter++` is 3 operations (read, increment, write) and `volatile` doesn't make those 3 atomic together.

  ```java
  // ✅ volatile is enough for a simple flag
  private volatile boolean running = true;

  public void stop() { running = false; }              // Thread A
  public void run() { while (running) { doWork(); } }  // Thread B sees update immediately

  // ❌ volatile NOT enough for increment
  private volatile int counter = 0;
  counter++; // still a race condition! read(0), read(0), write(1), write(1) = lost update

  // ✅ Use AtomicInteger for counters
  private AtomicInteger counter = new AtomicInteger(0);
  counter.incrementAndGet(); // single atomic CPU instruction (CAS)
  ```

  🏭 **Real World:** In a Spring Boot app, you might have a `volatile boolean` flag to indicate the application is shutting down. For request counting or rate limiting, use `AtomicInteger` or `AtomicLong`.

---

- [ ] **What is `ExecutorService`? Why not use raw threads?**

  Creating raw threads is expensive (allocates OS thread, ~1MB stack). `ExecutorService` manages a **thread pool** — reuses threads instead of creating new ones.

  ```java
  // ❌ Raw thread — creates new OS thread for every task (expensive)
  new Thread(() -> processOrder(order)).start();

  // ✅ Thread pool — reuses threads
  ExecutorService pool = Executors.newFixedThreadPool(10); // 10 worker threads

  // Submit tasks to the pool
  Future<OrderResult> future = pool.submit(() -> processOrder(order));
  OrderResult result = future.get(); // blocks until done

  // In Spring Boot — use @Async instead (Spring manages the pool)
  @Async("taskExecutor")
  public CompletableFuture<OrderResult> processOrderAsync(Order order) {
      return CompletableFuture.completedFuture(processOrder(order));
  }
  ```

  🏭 **Real World:** Sending email notifications after an order is placed. Instead of blocking the HTTP response thread for 2 seconds while the email sends, use `@Async` to fire-and-forget. The API returns `200 OK` immediately, email sends in the background.

---

## 🔗 Study References
- [enhorse/java-interview — Full Java Q&A](https://github.com/enhorse/java-interview)
- [Baeldung — Java 8 Streams Guide](https://www.baeldung.com/java-8-streams)
- [Baeldung — Guide to CompletableFuture](https://www.baeldung.com/java-completablefuture)
- [Baeldung — Java HashMap](https://www.baeldung.com/java-hashmap)
