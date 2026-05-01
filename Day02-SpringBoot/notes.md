# 📘 Day 2 — Spring Boot (May 3rd)

> Each item: **Question** + **Answer**. Check off once you understand it.

---

## 🔷 Spring Core — IoC & DI

- [ ] **What is Inversion of Control (IoC)?** — Instead of your code creating objects (`new Service()`), the Spring container creates and manages them. You just declare what you need via `@Autowired` and Spring injects it. Control of object creation is *inverted* to the framework.

- [ ] **3 types of Dependency Injection?** — **Constructor injection** (recommended: makes dependencies explicit, easy to test). **Setter injection** (for optional dependencies). **Field injection** (avoid: hides dependencies, breaks testability). Constructor injection is preferred because it enables immutability with `final` fields.

- [ ] **`BeanFactory` vs `ApplicationContext`?** — `BeanFactory`: lazy initialization, basic DI. `ApplicationContext`: extends BeanFactory, adds event publishing, internationalization, AOP, eager initialization. Always use `ApplicationContext` in practice.

- [ ] **What is a Spring Bean?** — Any Java object managed by the Spring IoC container. Registered via `@Component` (and its stereotypes) or `@Bean` in a `@Configuration` class.

- [ ] **Bean Scopes: `singleton` vs `prototype`?** — `singleton` (default): one instance per Spring context, shared across all injections. `prototype`: new instance created every time it's requested. Web scopes: `request` (per HTTP request), `session` (per HTTP session).

- [ ] **`@Component` vs `@Service` vs `@Repository`?** — All register beans. `@Service`: marks business layer (semantic). `@Repository`: marks data layer + enables **Spring exception translation** (converts `SQLException` → `DataAccessException`). `@Component`: generic.

- [ ] **`@Bean` vs `@Component`?** — `@Component`: class-level, Spring auto-detects via classpath scan. `@Bean`: method-level inside `@Configuration`, you control creation logic (useful for third-party classes you can't annotate).

- [ ] **`@Qualifier` vs `@Primary`?** — When multiple beans match a type: `@Primary` marks the default one. `@Qualifier("beanName")` at injection point selects a specific one. `@Qualifier` takes priority over `@Primary`.

---

## 🔷 Bean Lifecycle

- [ ] **Spring Bean lifecycle steps?** — 1) Instantiate (constructor) → 2) Inject dependencies → 3) `@PostConstruct` (init logic, e.g. warm caches, validate config) → 4) Bean ready for use → 5) `@PreDestroy` (cleanup: close connections, flush buffers) → 6) Container shutdown.

- [ ] **What is `@PostConstruct`?** — Runs once after DI is complete. Use for: initializing caches, starting background threads, validating config values. Better than constructor (DI is complete). `@PreDestroy` runs on container shutdown for cleanup.

---

## 🔷 Spring Boot

- [ ] **What does `@SpringBootApplication` do?** — Shorthand for 3 annotations: `@Configuration` (defines bean factory), `@EnableAutoConfiguration` (auto-configure based on classpath), `@ComponentScan` (scan current package for beans).

- [ ] **How does Spring Boot auto-configuration work?** — At startup, reads `META-INF/spring/...AutoConfiguration.imports`. Each class has `@Conditional` annotations (`@ConditionalOnClass`, `@ConditionalOnMissingBean`). For example, if `DataSource` is on classpath, auto-configures Hibernate without you writing any config.

- [ ] **What is `@Value` vs `@ConfigurationProperties`?** — `@Value("${app.name}")`: injects a single property. `@ConfigurationProperties(prefix="app")`: maps an entire group of properties to a Java class with validation support. Use `@ConfigurationProperties` for complex config.

- [ ] **What is Spring Boot Actuator?** — Provides production-ready endpoints: `/actuator/health` (liveness/readiness), `/actuator/metrics`, `/actuator/info`, `/actuator/prometheus`. Critical for Kubernetes health probes.

- [ ] **`spring.jpa.hibernate.ddl-auto` options?** — `none`: do nothing. `validate`: check schema matches entities (safe for prod). `update`: alter tables (risky for prod). `create`: drop+create on startup. `create-drop`: drop on shutdown. **Use `validate` or `none` in production. Use Flyway/Liquibase instead.**

---

## 🔷 Spring Data JPA

- [ ] **What is JPA vs Hibernate vs Spring Data JPA?** — JPA: Java specification (interface). Hibernate: JPA *implementation* (most popular). Spring Data JPA: abstraction over Hibernate that auto-generates repos, reduces boilerplate. All 3 work together.

