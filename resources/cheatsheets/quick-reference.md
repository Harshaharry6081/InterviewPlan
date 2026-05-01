# ⚡ Java & Spring Boot Quick Cheatsheet

## Java 8+ One-Liners
```java
// Stream filter + map + collect
list.stream().filter(x -> x > 0).map(String::valueOf).collect(Collectors.toList())

// Optional
Optional.ofNullable(value).orElse("default")

// Sorting
list.sort(Comparator.comparing(Person::getName).thenComparing(Person::getAge))

// Group by
Map<String, List<Order>> = orders.stream().collect(Collectors.groupingBy(Order::getStatus))

// Flat map (flatten nested lists)
List<Item> items = orders.stream().flatMap(o -> o.getItems().stream()).collect(toList())
```

## Spring Annotations Quick Reference
| Annotation | Purpose |
|---|---|
| `@SpringBootApplication` | `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| `@RestController` | `@Controller` + `@ResponseBody` |
| `@RequestMapping` | Maps HTTP requests to handler methods |
| `@GetMapping` / `@PostMapping` | Shorthand for specific HTTP methods |
| `@PathVariable` | Extract value from URL path `/users/{id}` |
| `@RequestParam` | Extract query parameter `?page=1` |
| `@RequestBody` | Deserialize request JSON to object |
| `@ResponseBody` | Serialize return value to JSON |
| `@Valid` | Trigger Bean Validation on method argument |
| `@Transactional` | Wrap method in DB transaction |
| `@Cacheable` | Cache method result |
| `@Async` | Execute method in separate thread |
| `@Scheduled` | Run method on a schedule (cron) |
| `@EventListener` | Handle Spring events |
| `@Value` | Inject property value from config |
| `@Profile` | Conditionally activate bean for profile |
| `@ConditionalOnProperty` | Conditionally register bean based on property |

## HTTP Status Codes Quick Reference
```
2xx Success:  200 OK | 201 Created | 204 No Content
4xx Client:   400 Bad Request | 401 Unauthorized | 403 Forbidden | 404 Not Found | 409 Conflict
5xx Server:   500 Internal Server Error | 503 Service Unavailable
```

## JPA / SQL One-Liners
```java
// Find with pagination
repo.findAll(PageRequest.of(0, 20, Sort.by("createdAt").descending()))

// Derived query
List<Order> findByStatusAndCustomerIdOrderByCreatedAtDesc(String status, Long customerId)

// JPQL with JOIN FETCH (fix N+1)
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id)
```

## Docker Quick Reference
```bash
docker build -t myapp:1.0 .              # Build image
docker run -p 8080:8080 myapp:1.0        # Run container
docker compose up -d                      # Start all services
docker compose logs -f app               # Follow app logs
docker exec -it <container> /bin/sh      # Shell into container
docker ps                                # List running containers
docker images                            # List images
```

## kubectl Quick Reference
```bash
kubectl apply -f k8s/                    # Deploy
kubectl get pods                         # List pods
kubectl logs -f pod-name                 # Follow logs
kubectl describe pod pod-name            # Debug pod
kubectl scale deploy myapp --replicas=5  # Scale
kubectl rollout undo deploy myapp        # Rollback
kubectl port-forward pod-name 8080:8080  # Local testing
```

## Kafka Quick Reference
```
Topic Partitions = Max parallelism for consumer group
Message Key → same key → same partition (ordering guarantee)
Consumer Lag = messages in partition - consumer offset
Commit Offset = mark message as processed
```

## Redis Commands
```bash
SET key value EX 3600    # Set with 1 hour TTL
GET key
DEL key
EXISTS key
TTL key                  # Remaining time to live
KEYS pattern*            # Find keys (don't use in prod!)
FLUSHDB                  # Clear all keys (DANGER!)
```
