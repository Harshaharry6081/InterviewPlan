# 📘 Day 4 – Databases: PostgreSQL & MongoDB (May 5th)

> **Goal:** Confidently discuss data modeling, SQL, and NoSQL trade-offs.

---

## ✅ Checklist
- [ ] ACID Properties
- [ ] SQL Joins, Indexes, Query Optimization
- [ ] N+1 Problem & JPA Fetch Strategies
- [ ] MongoDB Document Modeling
- [ ] When to use SQL vs NoSQL

---

## 1. ACID Properties

| Property | Description |
|---|---|
| **Atomicity** | All operations in a transaction succeed or ALL fail (no partial commits) |
| **Consistency** | DB moves from one valid state to another (constraints maintained) |
| **Isolation** | Concurrent transactions don't interfere with each other |
| **Durability** | Committed data survives crashes (written to disk) |

### Isolation Levels (Very commonly asked!)
| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|---|---|---|
| READ UNCOMMITTED | ✅ Possible | ✅ Possible | ✅ Possible |
| READ COMMITTED | ❌ Prevented | ✅ Possible | ✅ Possible |
| REPEATABLE READ | ❌ Prevented | ❌ Prevented | ✅ Possible |
| SERIALIZABLE | ❌ Prevented | ❌ Prevented | ❌ Prevented |

---

## 2. PostgreSQL — SQL Essentials

### Joins
```sql
-- INNER JOIN: only matching rows
SELECT o.id, o.total, u.name
FROM orders o
INNER JOIN users u ON o.user_id = u.id;

-- LEFT JOIN: all orders, even if user is deleted
SELECT o.id, o.total, u.name
FROM orders o
LEFT JOIN users u ON o.user_id = u.id;

-- GROUP BY with HAVING
SELECT user_id, COUNT(*) as order_count, SUM(total) as total_spent
FROM orders
WHERE status = 'COMPLETED'
GROUP BY user_id
HAVING COUNT(*) > 5
ORDER BY total_spent DESC;
```

### Indexes — Performance Optimization
```sql
-- Single column index
CREATE INDEX idx_orders_user_id ON orders(user_id);

-- Composite index (order matters! most selective first)
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- Partial index (only index what you query)
CREATE INDEX idx_active_orders ON orders(user_id) WHERE status = 'ACTIVE';

-- Explain query plan
EXPLAIN ANALYZE SELECT * FROM orders WHERE user_id = 100;
-- Look for: Seq Scan (bad for large tables) vs Index Scan (good)
```

### Window Functions
```sql
-- Rank orders by amount per user
SELECT
    order_id,
    user_id,
    total_amount,
    RANK() OVER (PARTITION BY user_id ORDER BY total_amount DESC) as rank
FROM orders;

-- Running total
SELECT
    order_date,
    total_amount,
    SUM(total_amount) OVER (ORDER BY order_date) as running_total
FROM orders;
```

---

## 3. N+1 Problem (Critical for Spring interviews!)

```java
// THE PROBLEM: 1 query for orders + N queries for each order's items
List<Order> orders = orderRepository.findAll(); // Query 1
for (Order order : orders) {
    // LAZY fetch triggers N additional queries!
    System.out.println(order.getItems().size()); // Queries 2..N+1
}

// FIX 1: JOIN FETCH in JPQL
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findWithItems(@Param("status") String status);

// FIX 2: EntityGraph (declarative)
@EntityGraph(attributePaths = {"items", "items.product"})
List<Order> findByStatus(String status);

// FIX 3: Batch size (Hibernate hint)
@OneToMany(fetch = FetchType.LAZY)
@BatchSize(size = 50) // Load items in batches of 50
private List<OrderItem> items;
```

---

## 4. MongoDB — Document Modeling

### When to Use MongoDB vs PostgreSQL

| Use PostgreSQL When | Use MongoDB When |
|---|---|
| Data has clear relationships | Data is document-centric |
| ACID transactions are critical | Schema evolves frequently |
| Complex reporting/queries | High write throughput |
| Financial data | Product catalogs, user profiles |

### Schema Design Patterns
```javascript
// ✅ Embedding (good for data that's always accessed together)
{
  "_id": ObjectId("..."),
  "orderId": "ORD-001",
  "customer": {
    "name": "John Doe",      // Embed if customer data rarely changes
    "email": "john@example.com"
  },
  "items": [                   // Embed if items belong to one order
    { "productId": "P1", "qty": 2, "price": 29.99 }
  ]
}

// ✅ Referencing (good for shared/large data)
{
  "_id": ObjectId("..."),
  "orderId": "ORD-001",
  "customerId": ObjectId("CUST-123"),  // Reference - separate collection
  "productIds": ["P1", "P2"]           // Reference list
}
```

### Spring Data MongoDB
```java
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    private String name;
    private Double price;
    private List<String> tags;

    @Indexed
    private String sku;   // Creates a MongoDB index

    @Field("category_name")  // Map to different field name in DB
    private String category;
}

// Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByTagsContaining(String tag);
    List<Product> findByPriceBetween(Double min, Double max);

    @Query("{ 'price': { $gt: ?0, $lt: ?1 }, 'tags': ?2 }")
    List<Product> findByPriceRangeAndTag(Double min, Double max, String tag);
}
```

---

## 5. Key Interview Q&A

| Question | Answer |
|---|---|
| What is a deadlock? | Two transactions waiting for each other's locks. DB detects and kills one |
| Index on every column? | No! Indexes slow down writes. Index only high-cardinality, frequently queried columns |
| What is a covering index? | Index contains all columns needed by a query, avoiding table lookup entirely |
| MongoDB vs Cassandra? | MongoDB: document store, strong consistency. Cassandra: wide-column, eventual consistency, extreme write throughput |
| How to handle schema migrations? | Use Flyway (SQL scripts) or Liquibase (XML/YAML). Never use `ddl-auto: create-drop` in prod |
| What is sharding? | Horizontal partitioning of data across multiple servers (MongoDB supports this natively) |
