# 📘 Day 3 — REST APIs & Microservices (May 4th)

> **Source alignment:** [enhorse/java-interview](https://github.com/enhorse/java-interview) · Web Basics, REST, Microservices sections

---

## ✅ Master Checklist

### 🔷 HTTP & Web Basics
- [ ] What is HTTP vs HTTPS? How does TLS handshake work?
- [ ] What are the HTTP methods? Which are idempotent? Which are safe?
- [ ] What is the difference between PUT and PATCH?
- [ ] What is the difference between GET and POST?
- [ ] Know all HTTP status code families: 1xx, 2xx, 3xx, 4xx, 5xx.
- [ ] What is CORS? How do you enable it in Spring Boot?
- [ ] What is a cookie vs a session vs a JWT token?
- [ ] What is the difference between Authentication and Authorization?
- [ ] What is OAuth 2.0? What is OpenID Connect?
- [ ] What is a WebSocket? When to use it over REST?
- [ ] What is content negotiation (Accept header)?
- [ ] What is `application/json` vs `application/xml`?
- [ ] What is HATEOAS? What REST maturity level does it represent?

### 🔷 RESTful API Design
- [ ] What are the 6 REST architectural constraints?
- [ ] What is "stateless" in REST?
- [ ] How do you design URLs? (nouns not verbs, plural resources)
- [ ] How do you version a REST API? (URI `/v1/`, Header, Accept header)
- [ ] How do you implement pagination? (`page`, `size`, `sort` params)
- [ ] How do you design error responses? (consistent error body structure)
- [ ] What is an idempotency key? Why is it important for POST?
- [ ] What is request throttling / rate limiting?
- [ ] What is an API contract? What is OpenAPI / Swagger?

### 🔷 Microservices Architecture
- [ ] What is a Microservice? How is it different from a monolith?
- [ ] What are the advantages of microservices? Disadvantages?
- [ ] What is Domain-Driven Design (DDD)? What is a Bounded Context?
- [ ] What is an API Gateway? What problems does it solve?
- [ ] What is Service Discovery? How does Eureka work?
- [ ] What is a Load Balancer? Client-side vs Server-side load balancing.
- [ ] What is the Circuit Breaker pattern? States: CLOSED, OPEN, HALF-OPEN.
- [ ] What is Resilience4j? Key annotations: `@CircuitBreaker`, `@Retry`, `@TimeLimiter`.
- [ ] What is the Bulkhead pattern?
- [ ] What is the Saga pattern? Choreography vs Orchestration.
- [ ] What is the Strangler Fig pattern?
- [ ] What is the Sidecar pattern?
- [ ] What is a service mesh? (Istio, Linkerd)
- [ ] What is gRPC? When to prefer it over REST?
- [ ] How do microservices handle distributed transactions?
- [ ] What is eventual consistency?
- [ ] What is the Outbox pattern?
- [ ] How do you propagate authentication tokens between microservices?

### 🔷 Communication Patterns
- [ ] Synchronous vs Asynchronous communication — when to use each?
- [ ] What is request-response vs event-driven?
- [ ] What is a Dead Letter Queue (DLQ)? When does a message go to DLQ?
- [ ] What is idempotent message processing? Why is it critical?

---

## 📝 Key Code to Write from Memory

```java
// 1. CORS configuration in Spring Boot
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://myapp.com"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

// 2. Circuit Breaker with Resilience4j
@CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
@Retry(name = "inventoryService")
public Integer checkStock(Long productId) {
    return inventoryClient.getStock(productId);
}

public Integer inventoryFallback(Long productId, Throwable t) {
    log.warn("Inventory service down, returning cached value");
    return redisTemplate.opsForValue().get("stock:" + productId);
}

// 3. Consistent error response structure
public record ErrorResponse(
    String code,
    String message,
    Instant timestamp,
    String path
) {}
```

---

## 🔗 Reference
- [enhorse/java-interview — Web Basics](https://github.com/enhorse/java-interview/blob/master/web.md)
