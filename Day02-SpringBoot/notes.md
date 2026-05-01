# 📘 Day 2 — Spring Boot (May 3rd)
> **Format:** Question → Detailed Answer → 🏭 Real-World Use → 💻 Code

---

## 🔷 Spring Core — IoC & Dependency Injection

- [ ] **What is Inversion of Control (IoC)?**

  Normally *your code* creates objects: `OrderService service = new OrderService(new OrderRepository())`. IoC flips this — the **Spring container** creates and wires objects for you. You just declare what you need.

  This means your classes don't know *how* their dependencies are created — they just receive them. This makes swapping implementations (e.g., real DB vs mock) trivial for testing.

  🏭 **Real World:** In your Accenture Spring Boot microservice, you never write `new` for service or repository classes. Spring creates them as singletons, injects them wherever needed, and manages their lifecycle. This is why Spring apps are so testable — you can inject mock dependencies in unit tests.

---

- [ ] **What are the 3 types of Dependency Injection? Which is best?**

  ```java
  // 1. ❌ Field Injection — most common but WORST
  @Service
  public class OrderService {
      @Autowired
      private OrderRepository repo; // hidden dependency, breaks testability
  }

  // 2. ⚠️ Setter Injection — for optional dependencies
  @Service
  public class OrderService {
      private OrderRepository repo;
      
      @Autowired
      public void setRepo(OrderRepository repo) { this.repo = repo; }
  }

  // 3. ✅ Constructor Injection — BEST (used by Spring Boot + Lombok)
  @Service
  @RequiredArgsConstructor  // Lombok generates the constructor
  public class OrderService {
      private final OrderRepository repo;     // final = guaranteed, immutable
      private final EmailService emailService;
      // Lombok generates: public OrderService(OrderRepository repo, EmailService email) {...}
  }
  ```

  **Why constructor injection wins:**
  - Dependencies are `final` — immutable after creation
  - Makes dependencies visible and explicit
  - Easy to test: `new OrderService(mockRepo, mockEmailService)`
  - No Spring needed for unit tests!

  🏭 **Real World:** Accenture's Spring Boot codebase likely follows `@RequiredArgsConstructor` + constructor injection as the standard. This is the modern Spring recommendation.

---

- [ ] **What are Bean Scopes? Explain singleton vs prototype.**

  | Scope | When | One Instance Per |
  |---|---|---|
  | `singleton` (default) | Always | **Spring Application Context** |
  | `prototype` | On request | **Each time bean is requested** |
  | `request` | Web apps | **Each HTTP Request** |
  | `session` | Web apps | **Each HTTP Session** |

  ```java
  // Singleton (default) — same object shared across all HTTP requests
  @Service // → @Scope("singleton") by default
  public class OrderService { ... } // one instance, all threads share it

  // Prototype — new instance every time
  @Component
  @Scope("prototype")
  public class ReportGenerator { // new instance per use (stateful, not thread-safe)
      private List<String> rows = new ArrayList<>(); // safe! each call gets fresh list
  }
  ```

  🏭 **Real World:** `singleton` is correct for **stateless services** (no instance variables that change per-request). If a bean holds per-request state (like a report builder), it must be `prototype`. Mixing these up causes bugs where one user's data leaks into another user's response!

  ⚠️ **Classic bug:** Adding a `List` field to a `@Service` (singleton) to accumulate request data — all 1000 concurrent users write to the same list. Fix: use `prototype` scope or pass data as method parameters.

---

- [ ] **How does Spring Boot Auto-Configuration work?**

  When your app starts, Spring Boot reads:
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

  This file lists hundreds of auto-config classes. Each uses `@Conditional` annotations to decide whether to activate:

  ```java
  // Spring's DataSourceAutoConfiguration (simplified)
  @Configuration
  @ConditionalOnClass(DataSource.class)          // only if DataSource class is on classpath
  @ConditionalOnMissingBean(DataSource.class)    // only if YOU haven't defined your own
  @EnableConfigurationProperties(DataSourceProperties.class)
  public class DataSourceAutoConfiguration {
      @Bean
      public DataSource dataSource(DataSourceProperties props) {
          return props.initializeDataSourceBuilder().build(); // creates HikariCP pool
      }
  }
  ```

  🏭 **Real World:** Add `spring-boot-starter-data-jpa` to your `pom.xml` → Spring auto-configures Hibernate, EntityManagerFactory, TransactionManager. Add `spring-boot-starter-redis` → auto-configures RedisTemplate. You write **zero configuration** for these. That's the magic of auto-config.

  ```yaml
  # application.yml — all you need for DB config
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/mydb
      username: user
      password: pass
    jpa:
      hibernate:
        ddl-auto: validate
  ```

