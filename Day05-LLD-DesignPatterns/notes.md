# 📘 Day 5 – Low Level Design (LLD) & Design Patterns (May 6th)

> **Goal:** Write clean, SOLID, extensible code and design a system class-by-class.

---

## ✅ Checklist
- [ ] SOLID Principles (know each with Java example)
- [ ] Creational Patterns: Singleton, Factory, Builder
- [ ] Structural Patterns: Decorator, Adapter
- [ ] Behavioral Patterns: Strategy, Observer
- [ ] Practice: Design a Parking Lot

---

## 1. SOLID Principles

### S — Single Responsibility
```java
// ❌ BAD: One class doing too much
class OrderService {
    public void placeOrder(Order o) { ... }
    public void sendEmailConfirmation(Order o) { ... }  // Not its job!
    public void generateInvoicePdf(Order o) { ... }     // Not its job!
}

// ✅ GOOD: Each class has ONE reason to change
class OrderService { public void placeOrder(Order o) { ... } }
class EmailService { public void sendConfirmation(Order o) { ... } }
class InvoiceService { public void generatePdf(Order o) { ... } }
```

### O — Open/Closed Principle
```java
// ❌ BAD: Adding new payment type requires modifying this class
class PaymentProcessor {
    public void process(String type, double amount) {
        if (type.equals("CREDIT")) { ... }
        else if (type.equals("DEBIT")) { ... }
        // Must modify code every time a new type is added!
    }
}

// ✅ GOOD: Open for extension, closed for modification
interface PaymentStrategy {
    void process(double amount);
}
class CreditCardPayment implements PaymentStrategy { ... }
class UpiPayment implements PaymentStrategy { ... }
// Adding PayPal = new class, no modification needed
```

### L — Liskov Substitution
```java
// ❌ BAD: Square violates LSP when extending Rectangle
class Rectangle {
    void setWidth(int w) { this.width = w; }
    void setHeight(int h) { this.height = h; }
    int area() { return width * height; }
}
class Square extends Rectangle {
    void setWidth(int w) { this.width = this.height = w; }  // Breaks expected behavior!
}

// ✅ GOOD: Separate shapes, no inheritance issues
interface Shape { int area(); }
class Rectangle implements Shape { ... }
class Square implements Shape { ... }
```

### I — Interface Segregation
```java
// ❌ BAD: Fat interface forces classes to implement unused methods
interface Animal {
    void eat();
    void fly();   // What about dogs?
    void swim();  // What about eagles?
}

// ✅ GOOD: Small, specific interfaces
interface Eatable { void eat(); }
interface Flyable { void fly(); }
interface Swimmable { void swim(); }
class Duck implements Eatable, Flyable, Swimmable { ... }
class Dog implements Eatable, Swimmable { ... }
```

### D — Dependency Inversion
```java
// ❌ BAD: High-level module depends on low-level module
class OrderService {
    private MySQLDatabase db = new MySQLDatabase();  // Tightly coupled!
}

// ✅ GOOD: Both depend on abstraction
interface OrderRepository { Order findById(Long id); }
class OrderService {
    private final OrderRepository repo;  // Depends on interface
    OrderService(OrderRepository repo) { this.repo = repo; }
}
class MySQLOrderRepository implements OrderRepository { ... }
class MongoOrderRepository implements OrderRepository { ... }
```

---

## 2. Design Patterns

### Singleton — One instance globally
```java
public class DatabaseConnectionPool {
    private static volatile DatabaseConnectionPool instance;

    private DatabaseConnectionPool() { /* expensive init */ }

    // Thread-safe double-checked locking
    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }
}
```

