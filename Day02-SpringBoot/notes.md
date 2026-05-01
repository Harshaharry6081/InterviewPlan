# 📘 Day 2 – Spring Boot (May 3rd)

> **Goal:** Understand the full lifecycle of a Spring Boot app — from startup to request handling.

---

## ✅ Checklist
- [ ] IoC & Dependency Injection
- [ ] Bean Lifecycle & Scopes
- [ ] Spring Boot Auto-configuration
- [ ] Spring Data JPA (Repositories, Transactions)
- [ ] Spring Security basics (JWT)
- [ ] Profiles & application.yml

---

## 1. IoC & Dependency Injection

**Inversion of Control (IoC):** Instead of you creating objects (`new Service()`), Spring creates and manages them.

**Three types of Dependency Injection:**
```java
// 1. Constructor Injection (RECOMMENDED)
@Service
public class OrderService {
    private final OrderRepository repo;

    @Autowired  // optional in newer Spring versions if single constructor
    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }
}

// 2. Field Injection (NOT recommended - hides dependencies)
@Service
public class OrderService {
    @Autowired
    private OrderRepository repo;
}

// 3. Setter Injection (use for optional dependencies)
@Service
public class OrderService {
    private OrderRepository repo;

    @Autowired
    public void setRepo(OrderRepository repo) {
        this.repo = repo;
    }
}
```

---

## 2. Bean Scopes

| Scope | Description |
|---|---|
| `singleton` | **Default.** One instance per Spring context |
| `prototype` | New instance every time it's requested |
| `request` | One instance per HTTP request (Web apps) |
| `session` | One instance per HTTP session (Web apps) |

```java
@Bean
@Scope("prototype")
public ExpensiveObject expensiveObject() {
    return new ExpensiveObject();
}
```

---

## 3. Bean Lifecycle

```
Container Created
       ↓
Bean Instantiated (constructor)
       ↓
Dependencies Injected (@Autowired)
       ↓
@PostConstruct (init logic)
       ↓
Bean Ready to Use
       ↓
@PreDestroy (cleanup logic)
       ↓
Container Shutdown
```

```java
@Component
public class MyBean {

    @PostConstruct
    public void init() {
        System.out.println("Bean initialized! Connect to DB, warm caches here.");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Bean destroyed! Close connections here.");
    }
}
```

---

## 4. Spring Data JPA

### Repository Hierarchy
```
Repository (marker)
    └── CrudRepository (CRUD methods)
            └── PagingAndSortingRepository
                    └── JpaRepository (flush, batch) ← Use this
```

### Custom Queries
```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Derived Query (Spring generates SQL)
    List<Order> findByStatusAndCustomerId(String status, Long customerId);

    // JPQL Query
    @Query("SELECT o FROM Order o WHERE o.totalAmount > :amount")
    List<Order> findExpensiveOrders(@Param("amount") Double amount);

    // Native SQL
    @Query(value = "SELECT * FROM orders WHERE status = ?1", nativeQuery = true)
    List<Order> findByStatusNative(String status);
}
```

### @Transactional — MOST IMPORTANT
```java
@Service
@Transactional  // All methods transactional by default
public class OrderService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOrder(Order order) {
        // Runs in a NEW transaction, even if called from within another transaction
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Order getOrder(Long id) {
        // Prevents non-repeatable reads
    }

    @Transactional(readOnly = true)  // Performance optimization for reads
    public List<Order> getAllOrders() { ... }
}
```

**Propagation levels to know:**
- `REQUIRED` (default): Join existing tx or create new one
- `REQUIRES_NEW`: Always create a new tx, suspend existing
- `SUPPORTS`: Use existing tx if present, else non-transactional
- `NEVER`: Must NOT run within a transaction

---

## 5. Auto-Configuration & application.yml

```yaml
# application.yml
spring:
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://localhost:5432/orders_db
    username: ${DB_USER:postgres}   # env var with default fallback
    password: ${DB_PASS:secret}
  jpa:
    hibernate:
      ddl-auto: validate           # never use 'create-drop' in prod!
    show-sql: false

server:
  port: 8080

---
# Profile-specific overrides
spring:
  config:
    activate:
      on-profile: dev
  jpa:
    show-sql: true
```

**Profiles:**
```bash
# Run with dev profile
java -jar app.jar --spring.profiles.active=dev

# Or in IDE
-Dspring.profiles.active=dev
```

---

## 6. Key Interview Q&A

| Question | Answer |
|---|---|
| `@Component` vs `@Service` vs `@Repository`? | All register beans. `@Service` = business layer, `@Repository` = adds exception translation |
| What is `@SpringBootApplication`? | = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| How does auto-configuration work? | Spring reads `META-INF/spring.factories`, checks conditions (`@ConditionalOnClass`) |
| N+1 Problem in JPA? | Fetching parent + N separate queries for children. Fix: `@EntityGraph` or JOIN FETCH |
| What is `@Transactional` self-invocation problem? | Calling a `@Transactional` method from within the same class bypasses the proxy! |
| `FetchType.LAZY` vs `EAGER`? | LAZY = load only when accessed, EAGER = load immediately. Use LAZY by default |