---

- [ ] **What is `@Transactional`? Where should it go and why?**

  `@Transactional` wraps a method in a **database transaction**. If the method completes normally → `COMMIT`. If an unchecked exception is thrown → `ROLLBACK`. Spring manages this via AOP proxy.

  ```java
  // ✅ CORRECT — on service layer
  @Service
  public class OrderService {

      @Transactional  // ← service layer is the right place
      public Order createOrder(CreateOrderRequest req) {
          Order order = orderRepo.save(new Order(req));    // DB write 1
          inventoryRepo.decrementStock(req.getProductId()); // DB write 2
          // If EITHER fails → BOTH are rolled back. Atomicity!
          return order;
      }

      @Transactional(readOnly = true) // ← optimization for reads
      public List<Order> getOrdersByCustomer(Long customerId) {
          return orderRepo.findByCustomerId(customerId);
          // Hibernate skips dirty checking — faster!
      }
  }
  ```

  🏭 **Real World:** E-commerce order creation: write to `orders` table AND decrement `inventory` table. Both must succeed or both must fail. Without `@Transactional`, if the server crashes between the two writes, you'd have an order with no inventory deduction (overselling!).

---

- [ ] **What is the self-invocation problem with `@Transactional`?**

  Spring's `@Transactional` works through a **proxy object** wrapping your bean. External calls go through the proxy (transaction applied). Internal calls (same class calling itself) **bypass the proxy** — no transaction!

  ```java
  @Service
  public class OrderService {

      // ❌ BROKEN — internal call bypasses proxy, no transaction on createOrder!
      public void processOrders(List<CreateOrderRequest> requests) {
          for (var req : requests) {
              createOrder(req); // Direct call — skips Spring's proxy!
          }
      }

      @Transactional
      public Order createOrder(CreateOrderRequest req) { ... }
  }

  // ✅ FIX Option 1: Inject self
  @Service
  public class OrderService {
      @Lazy @Autowired
      private OrderService self; // inject own proxy

      public void processOrders(List<CreateOrderRequest> requests) {
          for (var req : requests) {
              self.createOrder(req); // Goes through proxy ✅
          }
      }
  }

  // ✅ FIX Option 2: Move to separate class (cleaner)
  @Service
  public class OrderProcessingService {
      @Autowired private OrderService orderService;

      public void processOrders(List<CreateOrderRequest> requests) {
          requests.forEach(orderService::createOrder); // external call ✅
      }
  }
  ```

  🏭 **Real World:** This is one of the top interview questions at companies like Accenture/TCS because it's a subtle bug that causes production issues. Interviewers often ask: "Your transaction isn't rolling back, why?"

---

- [ ] **What is the N+1 problem in JPA? How do you fix it?**

  You fetch **1** query for N orders, then execute **N** separate queries to fetch each order's items.
  Total = N+1 database round trips. For 1000 orders = 1001 queries. 🐢

  ```java
  // ❌ N+1 problem setup
  @Entity
  public class Order {
      @OneToMany(mappedBy = "order", fetch = FetchType.LAZY) // LAZY = query per access
      private List<OrderItem> items;
  }

  // This triggers N+1:
  List<Order> orders = orderRepo.findAll();           // Query 1: SELECT * FROM orders
  orders.forEach(o -> System.out.println(o.getItems())); // N queries: SELECT * FROM items WHERE order_id=?

  // ✅ FIX 1: JOIN FETCH in JPQL
  @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items")
  List<Order> findAllWithItems();
  // ONE query: SELECT orders.*, items.* FROM orders JOIN items ON ...

  // ✅ FIX 2: @EntityGraph (no JPQL needed)
  @EntityGraph(attributePaths = {"items", "customer"})
  List<Order> findByStatus(String status);

  // ✅ FIX 3: @BatchSize (for large collections)
  @OneToMany
  @BatchSize(size = 50) // fetches items for 50 orders in one query
  private List<OrderItem> items;
  ```

  🏭 **Real World:** This is the #1 Hibernate performance issue. Running `SHOW SQL` on a production app and seeing 1001 queries for what should be 1 is a classic N+1. Enable `spring.jpa.show-sql=true` locally and watch for repeated queries.