- [ ] **JPA relationships — what is `@OneToMany` + `@ManyToOne`?** — Customer has many Orders: `@OneToMany(mappedBy="customer")` on Customer. Order has one Customer: `@ManyToOne @JoinColumn(name="customer_id")` on Order. Always define `mappedBy` on the *non-owning* side.

- [ ] **`FetchType.LAZY` vs `FetchType.EAGER`?** — `LAZY`: collection loaded only when accessed (better performance). `EAGER`: loaded immediately with parent. **Defaults**: `@OneToMany` = LAZY, `@ManyToOne` = EAGER. Always prefer LAZY to avoid unexpected queries.

- [ ] **What is the N+1 problem?** — 1 query fetches N orders. For each order, a separate query fetches its items → N+1 total queries. **Fix 1**: `JOIN FETCH` in JPQL. **Fix 2**: `@EntityGraph(attributePaths="items")` on repo method. **Fix 3**: `@BatchSize(size=50)`.

- [ ] **What is `@Transactional`?** — Spring wraps the method in a DB transaction. If method completes normally → commit. If exception (unchecked by default) → rollback. Place on *service* layer, not repository or controller.

- [ ] **Propagation types — REQUIRED vs REQUIRES_NEW?** — `REQUIRED` (default): join existing transaction, or create new one if none. `REQUIRES_NEW`: always suspend existing transaction and start a fresh one. Use `REQUIRES_NEW` for audit logs that must be saved even if main transaction rolls back.

- [ ] **Isolation levels?** — `READ_UNCOMMITTED` (dirty reads possible) → `READ_COMMITTED` (default PG, prevents dirty reads) → `REPEATABLE_READ` (prevents non-repeatable reads) → `SERIALIZABLE` (safest, slowest, prevents phantom reads).

- [ ] **The `@Transactional` self-invocation trap?** — Calling a `@Transactional` method from within the **same class** bypasses the Spring proxy → transaction is NOT applied! Fix: inject `self` (`@Autowired private MyService self;`) or move the method to another bean.

- [ ] **`readOnly = true` in `@Transactional`?** — Hint to Hibernate to skip dirty checking (won't track changes to entities). Hibernate may skip flushing, improving performance. Always use on read-only service methods.

---

## 🔷 Spring MVC (REST)

- [ ] **`@PathVariable` vs `@RequestParam` vs `@RequestBody`?** — `@PathVariable`: from URL path `/orders/{id}`. `@RequestParam`: from query string `/orders?status=ACTIVE`. `@RequestBody`: deserializes JSON request body to Java object (uses Jackson).

- [ ] **How does global exception handling work?** — `@RestControllerAdvice` on a class. Inside, `@ExceptionHandler(SomeException.class)` methods intercept that exception across all controllers and return a consistent error response. Avoids try-catch in every controller.

- [ ] **What is `HandlerInterceptor`?** — Intercepts requests before/after controller execution. `preHandle()` (before), `postHandle()` (after), `afterCompletion()` (after view rendered). Use for: logging, auth, rate limiting. More Spring-aware than a Servlet Filter.

- [ ] **Filter vs HandlerInterceptor — order of execution?** — `Filter` (Servlet level, runs first) → `DispatcherServlet` → `HandlerInterceptor` (Spring level) → `Controller`. Filters are for low-level concerns (CORS, encoding). Interceptors for Spring-specific concerns (auth, logging).

---

## 📝 Key Code to Write from Memory

```java
// 1. Constructor injection (RECOMMENDED)
@Service
public class OrderService {
    private final OrderRepository repo;
    private final EmailService emailService;

    public OrderService(OrderRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }
}

// 2. @ConfigurationProperties
@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
    private String name;
    private int maxConnections = 10; // default value
    // getters & setters
}

// 3. Fix N+1 with JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.customerId = :cid")
List<Order> findWithItems(@Param("cid") Long customerId);

// 4. @Transactional with REQUIRES_NEW for audit
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAuditLog(String action) {
    // Saves even if caller's transaction rolls back
    auditRepo.save(new AuditLog(action, Instant.now()));
}
```

---

## 🔗 Study References
- [enhorse/java-interview — Spring](https://github.com/enhorse/java-interview/blob/master/spring.md)
- [Baeldung — Spring Boot Interview Questions](https://www.baeldung.com/spring-boot-interview-questions)
- [Baeldung — Spring @Transactional](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)
- [Baeldung — N+1 Problem](https://www.baeldung.com/hibernate-show-sql)