### Factory Pattern — Decouple creation from usage
```java
interface Notification { void send(String message); }
class EmailNotification implements Notification { ... }
class SmsNotification implements Notification { ... }
class PushNotification implements Notification { ... }

class NotificationFactory {
    public static Notification create(String type) {
        return switch (type) {
            case "EMAIL" -> new EmailNotification();
            case "SMS" -> new SmsNotification();
            case "PUSH" -> new PushNotification();
            default -> throw new IllegalArgumentException("Unknown: " + type);
        };
    }
}
// Usage: NotificationFactory.create("EMAIL").send("Order confirmed!");
```

### Builder Pattern — Complex object construction
```java
// Used everywhere in Spring Boot (ResponseEntity.ok().body(...))
public class Order {
    private final Long id;
    private final String status;
    private final List<OrderItem> items;
    private final LocalDateTime createdAt;

    private Order(Builder builder) { ... }

    public static class Builder {
        private Long id;
        private String status = "PENDING";
        private List<OrderItem> items = new ArrayList<>();
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder items(List<OrderItem> items) { this.items = items; return this; }
        public Order build() { return new Order(this); }
    }
}

// Usage
Order order = new Order.Builder()
    .id(1L)
    .status("CONFIRMED")
    .items(cartItems)
    .build();
```

### Strategy Pattern — Interchangeable algorithms
```java
// Sort a list of products by different criteria
interface SortStrategy {
    List<Product> sort(List<Product> products);
}
class SortByPrice implements SortStrategy { ... }
class SortByRating implements SortStrategy { ... }
class SortByPopularity implements SortStrategy { ... }

class ProductService {
    private SortStrategy strategy;

    public void setSortStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Product> getProducts() {
        return strategy.sort(allProducts);
    }
}
```

### Observer Pattern — Event-driven (basis of Spring Events)
```java
// Publisher
class OrderService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public Order createOrder(CreateOrderRequest req) {
        Order order = orderRepository.save(new Order(req));
        eventPublisher.publishEvent(new OrderCreatedEvent(order));  // Fire event
        return order;
    }
}

// Subscribers (loose coupling!)
@Component
class EmailListener {
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        emailService.send(event.getOrder().getCustomerEmail(), "Order Confirmed!");
    }
}

@Component
class InventoryListener {
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        inventoryService.reserveItems(event.getOrder().getItems());
    }
}
```

---

## 3. LLD Practice: Parking Lot

```java
// Entities
enum VehicleType { MOTORCYCLE, CAR, TRUCK }
enum SpotSize { SMALL, MEDIUM, LARGE }
enum SpotStatus { AVAILABLE, OCCUPIED }

class Vehicle {
    private String licensePlate;
    private VehicleType type;
}

class ParkingSpot {
    private int spotId;
    private SpotSize size;
    private SpotStatus status;
    private Vehicle currentVehicle;

    public boolean canFit(Vehicle v) {
        return switch (v.getType()) {
            case MOTORCYCLE -> true;
            case CAR -> size == SpotSize.MEDIUM || size == SpotSize.LARGE;
            case TRUCK -> size == SpotSize.LARGE;
        };
    }
}

class ParkingLot {
    private List<ParkingSpot> spots;

    public Optional<ParkingSpot> findAvailableSpot(Vehicle vehicle) {
        return spots.stream()
            .filter(s -> s.getStatus() == SpotStatus.AVAILABLE)
            .filter(s -> s.canFit(vehicle))
            .findFirst();
    }

    public Ticket park(Vehicle vehicle) {
        ParkingSpot spot = findAvailableSpot(vehicle)
            .orElseThrow(() -> new RuntimeException("Parking full!"));
        spot.setCurrentVehicle(vehicle);
        spot.setStatus(SpotStatus.OCCUPIED);
        return new Ticket(vehicle, spot, LocalDateTime.now());
    }

    public double checkout(Ticket ticket) {
        long hours = ChronoUnit.HOURS.between(ticket.getEntryTime(), LocalDateTime.now());
        ticket.getSpot().setStatus(SpotStatus.AVAILABLE);
        return calculateFee(ticket.getVehicle().getType(), hours);
    }
}
```