---

- [ ] **What are `@Transactional` Propagation types? (REQUIRED vs REQUIRES_NEW)**

  Propagation controls what happens when a transactional method **calls another** transactional method:

  | Propagation | Behavior | Use Case |
  |---|---|---|
  | `REQUIRED` (default) | Join existing tx, or create new one | Normal service methods |
  | `REQUIRES_NEW` | **Suspend** existing tx, create fresh one | Audit logs (must save even if main tx rolls back) |
  | `SUPPORTS` | Join if exists, else no tx | Read methods |
  | `NEVER` | Throw exception if in a tx | Background jobs |
  | `NOT_SUPPORTED` | Suspend tx, run without | Batch reads |

  ```java
  @Service
  public class OrderService {
      @Autowired private AuditService auditService;

      @Transactional
      public Order createOrder(CreateOrderRequest req) {
          Order order = orderRepo.save(new Order(req));
          
          auditService.log("ORDER_CREATED", order.getId()); // saves in NEW transaction
          
          throw new RuntimeException("Simulated failure"); // main tx ROLLS BACK
          // But audit log IS saved (REQUIRES_NEW committed before exception)
      }
  }

  @Service
  public class AuditService {
      @Transactional(propagation = Propagation.REQUIRES_NEW)
      public void log(String action, Long entityId) {
          auditRepo.save(new AuditLog(action, entityId, Instant.now()));
          // Commits immediately in its own transaction
      }
  }
  ```

  🏭 **Real World:** Audit logging is the classic use case. Regulatory systems (banking, healthcare) must record every action even if the main operation fails.

---

## 🔷 Spring MVC & REST

- [ ] **How does global exception handling work with `@RestControllerAdvice`?**

  Without global handling, every controller method would need try-catch. `@RestControllerAdvice` centralizes all exception-to-response mapping.

  ```java
  // Custom exceptions
  public class OrderNotFoundException extends RuntimeException {
      public OrderNotFoundException(Long id) {
          super("Order not found: " + id);
      }
  }

  public class InsufficientStockException extends RuntimeException {
      private final Long productId;
      public InsufficientStockException(Long productId) {
          super("Insufficient stock for product: " + productId);
          this.productId = productId;
      }
  }

  // Centralized handler — ONE place for all error responses
  @RestControllerAdvice
  @Slf4j
  public class GlobalExceptionHandler {

      @ExceptionHandler(OrderNotFoundException.class)
      public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex,
                                                           HttpServletRequest request) {
          log.warn("Order not found: {}", ex.getMessage());
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage(),
                                     request.getRequestURI(), Instant.now()));
      }

      @ExceptionHandler(InsufficientStockException.class)
      public ResponseEntity<ErrorResponse> handleStock(InsufficientStockException ex,
                                                        HttpServletRequest request) {
          return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage(),
                                     request.getRequestURI(), Instant.now()));
      }

      @ExceptionHandler(MethodArgumentNotValidException.class)
      public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
          String message = ex.getBindingResult().getFieldErrors().stream()
              .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
              .collect(Collectors.joining(", "));
          return ResponseEntity.badRequest()
              .body(new ErrorResponse("VALIDATION_FAILED", message,
                                     request.getRequestURI(), Instant.now()));
      }

      @ExceptionHandler(Exception.class)  // catch-all
      public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
          log.error("Unexpected error", ex);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new ErrorResponse("INTERNAL_ERROR", "Something went wrong",
                                     request.getRequestURI(), Instant.now()));
      }
  }

  // Consistent error response body
  public record ErrorResponse(String code, String message, String path, Instant timestamp) {}
  ```

  🏭 **Real World:** Every production Spring Boot app has a `GlobalExceptionHandler`. Without it, Spring returns ugly HTML error pages or inconsistent JSON structures. Clients (Angular/React frontend) need predictable error format.

---

## 🔗 Study References
- [enhorse/java-interview — Spring](https://github.com/enhorse/java-interview/blob/master/spring.md)
- [Baeldung — Spring @Transactional Guide](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)
- [Baeldung — Spring N+1 Problem](https://www.baeldung.com/spring-hibernate-n1-problem)
- [Baeldung — @RestControllerAdvice](https://www.baeldung.com/exception-handling-for-rest-with-spring)
