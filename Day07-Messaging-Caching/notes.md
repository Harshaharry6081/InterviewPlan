# 📘 Day 7 – Messaging (Kafka) & Caching (Redis) (May 8th)

> **Goal:** Understand async communication and performance optimization — these are "stand-out" skills.

---

## ✅ Checklist
- [ ] Kafka Core Concepts (Topics, Partitions, Consumer Groups)
- [ ] Spring Kafka (Producer & Consumer)
- [ ] Redis Caching (Cache-Aside Pattern, Eviction)
- [ ] When to use sync vs async communication

---

## 1. Kafka — Core Concepts

```
Producer → [Topic] → Consumer Group
              |
         Partition 0: [msg1, msg2, msg3]
         Partition 1: [msg4, msg5, msg6]
         Partition 2: [msg7, msg8, msg9]
              |
    Consumer Group "order-processors":
         Consumer A → reads Partition 0
         Consumer B → reads Partition 1
         Consumer C → reads Partition 2
```

**Key Rules:**
- One **Consumer per Partition** within a group (parallelism is limited by partition count)
- **Offset**: Pointer to where a consumer is in a partition
- **Consumer Group**: Logical group of consumers that together consume all messages from a topic
- Messages are ordered **within a partition** (not across partitions)
- **Retention**: Messages are kept even after consumption (unlike queues)

### Key Kafka Terminology

| Term | Meaning |
|---|---|
| **Topic** | Category of messages (like a database table) |
| **Partition** | Sub-division of a topic for parallel processing |
| **Offset** | Sequential ID of a message within a partition |
| **Broker** | Kafka server that stores messages |
| **Producer** | Application that writes messages |
| **Consumer** | Application that reads messages |
| **Consumer Group** | Multiple consumers sharing a topic |
| **Leader** | Broker responsible for reads/writes to a partition |
| **Replica** | Copy of a partition on another broker (HA) |
| **Rebalance** | Redistribution of partitions when consumer joins/leaves |

---

## 2. Spring Kafka — Producer

```java
// application.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: order-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.example.events"

// Producer Service
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .totalAmount(order.getTotalAmount())
            .timestamp(Instant.now())
            .build();

        // Key by customerId ensures messages for same customer go to same partition
        kafkaTemplate.send("order-events", String.valueOf(order.getCustomerId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event for order {}", order.getId(), ex);
                } else {
                    log.info("Event published to partition {} offset {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
```

---

## 3. Spring Kafka — Consumer

```java
@Service
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(
        topics = "order-events",
        groupId = "notification-service",
        concurrency = "3"  // 3 consumer threads = 3 partitions in parallel
    )
    public void handleOrderCreated(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received event from partition={}, offset={}: {}", partition, offset, event);

        try {
            notificationService.notifyCustomer(event.getCustomerId(), event.getOrderId());
        } catch (Exception ex) {
            log.error("Failed to process event: {}", event, ex);
            throw ex;  // Triggers retry if configured
        }
    }

    // Dead Letter Topic — messages that fail after all retries
    @KafkaListener(topics = "order-events.DLT")
    public void handleDeadLetter(OrderCreatedEvent event) {
        log.error("Dead letter received: {}", event);
        // Save to DB for manual inspection/reprocessing
        failedEventRepository.save(FailedEvent.from(event));
    }
}
```

---

## 4. Redis Caching

### Cache-Aside Pattern (most common)
```
Read: App checks cache → HIT: return from cache
                       → MISS: read from DB → store in cache → return

Write: App writes to DB → invalidate/update cache
```

### Spring Boot Redis Caching
```java
// application.yml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379

// Enable caching
@SpringBootApplication
@EnableCaching
public class Application { ... }

// Service with caching
@Service
public class ProductService {

    @Cacheable(
        value = "products",
        key = "#id",
        unless = "#result == null"  // Don't cache null results
    )
    public Product getProduct(Long id) {
        // This method body is ONLY called on cache MISS
        log.info("Cache MISS - fetching from DB for id={}", id);
        return productRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        // Removes cached entry when product is updated
        return productRepository.save(product);
    }

    @CachePut(value = "products", key = "#result.id")
    public Product createProduct(Product product) {
        // Saves to cache AFTER method executes (unlike @Cacheable)
        return productRepository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void clearAllProductCache() {
        // Clears entire "products" cache (use with care!)
    }
}
```

### Redis Data Structures
```bash
# Strings (simple key-value)
SET user:session:abc123 "userId:42" EX 3600  # Expires in 1 hour
GET user:session:abc123

# Hashes (object fields)
HSET product:1 name "Laptop" price "999.99" stock "50"
HGET product:1 price

# Lists (queues/stacks)
LPUSH notifications "Order placed"
RPOP notifications

# Sets (unique members)
SADD product:1:tags "electronics" "laptop" "sale"
SMEMBERS product:1:tags

# Sorted Sets (leaderboards, priority queues)
ZADD leaderboard 1500 "player1"
ZADD leaderboard 2000 "player2"
ZRANGEBYSCORE leaderboard -inf +inf WITHSCORES
```

---

## 5. Kafka vs RabbitMQ Decision Matrix

| Feature | Kafka | RabbitMQ |
|---|---|---|
| **Architecture** | Log-based (pull) | Queue-based (push) |
| **Message Retention** | Configurable retention (days/weeks) | Deleted after consumption |
| **Throughput** | Millions/sec | Tens of thousands/sec |
| **Consumer Groups** | Multiple groups read same msg | Message consumed once |
| **Ordering** | Per-partition | Per-queue |
| **Use Case** | Event streaming, audit logs, real-time analytics | Task queues, RPC, routing |

---

## 6. Key Interview Q&A

| Question | Answer |
|---|---|
| What happens if a Kafka consumer dies? | Group rebalances — partitions are redistributed to remaining consumers |
| At-least-once vs at-most-once vs exactly-once? | At-least-once: may reprocess (safe for idempotent ops). Exactly-once: Kafka transactions (expensive) |
| Cache stampede / thundering herd? | Many requests hit DB simultaneously after cache expires. Fix: distributed lock, random TTL jitter |
| What is cache invalidation? | Removing stale data from cache when source data changes. "One of two hard problems in CS" |
| Redis vs Memcached? | Redis: data structures, persistence, pub/sub, clustering. Memcached: simpler, multi-threaded |
