# 📘 Day 5 — LLD & Design Patterns (May 6th)

> **Source alignment:** [ashishps1/awesome-low-level-design](https://github.com/ashishps1/awesome-low-level-design) — Complete coverage of all patterns and problems

---

## ✅ Master Checklist

### 🔷 SOLID Principles (Must explain each with Java example)
- [ ] **S** — Single Responsibility Principle: One class, one reason to change.
- [ ] **O** — Open/Closed Principle: Open for extension, closed for modification.
- [ ] **L** — Liskov Substitution Principle: Subtypes must be substitutable for their parent.
- [ ] **I** — Interface Segregation Principle: Many small interfaces > one fat interface.
- [ ] **D** — Dependency Inversion Principle: Depend on abstractions, not concretions.

### 🔷 Creational Patterns
- [ ] **Singleton** — Only one instance. Thread-safe double-checked locking. Enum Singleton.
- [ ] **Factory Method** — Subclasses decide which class to instantiate.
- [ ] **Abstract Factory** — Family of related objects without specifying concrete classes.
- [ ] **Builder** — Step-by-step construction of complex objects. (Lombok `@Builder`)
- [ ] **Prototype** — Clone existing objects instead of creating new ones.

### 🔷 Structural Patterns
- [ ] **Adapter** — Bridge between incompatible interfaces.
- [ ] **Decorator** — Add behaviour dynamically without modifying the class.
- [ ] **Facade** — Simplified interface to a complex subsystem.
- [ ] **Proxy** — Surrogate / placeholder (Spring AOP uses this!).
- [ ] **Composite** — Tree structures where individual and composites are treated uniformly.
- [ ] **Bridge** — Decouple abstraction from implementation.
- [ ] **Flyweight** — Share common data to reduce memory (String Pool is an example).

### 🔷 Behavioral Patterns
- [ ] **Strategy** — Interchangeable algorithms. (e.g., sorting strategies, payment methods)
- [ ] **Observer** — One-to-many dependency. Publisher/Subscriber. (Spring `@EventListener`)
- [ ] **Command** — Encapsulate a request as an object. Supports undo/redo.
- [ ] **Template Method** — Skeleton algorithm in base class, steps overridden in subclass.
- [ ] **Chain of Responsibility** — Pass request through a chain of handlers. (Spring Filter chain)
- [ ] **State** — Object behaviour changes based on internal state.
- [ ] **Iterator** — Sequential access to elements without exposing internal structure.
- [ ] **Mediator** — Central object that handles communication between objects.

---

## ✅ LLD Design Problems — Practice Each One

### From [awesome-low-level-design](https://github.com/ashishps1/awesome-low-level-design):

#### Tier 1 — Must Practice (Most Common in Interviews)
- [ ] 🅿️ **Parking Lot** — Vehicles, Spots (Small/Medium/Large), Ticket, Payment
- [ ] 🎬 **BookMyShow** — Movie, Theater, Screen, Seat, Booking
- [ ] 🛒 **Shopping Cart / E-Commerce** — Product, Cart, Order, Payment Strategy
- [ ] 🏧 **ATM Machine** — Card, Account, Transaction, State pattern
- [ ] 🚗 **Ride Sharing (Uber/Ola)** — Driver, Rider, Trip, Pricing Strategy

#### Tier 2 — Good to Practice
- [ ] 📚 **Library Management System** — Book, Member, Loan, Search
- [ ] 🏨 **Hotel Booking System** — Room, Reservation, Guest
- [ ] 🍕 **Food Delivery (Zomato/Swiggy)** — Restaurant, Menu, Order, DeliveryAgent
- [ ] ♟️ **Chess Game** — Board, Piece hierarchy, Move validation
- [ ] 🚦 **Traffic Signal Controller** — State pattern with timer

#### Tier 3 — Nice to Know
- [ ] 💬 **Chat Application** — User, Group, Message, Observer pattern
- [ ] 📦 **Inventory Management** — Product, Stock, Supplier
- [ ] 🏋️ **Gym Management System** — Member, Trainer, Slot, Subscription

---

## 📝 Template — How to Answer ANY LLD Question

```
Step 1: Clarify Requirements (2 min)
  → "What are the actors / users of this system?"
  → "What are the core use cases?"
  → "Any constraints I should know?"

Step 2: Identify Core Entities (2 min)
  → List the main classes/objects needed

Step 3: Define Relationships (2 min)
  → Has-a (composition) vs Is-a (inheritance)
  → Cardinality: one-to-many, many-to-many

Step 4: Apply Design Patterns (3 min)
  → "I'll use Strategy for payment types"
  → "Observer for notifications"
  → "Factory for creating vehicles"

Step 5: Write the Code (10+ min)
  → Start with interfaces/abstractions
  → Implement concrete classes
  → Wire them in a main/service class
```

---

## 📝 Parking Lot — Full Solution Skeleton

```java
// Enums
enum VehicleType { MOTORCYCLE, CAR, TRUCK }
enum SpotStatus { AVAILABLE, OCCUPIED }

// Entities
class Vehicle {
    private String plate;
    private VehicleType type;
}

class ParkingSpot {
    private int id;
    private VehicleType supportedType;  // MOTORCYCLE supports all, LARGE supports TRUCK
    private SpotStatus status = SpotStatus.AVAILABLE;
    private Vehicle vehicle;

    public boolean canFit(Vehicle v) {
        return switch(v.getType()) {
            case MOTORCYCLE -> true;
            case CAR -> supportedType == VehicleType.CAR || supportedType == VehicleType.TRUCK;
            case TRUCK -> supportedType == VehicleType.TRUCK;
        };
    }
}

class Ticket {
    private String id;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private LocalDateTime entryTime;
}

// Strategy for pricing
interface PricingStrategy {
    double calculate(VehicleType type, long hours);
}
class HourlyPricing implements PricingStrategy { ... }
class DailyCapPricing implements PricingStrategy { ... }

// Main Lot (Singleton)
class ParkingLot {
    private static ParkingLot instance;
    private List<ParkingSpot> spots;
    private PricingStrategy pricing;

    private ParkingLot() { ... }
    public static synchronized ParkingLot getInstance() {
        if (instance == null) instance = new ParkingLot();
        return instance;
    }

    public Ticket park(Vehicle vehicle) {
        ParkingSpot spot = spots.stream()
            .filter(s -> s.getStatus() == SpotStatus.AVAILABLE && s.canFit(vehicle))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Lot is full!"));
        spot.setVehicle(vehicle);
        spot.setStatus(SpotStatus.OCCUPIED);
        return new Ticket(vehicle, spot, LocalDateTime.now());
    }

    public double checkout(Ticket ticket) {
        long hours = ChronoUnit.HOURS.between(ticket.getEntryTime(), LocalDateTime.now());
        ticket.getSpot().setStatus(SpotStatus.AVAILABLE);
        return pricing.calculate(ticket.getVehicle().getType(), hours);
    }
}
```

---

## 🔗 Reference
- [ashishps1/awesome-low-level-design](https://github.com/ashishps1/awesome-low-level-design)
- [SOLID principles with Java examples](https://www.baeldung.com/solid-principles)
