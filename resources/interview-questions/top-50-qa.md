# ❓ Top 50 Interview Questions — Spring Boot Java Backend

> These are the most frequently asked questions for 3–6 year Java Backend Developer roles.
> Read the question first, try to answer in your head, THEN read the answer.

---

## 🔵 Java Core (Must Answer in 30 seconds)

**Q1: What is the difference between `==` and `.equals()`?**
> `==` compares **references** (memory addresses). `.equals()` compares **values**. For Strings, always use `.equals()`.

**Q2: Why is String immutable in Java?**
> Security (can't modify shared strings), thread-safety (safe without synchronization), String Pool efficiency (can cache strings), and hashcode caching (HashMap keys).

**Q3: What is the difference between `ArrayList` and `LinkedList`?**
> `ArrayList`: backed by array, O(1) get by index, O(n) insert/delete in middle. `LinkedList`: backed by doubly linked list, O(n) get by index, O(1) insert/delete at ends. Use ArrayList by default.

**Q4: What is `HashMap`'s default capacity and load factor?**
> Initial capacity: **16**. Load factor: **0.75**. Resizes (doubles) when 12 elements are inserted.

**Q5: What happens if two keys in HashMap have the same hashCode?**
> **Collision** — they're stored in the same bucket as a linked list (or Red-Black Tree if >8 entries in Java 8+). Retrieval uses `.equals()` to find the correct key.

**Q6: What is `ConcurrentHashMap` and how is it different from `HashMap`?**
> `ConcurrentHashMap` is thread-safe. In Java 8+, it uses **CAS operations and synchronized blocks at the bucket level** (not the whole map), giving better throughput than `Hashtable`.

**Q7: What are the 4 functional interfaces in Java 8?**
> `Function<T,R>` (T→R), `Predicate<T>` (T→boolean), `Consumer<T>` (T→void), `Supplier<T>` (→T).

**Q8: What is `Optional` and why use it?**
> A container that may or may not hold a value. Avoids `NullPointerException`. Forces the caller to handle the "empty" case explicitly.

**Q9: Difference between `Comparable` and `Comparator`?**
> `Comparable`: defines natural ordering within the class itself (`compareTo`). `Comparator`: external comparison strategy, used for custom sorting without modifying the class.

**Q10: What is the difference between `checked` and `unchecked` exceptions?**
> **Checked**: Must be handled or declared (IOException, SQLException). **Unchecked**: Extend RuntimeException, don't need to be declared (NPE, IllegalArgumentException).

---

## 🟢 Spring Boot (Must Answer Confidently)

**Q11: What does `@SpringBootApplication` do?**
> It's a shortcut for 3 annotations: `@Configuration` (defines beans), `@EnableAutoConfiguration` (auto-configures based on classpath), `@ComponentScan` (scans for components in the package).

**Q12: What is the difference between `@Component`, `@Service`, and `@Repository`?**
> All register beans. `@Service` = business layer (semantic). `@Repository` = data layer + adds **exception translation** (converts DB-specific exceptions to Spring's `DataAccessException`). `@Component` = generic.

**Q13: What are Bean Scopes in Spring?**
> `singleton` (default, one per context), `prototype` (new each time), `request` (one per HTTP request), `session` (one per HTTP session).

**Q14: What is `@Transactional` and when would you use it?**
> Wraps a method in a database transaction. Use it on service methods that perform multiple DB operations that must all succeed or all fail.

**Q15: What is the `@Transactional` self-invocation problem?**
> If a method in class A calls another `@Transactional` method in the **same class**, the transaction is bypassed because Spring uses a proxy. The call goes directly, not through the proxy. Fix: inject self, or move the method to another class.

**Q16: What is the difference between `FetchType.LAZY` and `EAGER`?**
> `LAZY`: collection is loaded only when accessed. `EAGER`: collection is loaded immediately with the parent. Default: `@OneToMany` = LAZY, `@ManyToOne` = EAGER. **Always prefer LAZY**.

**Q17: What is the N+1 problem?**
> When fetching N parent entities triggers N additional queries for each entity's children. Fix: use JOIN FETCH, `@EntityGraph`, or `@BatchSize`.

**Q18: How does Spring Boot auto-configuration work?**
> On startup, Spring reads `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Each auto-config class uses `@ConditionalOnClass`, `@ConditionalOnMissingBean` etc. to decide whether to configure itself.

**Q19: What is `@Value` vs `@ConfigurationProperties`?**
> `@Value("${property.name}")`: injects single value. `@ConfigurationProperties(prefix="app")`: binds a whole group of properties to a Java class (better for complex config).

**Q20: How do you handle exceptions globally in Spring Boot?**
> Using `@RestControllerAdvice` with `@ExceptionHandler` methods. Returns a consistent error response structure from one central place.

---

## 🟡 Microservices & REST

**Q21: What is an API Gateway?**
> Single entry point for all client requests. Handles authentication, rate limiting, routing, load balancing, and SSL termination. Examples: Spring Cloud Gateway, AWS API Gateway.

**Q22: What is a Circuit Breaker?**
> A pattern that stops sending requests to a failing service. States: CLOSED (normal), OPEN (failing, reject calls), HALF-OPEN (test if service recovered). Tool: Resilience4j.

**Q23: What is the Saga Pattern?**
> For distributed transactions across microservices. Each service performs a local transaction and publishes an event. If one step fails, compensating transactions are triggered to roll back.

**Q24: What is Service Discovery?**
> Allows services to find each other dynamically without hardcoded URLs. Services register with a registry (Eureka, Consul). Clients query the registry to get healthy instances.

**Q25: REST vs gRPC — when to use which?**
> REST: human-readable, JSON, browser-friendly, broader tooling. gRPC: binary Protocol Buffers, lower latency, strongly-typed contracts, bidirectional streaming. Use gRPC for internal high-performance service-to-service calls.

---

## 🔴 Databases

**Q26: What are the ACID properties?**
> **A**tomicity (all or nothing), **C**onsistency (valid state to valid state), **I**solation (concurrent transactions don't interfere), **D**urability (committed data survives crashes).

**Q27: What is database indexing and when should you avoid it?**
> Index = sorted data structure that speeds up reads. Avoid when: table is small, column has low cardinality, table has very high write rate (indexes slow writes).

**Q28: What is a deadlock?**
> Two transactions each hold a lock the other needs. DB detects it and kills one transaction. Prevention: always acquire locks in the same order.

**Q29: When would you use MongoDB over PostgreSQL?**
> MongoDB: schema-less/flexible structure, document-centric data (product catalogs, user profiles), need for horizontal sharding, high write throughput. PostgreSQL: relational data, complex joins, ACID transactions, financial data.

**Q30: What is the difference between SQL and NoSQL CAP theorem?**
> SQL databases prioritize **Consistency + Availability** (give up partition tolerance in practice). MongoDB prioritizes **Consistency + Partition Tolerance**. Cassandra prioritizes **Availability + Partition Tolerance**.

---

## ⭐ Advanced / Good-to-Have

**Q31: What is Kafka and how does it differ from RabbitMQ?**
> Kafka: log-based, messages are retained (consumers can replay). RabbitMQ: queue-based, message deleted after consumption. Kafka excels at high-throughput event streaming; RabbitMQ excels at task queues and routing.

**Q32: What is a Consumer Group in Kafka?**
> A set of consumers that together consume all messages from a topic. Each partition is assigned to exactly one consumer in the group, enabling parallel processing.

**Q33: What is the Cache-Aside pattern?**
> Application checks cache first → HIT: return cached value. MISS: read from DB, write to cache, return value. Write path: update DB and invalidate cache.

**Q34: What are Redis eviction policies?**
> `noeviction` (error when full), `allkeys-lru` (least recently used from all), `volatile-lru` (LRU from keys with TTL), `allkeys-random`, `volatile-ttl` (evict key with least TTL). Use `allkeys-lru` for a general cache.

**Q35: What is the difference between a Pod and a Deployment in Kubernetes?**
> Pod: single running instance of a container. Deployment: manages a set of Pod replicas, handles rolling updates, self-healing, scaling.

**Q36: What is a Liveness vs Readiness probe in Kubernetes?**
> **Liveness**: Is the app alive? If fails, K8s restarts the pod. **Readiness**: Is the app ready to receive traffic? If fails, pod is removed from load balancer but not restarted.

**Q37: What is HPA in Kubernetes?**
> **Horizontal Pod Autoscaler** — automatically scales pod count up or down based on CPU/memory metrics or custom metrics.

**Q38: What is Terraform?**
> Infrastructure as Code (IaC) tool. Defines cloud infrastructure (VMs, DBs, networks) in declarative HCL files. `terraform apply` creates/updates infrastructure. Supports AWS, Azure, GCP.

**Q39: What is distributed tracing?**
> Tracking a request across multiple microservices using a shared **Trace ID**. Each service adds a **Span** to the trace. Tools: Zipkin, Jaeger, AWS X-Ray. In Spring Boot: Micrometer Tracing.

**Q40: What is the difference between metrics and logs?**
> **Logs**: textual records of events (what happened). **Metrics**: numerical measurements over time (how much, how fast). **Traces**: path of a request through services (why it happened).

---

## 🎯 System Design Mini-Questions

**Q41: How would you design a system to handle 1 million concurrent users?**
> Horizontal scaling (multiple instances), load balancer, CDN for static content, Redis caching, DB read replicas, async processing for non-critical paths (Kafka), connection pooling.

**Q42: How do you ensure database migrations don't cause downtime?**
> Use Flyway/Liquibase. Write backward-compatible migrations (add columns, don't rename). Deploy app first with backward-compatible code, then apply migration.

**Q43: How do you handle secrets in a production environment?**
> Never hardcode secrets. Use: Environment variables, Kubernetes Secrets + external secret manager (AWS Secrets Manager, HashiCorp Vault), Rotate secrets regularly.

**Q44: How do you implement a health check?**
> Use Spring Boot Actuator (`/actuator/health`). Implement custom `HealthIndicator` for DB, cache, external services. K8s reads this for liveness/readiness probes.

**Q45: How do you do a zero-downtime deployment?**
> Rolling updates (K8s default), Blue-Green deployment, Canary deployment. Key: ensure new version is backward-compatible with current DB schema.

---

## 🤝 Behavioral

**Q46: Tell me about yourself.**
> "[X] years of experience in Java backend development. I've worked with Spring Boot, microservices, [mention key stack]. Most recently I [describe last project briefly]. I'm passionate about building scalable systems and I'm looking to [connect to this role]."

**Q47: What's your greatest technical strength?**
> Prepare a genuine answer. Example: "I'm strong at designing clean, testable service layers and debugging complex distributed system issues using tracing and logging."

**Q48: Describe a production incident you handled.**
> Use STAR method. Focus on: how you detected it, your diagnostic process, the fix, and what you did to prevent recurrence.

**Q49: How do you stay updated with technology?**
> "I follow Baeldung, InfoQ, and the Spring blog. I participate in code reviews to learn from teammates. I experiment with new libraries in side projects."

**Q50: Why do you want to leave your current job?**
> Frame positively: "I've grown a lot at my current role. I'm looking for a new challenge where I can work on [specific thing at the new company] at larger scale / with newer technology."
