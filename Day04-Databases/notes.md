# 📘 Day 4 — Databases: PostgreSQL & MongoDB (May 5th)

> Each item: **Question** + **Answer**. Check off once you understand it.

---

## 🔷 Database Fundamentals

- [ ] **What are ACID properties?** — **Atomicity**: all-or-nothing (if one step fails, entire transaction rolls back). **Consistency**: DB moves from one valid state to another (constraints respected). **Isolation**: concurrent transactions don't see each other's intermediate state. **Durability**: committed data survives crashes (written to disk/WAL).

- [ ] **Dirty read vs Non-repeatable read vs Phantom read?** — **Dirty read**: reading uncommitted data from another transaction (data may be rolled back). **Non-repeatable read**: reading the same row twice in a transaction gets different values (another tx updated it). **Phantom read**: re-running a query gives different rows (another tx inserted/deleted rows).

- [ ] **4 SQL Isolation Levels?**
  - `READ UNCOMMITTED`: dirty reads possible (almost never used)
  - `READ COMMITTED`: no dirty reads (PostgreSQL default)
  - `REPEATABLE READ`: no dirty or non-repeatable reads (MySQL default)
  - `SERIALIZABLE`: safest, prevents all anomalies, slowest

- [ ] **What is optimistic vs pessimistic locking?** — **Optimistic**: no lock acquired. Check at commit time that data wasn't changed (using `@Version` in Hibernate). Good for low-conflict scenarios. **Pessimistic**: lock row when read (`SELECT FOR UPDATE`). Prevents others from reading/writing. Use for high-conflict critical sections.

- [ ] **How does Hibernate implement optimistic locking?** — Add `@Version` field (`int` or `Long`). Hibernate includes `WHERE version = ?` in UPDATE. If another transaction already updated it, version won't match → throws `OptimisticLockException`.

---

## 🔷 SQL — Critical Queries

- [ ] **`INNER JOIN` vs `LEFT JOIN` vs `FULL OUTER JOIN`?** — `INNER JOIN`: only rows with matches in BOTH tables. `LEFT JOIN`: all rows from left + matched rows from right (nulls where no match). `RIGHT JOIN`: opposite. `FULL OUTER JOIN`: all rows from both, nulls where no match.

- [ ] **`EXISTS` vs `IN` — which is faster?** — `EXISTS` is usually faster when the subquery returns many rows (short-circuits on first match). `IN` evaluates all values. `EXISTS` with correlated subquery = better for large datasets.

- [ ] **Window functions — what are `RANK()` vs `DENSE_RANK()` vs `ROW_NUMBER()`?** — `ROW_NUMBER()`: always unique (1,2,3,4). `RANK()`: gaps after ties (1,2,2,4). `DENSE_RANK()`: no gaps (1,2,2,3). All used with `OVER (PARTITION BY col ORDER BY col)`.

- [ ] **How to find the second highest salary?** — `SELECT MAX(salary) FROM employees WHERE salary < (SELECT MAX(salary) FROM employees);` Or: `SELECT salary FROM employees ORDER BY salary DESC LIMIT 1 OFFSET 1;`

- [ ] **What is a CTE (`WITH` clause)?** — Common Table Expression: a named temporary result set. Makes complex queries readable and avoids repeating subqueries. `WITH ranked AS (SELECT ...) SELECT * FROM ranked WHERE rn = 1`.

---

## 🔷 Indexes & Query Optimization

- [ ] **How does a B-Tree index work?** — A balanced tree structure where data is sorted. Lookup is O(log n). PostgreSQL uses B-Tree by default. Allows range queries (`>`, `<`, `BETWEEN`), equality, sorting.

