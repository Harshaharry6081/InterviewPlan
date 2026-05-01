# 💻 Coding Problems — Spring Boot & REST APIs

> These simulate real "write this code in the interview" scenarios for backend roles.

---

## 🔵 REST Controller — Write from Scratch

### Challenge: Build a complete CRUD REST Controller

**Given:** A `Product` entity with `id`, `name`, `price`, `category`.
**Task:** Write the REST controller with full error handling.

```java
// DTO Classes
public record CreateProductRequest(
    @NotBlank String name,
    @Positive Double price,
    @NotBlank String category
) {}

public record ProductResponse(Long id, String name, Double price, String category) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getCategory());
    }
}

// Controller — Write this yourself in the interview!
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(productService.findAll(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

// Service Layer
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(String category) {
        List<Product> products = category != null
            ? productRepository.findByCategory(category)
            : productRepository.findAll();
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
            .map(ProductResponse::from)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    public ProductResponse create(CreateProductRequest req) {
        Product product = Product.builder()
            .name(req.name())
            .price(req.price())
            .category(req.category())
            .build();
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, CreateProductRequest req) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        product.setName(req.name());
        product.setPrice(req.price());
        product.setCategory(req.category());
        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }
}
```

---

## 🟢 JPA / Database Problems

### Challenge: Fix the N+1 Problem

```java
// ❌ N+1 Problem — DON'T write this
@GetMapping("/orders-with-items")
public List<OrderResponse> getOrdersWithItems() {
    List<Order> orders = orderRepository.findAll();  // Query 1
    return orders.stream()
        .map(o -> new OrderResponse(o, o.getItems()))  // N more queries!
        .toList();
}

// ✅ Fixed — Use JOIN FETCH
// In Repository:
@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();

// In Controller:
@GetMapping("/orders-with-items")
public List<OrderResponse> getOrdersWithItems() {
    return orderRepository.findAllWithItems().stream()  // Single query!
        .map(OrderResponse::from)
        .toList();
}
```

### Challenge: Write a Custom JPA Query

```java
// Task: Find all orders above a certain amount, placed in the last N days,
// paginated and sorted by date descending.
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
           "WHERE o.totalAmount >= :minAmount " +
           "AND o.createdAt >= :since " +
           "AND o.status = :status")
    Page<Order> findFilteredOrders(
        @Param("minAmount") Double minAmount,
        @Param("since") LocalDateTime since,
        @Param("status") String status,
        Pageable pageable
    );
}

// Usage:
Page<Order> result = orderRepository.findFilteredOrders(
    100.0,
    LocalDateTime.now().minusDays(30),
    "COMPLETED",
    PageRequest.of(0, 20, Sort.by("createdAt").descending())
);
```

---

## 🟡 Design Coding Challenges

### Challenge: Implement a Simple Rate Limiter

```java
// Token Bucket Rate Limiter using Redis
@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS = 100;
    private static final int WINDOW_SECONDS = 60;

    public boolean isAllowed(String clientId) {
        String key = "rate_limit:" + clientId;
        Long current = redisTemplate.opsForValue().increment(key);

        if (current == 1) {
            // First request — set expiry
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        return current <= MAX_REQUESTS;
    }
}

// Use as a filter or interceptor:
@Component
public class RateLimitFilter implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(429);  // Too Many Requests
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return false;
        }
        return true;
    }
}
```

### Challenge: Implement Retry with Exponential Backoff

```java
// Manual retry logic (interview often asks this)
public <T> T retryWithBackoff(Supplier<T> operation, int maxRetries) {
    int attempt = 0;
    long delayMs = 100;

    while (attempt < maxRetries) {
        try {
            return operation.get();
        } catch (Exception ex) {
            attempt++;
            if (attempt >= maxRetries) throw new RuntimeException("Max retries exceeded", ex);

            try {
                Thread.sleep(delayMs);
                delayMs *= 2;  // Exponential backoff: 100, 200, 400, 800ms
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", ie);
            }
        }
    }
    throw new RuntimeException("Should not reach here");
}

// Usage:
Product product = retryWithBackoff(() -> externalApiClient.getProduct(id), 3);
```

---

## 🔴 Concurrency Problems

### Challenge: Thread-Safe Counter

```java
// ❌ Not thread-safe
class Counter {
    private int count = 0;
    public void increment() { count++; }  // Race condition!
    public int get() { return count; }
}

// ✅ Option 1: synchronized
class SynchronizedCounter {
    private int count = 0;
    public synchronized void increment() { count++; }
    public synchronized int get() { return count; }
}

// ✅ Option 2: AtomicInteger (preferred — lock-free)
class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    public void increment() { count.incrementAndGet(); }
    public int get() { return count.get(); }
}

// ✅ Option 3: LongAdder (best for high contention)
class HighThroughputCounter {
    private final LongAdder count = new LongAdder();
    public void increment() { count.increment(); }
    public long get() { return count.sum(); }
}
```

### Challenge: Producer-Consumer with BlockingQueue

```java
class ProducerConsumerExample {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>(100);

    // Producer
    public void produce() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            queue.put("message-" + i);  // Blocks if queue is full
            System.out.println("Produced: " + i);
        }
    }

    // Consumer
    public void consume() throws InterruptedException {
        while (true) {
            String message = queue.take();  // Blocks if queue is empty
            System.out.println("Consumed: " + message);
            processMessage(message);
        }
    }

    public static void main(String[] args) {
        ProducerConsumerExample example = new ProducerConsumerExample();
        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(() -> example.produce());
        executor.submit(() -> example.consume());  // Consumer 1
        executor.submit(() -> example.consume());  // Consumer 2
    }
}
```

---

## 📝 Quick Complexity Reference

| Algorithm | Time | Space |
|---|---|---|
| Array access | O(1) | - |
| HashMap get/put | O(1) avg | O(n) |
| Binary Search | O(log n) | O(1) |
| Merge Sort | O(n log n) | O(n) |
| Quick Sort | O(n log n) avg | O(log n) |
| DFS / BFS | O(V + E) | O(V) |
| Dynamic Programming | Varies | O(n) |
