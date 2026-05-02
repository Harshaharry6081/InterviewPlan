# 📘 Day 5 — LLD & Design Patterns (May 6th)
> **Goal:** Bridge the gap between "knowing the name" and "writing the code".

---

## 🔷 SOLID Principles

- [ ] **S — Single Responsibility Principle (SRP)**
  A class should have only ONE reason to change.
  🏭 **Real World:** Don't put SQL queries, Email sending, and PDF generation all in `OrderService`. Split them: `OrderRepository`, `EmailService`, `ReportGenerator`.

- [ ] **O — Open/Closed Principle (OCP)**
  Software entities should be open for extension, but closed for modification.
  🏭 **Real World:** If you add a new payment method (e.g., Crypto), you shouldn't have to modify the `PaymentProcessor`'s `if/else` block. Instead, implement a `PaymentStrategy` interface.

- [ ] **L — Liskov Substitution Principle (LSP)**
  Subtypes must be substitutable for their base types without breaking the app.
  ❌ **Bad Example:** A `Square` class extending `Rectangle`. If you set width=10 and height=5 on a `Rectangle` reference, it works. If that reference is actually a `Square`, setting width might change height, breaking the "Rectangle" contract.

- [ ] **I — Interface Segregation Principle (ISP)**
  Clients shouldn't be forced to depend on methods they don't use.
  🏭 **Real World:** Instead of one `SmartDevice` interface with `print()`, `fax()`, `scan()`, split them into `Printer`, `Fax`, and `Scanner` interfaces.

- [ ] **D — Dependency Inversion Principle (DIP)**
  Depend on abstractions (interfaces), not concretions (classes).
  🏭 **Real World:** `OrderService` should depend on `PaymentService` (interface), not `PayPalPaymentService` (class). Spring's Dependency Injection (DI) is the tool we use to achieve DIP.

---

## 🔷 Creational Patterns

- [ ] **Singleton Pattern**
  Ensures a class has only one instance.
  🏭 **Real World:** Spring Beans are singletons by default.
  ```java
  // Double-checked locking (Thread-safe)
  public class DatabaseConnection {
      private static volatile DatabaseConnection instance;
      private DatabaseConnection() {}
      public static DatabaseConnection getInstance() {
          if (instance == null) {
              synchronized (DatabaseConnection.class) {
                  if (instance == null) instance = new DatabaseConnection();
              }
          }
          return instance;
      }
  }
  ```

- [ ] **Builder Pattern**
  For objects with many optional parameters.
  🏭 **Real World:** In Spring Boot, we use Lombok's `@Builder`.
  ```java
  @Builder
  public class User {
      private String name;
      private String email;
      private int age; // optional
  }
  // Usage: User.builder().name("Harsh").email("h@g.com").build();
  ```

---

## 🔷 Structural Patterns

- [ ] **Adapter Pattern**
  Converts one interface to another that the client expects.
  🏭 **Real World:** Integrating a 3rd party legacy library into your modern Spring app. You create an Adapter class that implements your internal interface but calls the legacy methods inside.

- [ ] **Proxy Pattern**
  A placeholder for another object to control access (security, logging, lazy loading).
  🏭 **Real World:** Spring's `@Transactional` and `@Cacheable` use **Dynamic Proxies**. When you call a method, you're actually calling the Proxy, which starts the transaction/checks the cache, then calls your real method.

---

## 🔷 Behavioral Patterns

- [ ] **Strategy Pattern**
  Defines a family of algorithms, encapsulates each one, and makes them interchangeable.
  🏭 **Real World:** Routing logic or Payment logic.
  ```java
  public interface ShippingStrategy {
      double calculate(double weight);
  }

  @Service
  public class FedExStrategy implements ShippingStrategy { ... }

  @Service
  public class DHLStrategy implements ShippingStrategy { ... }

  // Context
  public class OrderProcessor {
      private ShippingStrategy strategy;
      public void setStrategy(ShippingStrategy s) { this.strategy = s; }
  }
  ```

- [ ] **Observer Pattern**
  One-to-many relationship where state change in one object notifies all observers.
  🏭 **Real World:** Spring's `ApplicationEvent` system.
  ```java
  // 1. Define event
  public class OrderPlacedEvent extends ApplicationEvent { ... }

  // 2. Publish event
  publisher.publishEvent(new OrderPlacedEvent(order));

  // 3. Listen to event (Observer)
  @EventListener
  public void handleOrder(OrderPlacedEvent event) {
      emailService.send(event.getOrder());
  }
  ```

---

## 📝 LLD Design Problem: BookMyShow Skeleton

```java
// Focus on relationships and core logic
class Theater {
    private List<Screen> screens;
}

class Screen {
    private List<Seat> seats;
}

class Show {
    private Movie movie;
    private Screen screen;
    private LocalDateTime startTime;
    private Map<Seat, SeatStatus> seatStatus; // Key to LLD: track seat availability per show
}

class Booking {
    private Show show;
    private List<Seat> selectedSeats;
    private User user;
    private BookingStatus status;
    
    public void confirm() {
        // 1. Validate seats are still available
        // 2. Process payment
        // 3. Update seatStatus in Show
    }
}
```

---

## 🔗 Study References
- [Refactoring.Guru — Design Patterns](https://refactoring.guru/design-patterns)
- [Baeldung — SOLID Principles](https://www.baeldung.com/solid-principles)
- [Baeldung — Spring Design Patterns](https://www.baeldung.com/spring-framework-design-patterns)