- [ ] **Clustered vs non-clustered index?** — **Clustered**: rows physically stored in index order (SQL Server's Primary Key index, InnoDB). Only 1 per table. **Non-clustered**: separate structure pointing to rows. Multiple allowed. PostgreSQL technically has no clustered index but `CLUSTER` command can reorder physically.

- [ ] **What is a covering index?** — An index that contains ALL columns needed by a query — query answered from index alone without touching the table (Index-Only Scan). Much faster. Example: index on `(user_id, status)` covers `SELECT status FROM orders WHERE user_id = ?`.

- [ ] **When should you NOT create an index?** — Small tables (full scan faster). Columns with very low cardinality (e.g., `is_active` = true/false). Columns that are written to very frequently (each write updates the index). Rarely used in WHERE clauses.

- [ ] **How to read `EXPLAIN ANALYZE` output?** — Look for: **Seq Scan** on large table (bad, needs index). **Index Scan** (good). **Nested Loop** (ok for small tables). Check **actual rows** vs **estimated rows** (large difference = stale statistics, run `ANALYZE`). Check **cost** and **actual time**.

---

## 🔷 MongoDB

- [ ] **When to embed documents vs reference them?** — **Embed** when: data is always accessed together, child data is small, child belongs to one parent (e.g., order items inside order). **Reference** when: data is shared across documents, data is large/unbounded, data changes independently (e.g., product catalog referenced by orders).

- [ ] **MongoDB aggregation pipeline — key stages?** — `$match` (filter, like WHERE), `$group` (group + aggregate), `$project` (select/transform fields), `$sort`, `$limit`, `$skip`, `$lookup` (like JOIN), `$unwind` (flatten array field).

- [ ] **CAP Theorem — where does MongoDB sit?** — CAP: you can only guarantee 2 of 3: **C**onsistency, **A**vailability, **P**artition tolerance. MongoDB chooses **CP** (Consistency + Partition tolerance). During network partition, it may reject writes to maintain consistency.

- [ ] **Does MongoDB support ACID transactions?** — Yes, since version 4.0 for **multi-document transactions** (single-document operations were always atomic). Use sessions: `session.startTransaction()`. But prefer single-document atomicity via embedding for performance.

---

## 🔷 Hibernate Deep Dive

- [ ] **`save()` vs `persist()` vs `merge()`?** — `persist()`: JPA standard, entity must be new (transient), makes it managed. `save()`: Hibernate-specific, can work with detached entities, returns generated ID immediately. `merge()`: copies state of detached entity to managed entity → returns the managed instance.

- [ ] **First-level vs second-level cache?** — **First-level** (Session cache): automatic, per-session, same session returns same object without DB hit. **Second-level** (e.g., Ehcache/Redis): shared across sessions, must configure explicitly (`@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)`).

- [ ] **`@Inheritance` strategies?** — `SINGLE_TABLE` (all subclasses in one table, discriminator column, best performance but nulls). `TABLE_PER_CLASS` (one table per concrete class, no joins but duplicate columns). `JOINED` (one table per class + join, normalized, slower queries). Default: `SINGLE_TABLE`.

---

## 📝 SQL Queries to Write from Memory

```sql
-- Rank employees by salary within each department
SELECT name, dept, salary,
  RANK() OVER (PARTITION BY dept ORDER BY salary DESC) as dept_rank
FROM employees;

-- Latest order per customer (CTE + ROW_NUMBER)
WITH ranked AS (
  SELECT *, ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY created_at DESC) as rn
  FROM orders
)
SELECT * FROM ranked WHERE rn = 1;

-- Find duplicate emails
SELECT email, COUNT(*) as cnt
FROM users
GROUP BY email
HAVING COUNT(*) > 1;

-- Delete duplicates, keep row with smallest id
DELETE FROM users
WHERE id NOT IN (SELECT MIN(id) FROM users GROUP BY email);

-- Composite index for common query pattern
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status)
WHERE status != 'CANCELLED';  -- partial index
```

---

## 🔗 Study References
- [enhorse/java-interview — SQL](https://github.com/enhorse/java-interview/blob/master/sql.md)
- [enhorse/java-interview — Hibernate](https://github.com/enhorse/java-interview/blob/master/hibernate.md)
- [Baeldung — Hibernate Caching](https://www.baeldung.com/hibernate-second-level-cache)
- [PostgreSQL EXPLAIN docs](https://www.postgresql.org/docs/current/using-explain.html)
