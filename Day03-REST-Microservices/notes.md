# 📘 Day 3 – REST APIs & Microservices (May 4th)

> **Goal:** Be able to design and defend a RESTful API and microservices architecture end-to-end.

---

## ✅ Checklist
- [ ] REST Principles (Idempotency, Stateless)
- [ ] HTTP Status Codes & Methods
- [ ] Spring MVC (@RestController, Exception Handling)
- [ ] Microservices Patterns (Gateway, Circuit Breaker, Service Discovery)
- [ ] API versioning strategies

---

## 1. REST Principles

| Principle | Meaning |
|---|---|
| **Stateless** | Server stores NO session state. Each request must carry all info |
| **Uniform Interface** | Standard HTTP methods, resource-based URLs |
| **Client-Server** | UI and Backend are decoupled |
| **Cacheable** | Responses can be cached (GET should be cacheable) |

### Idempotency — CRITICAL INTERVIEW TOPIC
| Method | Idempotent? | Safe? | Description |
|---|---|---|---|
| GET | ✅ Yes | ✅ Yes | Read only, no side effects |
| POST | ❌ No | ❌ No | Creates resource, NOT idempotent |
| PUT | ✅ Yes | ❌ No | Full replace, calling twice = same result |
| PATCH | ❌ No | ❌ No | Partial update |
| DELETE | ✅ Yes | ❌ No | Deleting again = same result (not found) |

---

## 2. HTTP Status Codes

| Code | Meaning | When to Use |
|---|---|---|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation failure |
| 401 | Unauthorized | Not authenticated |
| 403 | Forbidden | Authenticated but no permission |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 422 | Unprocessable Entity | Semantic validation error |
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | Downstream dependency down |

---

## 3. Spring MVC — REST Controller

```java
@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.create(request);
        URI location = URI.create("/api/v1/orders/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.findAll(status, PageRequest.of(page, size)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Global Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.toList());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", errors.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log the full exception internally, but return generic message
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_ERROR", "Something went wrong"));
    }
}
```

---

## 4. Microservices Architecture

### Key Patterns

**API Gateway Pattern:**
```
Client → [API Gateway] → Order Service
                       → User Service
                       → Notification Service

Benefits:
- Single entry point
- Authentication/Authorization
- Rate Limiting
- Load Balancing
- SSL Termination
Tools: Spring Cloud Gateway, AWS API Gateway, Kong
```

**Circuit Breaker Pattern (Resilience4j):**
```java
@Service
public class ProductService {

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackProduct")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public CompletableFuture<Product> getProduct(Long id) {
        return CompletableFuture.supplyAsync(() -> productClient.getById(id));
    }

    // Called when circuit is OPEN or after retries exhausted
    public CompletableFuture<Product> fallbackProduct(Long id, Throwable ex) {
        log.warn("Fallback triggered for product {}: {}", id, ex.getMessage());
        return CompletableFuture.completedFuture(Product.unknown());
    }
}
```

**States of a Circuit Breaker:**
```
CLOSED (Normal) → too many failures → OPEN (Rejects all calls)
                                          ↓ after timeout
                                       HALF-OPEN (Lets a few calls through)
                                          ↓ success → CLOSED
                                          ↓ failure → OPEN
```

**Service Discovery:**
```
Services register themselves → [Eureka/Consul Registry]
Other services query registry → Get healthy instance list
                              → Call directly or via Gateway

Tools: Netflix Eureka, HashiCorp Consul, Kubernetes DNS
```

---

## 5. API Versioning Strategies

```java
// Strategy 1: URI versioning (Most common, clear)
GET /api/v1/orders
GET /api/v2/orders

// Strategy 2: Header versioning (Cleaner URLs)
GET /api/orders
Headers: API-Version: 2

// Strategy 3: Accept header (Content negotiation)
GET /api/orders
Accept: application/vnd.myapp.v2+json
```

---

## 6. Key Interview Q&A

| Question | Answer |
|---|---|
| REST vs SOAP? | REST: lightweight JSON, stateless, HTTP. SOAP: XML, has standards/contracts (WSDL), more verbose |
| PUT vs PATCH? | PUT replaces the entire resource. PATCH updates only specified fields |
| What is HATEOAS? | Responses include links to related actions (Level 3 REST) |
| What is service mesh? | Infrastructure layer for service-to-service comms (Istio, Linkerd) |
| How do microservices communicate? | Sync (REST, gRPC) or Async (Kafka, RabbitMQ) |
| What is the Saga Pattern? | Distributed transactions via a sequence of local transactions + compensating transactions |
| How to secure microservices? | JWT tokens validated at API Gateway, OAuth2/OIDC |
