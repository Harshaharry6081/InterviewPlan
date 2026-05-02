# 📘 Day 3 — REST APIs & Microservices (May 4th)
> **Format:** Question → Detailed Answer → 🏭 Real-World Use → 💻 Code

---

## 🔷 HTTP & REST Fundamentals

- [ ] **What is the difference between PUT and PATCH?**

  **PUT** is for **full updates**. It replaces the entire resource. If you send a PUT request with only one field, the rest of the fields in the DB might be set to null or default.
  **PATCH** is for **partial updates**. It only modifies the fields you send, leaving others untouched.

  🏭 **Real World:** If a user wants to change *only* their profile picture, use `PATCH /users/1`. If you're updating a whole "Order" object with all its lines and addresses, use `PUT /orders/1`.

  ```java
  // Spring Boot Controller handling PATCH
  @PatchMapping("/{id}")
  public ResponseEntity<User> updatePartially(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
      User user = repository.findById(id).orElseThrow();
      fields.forEach((key, value) -> {
          Field field = ReflectionUtils.findField(User.class, key);
          field.setAccessible(true);
          ReflectionUtils.setField(field, user, value);
      });
      return ResponseEntity.ok(repository.save(user));
  }
  ```

---

- [ ] **Which HTTP methods are Idempotent? Why does it matter?**

  An operation is **idempotent** if making the same request multiple times has the same effect as making it once.

  | Method | Idempotent? | Safe? |
  |---|---|---|
  | `GET` | ✅ Yes | ✅ Yes |
  | `POST` | ❌ No | ❌ No |
  | `PUT` | ✅ Yes | ❌ No |
  | `DELETE` | ✅ Yes | ❌ No |
  | `PATCH` | ❌ No (usually) | ❌ No |

  🏭 **Real World:** If a "Payment" request (`POST /payments`) times out, the client doesn't know if it succeeded. If they retry, they might be charged twice!
  **Fix:** Use an `Idempotency-Key` header. The server stores the key for 24h; if a second request arrives with the same key, it returns the cached response instead of processing again.

---

- [ ] **JWT vs Session Authentication — which to use for Microservices?**

  **Session-based:** Server stores session ID in memory/DB. State is on the server. Hard to scale (requires sticky sessions or shared Redis).
  **JWT (Token-based):** Stateless. All user info is inside the signed token. Server just validates the signature. Perfect for scaling microservices.

  🏭 **Real World:** In a microservice mesh, the **API Gateway** validates the JWT once, then passes the user identity (`user-id`, `roles`) to downstream services via headers. Downstream services don't need to call a central "Auth Service" for every request.

---

## 🔷 Microservices Patterns

- [ ] **What is a Circuit Breaker? (Resilience4j)**

  Prevents a "cascading failure". If Service A calls Service B and Service B is slow/down, Service A's threads will hang, eventually crashing Service A too.
  The Circuit Breaker "trips" (opens) after X failures, immediately returning a **fallback** instead of calling the failing service.

  **States:**
  1. `CLOSED`: Normal operation.
  2. `OPEN`: Service failing, calls blocked.
  3. `HALF_OPEN`: Test calls to see if service recovered.

  ```java
  @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
  public String processPayment(Order order) {
      return restTemplate.postForObject("http://payment-service/pay", order, String.class);
  }

  public String paymentFallback(Order order, Throwable t) {
      return "Payment Service is currently busy. Your order is queued.";
  }
  ```

---

- [ ] **Saga Pattern: How to handle transactions across microservices?**

  Since microservices have private DBs, you can't use `@Transactional` across them. Saga manages this as a sequence of local transactions.

  1. **Choreography:** Services exchange events. A → B → C. (Simple, but hard to track).
  2. **Orchestration:** A central "Orchestrator" tells each service what to do. (Complex, but easy to monitor).

  🏭 **Real World:** **Order Fulfillment Saga**:
  - `OrderService` reserves order.
  - `PaymentService` charges card.
  - `InventoryService` picks items.
  - If `InventoryService` fails → **Compensating Transaction**: `PaymentService` issues a refund and `OrderService` cancels order.

---

- [ ] **API Gateway vs Service Discovery (Eureka)**

  **Eureka:** The "Phonebook". Every microservice registers its IP/Port here.
  **API Gateway (Spring Cloud Gateway):** The "Front Door". It handles routing, security (JWT), rate limiting, and logging. It looks up service IPs in Eureka to route requests.

---

## 🔷 Communication Patterns

- [ ] **When to use Feign Client vs Kafka?**

  **Feign Client (REST):** Synchronous. Use when you need an **immediate response** (e.g., checking if a user exists during login).
  **Kafka (Messaging):** Asynchronous. Use for **fire-and-forget** or background tasks (e.g., sending an email, updating analytics, generating a report).

  🏭 **Real World:** When a user places an order:
  - Call `InventoryService` via **Feign** (must ensure items are in stock NOW).
  - Publish `OrderPlaced` event to **Kafka** (email service and shipping service will consume this whenever they are ready).

---

## 🔗 Study References
- [Baeldung — Spring Cloud Gateway](https://www.baeldung.com/spring-cloud-gateway)
- [Baeldung — Resilience4j Guide](https://www.baeldung.com/resilience4j)
- [Microservices.io — Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Auth0 — JWT Introduction](https://auth0.com/learn/json-web-tokens/)
