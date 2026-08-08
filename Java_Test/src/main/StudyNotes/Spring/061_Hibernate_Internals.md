# 61. Hibernate Internals

## Most Asked

---

# 1. Hibernate architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

Hibernate architecture has layered components between your Java code and the database.

```text
┌─────────────────────────────────────────┐
│  Application (Entities, Repositories)   │
├─────────────────────────────────────────┤
│  Spring Data JPA / EntityManager API    │
├─────────────────────────────────────────┤
│  Hibernate Core                         │
│  ├── Session / EntityManager            │
│  ├── Persistence Context (L1 Cache)     │
│  ├── Transaction Manager                │
│  └── Query Engine (HQL / Criteria / SQL)│
├─────────────────────────────────────────┤
│  JDBC / Connection Pool (HikariCP)      │
├─────────────────────────────────────────┤
│  Database (MySQL, PostgreSQL, Oracle)   │
└─────────────────────────────────────────┘
```

| Layer | Responsibility |
|-------|---------------|
| Configuration | `SessionFactory`, dialect, mappings |
| Runtime | Session, persistence context, dirty checking |
| Database | JDBC, connection pooling, SQL execution |

```java
// Spring Boot hides this, but internally:
EntityManager em = ...;                    // JPA API
Session session = em.unwrap(Session.class); // Hibernate API
```

**Interview Point:**

> You interact via EntityManager (JPA). Hibernate manages persistence context, dirty checking, and SQL generation underneath.

</details>

---

# 2. Session?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Session** is Hibernate's primary runtime interface — a short-lived, non-thread-safe object that manages entity lifecycle within a persistence context.

| Property | Detail |
|----------|--------|
| Scope | One per request/transaction (in Spring) |
| Thread safety | ❌ Not thread-safe |
| Lifecycle | Opened → work → closed/flushed |
| Spring equivalent | `EntityManager` wraps Session |

```java
// Hibernate native (rare in Spring Boot)
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();
Employee emp = session.get(Employee.class, 1L);
emp.setSalary(emp.getSalary().add(BigDecimal.valueOf(5000)));
tx.commit();  // dirty checking → UPDATE SQL
session.close();

// Spring — @Transactional opens/closes session automatically
@Transactional
public void giveRaise(Long empId) {
    Employee emp = employeeRepo.findById(empId).orElseThrow();
    emp.setSalary(emp.getSalary().add(BigDecimal.valueOf(5000)));
    // no explicit save needed — dirty checking at commit
}
```

**Interview Point:**

> In Spring Boot, one Session per `@Transactional` method. Spring manages open/close — you never call `session.close()` manually.

</details>

---

# 3. SessionFactory?

<details>
<summary>Show Answer</summary>

**Answer:**

**SessionFactory** is a **thread-safe, immutable** factory that creates Session instances. It holds all entity metadata, mappings, and second-level cache configuration.

| | SessionFactory | Session |
|---|---------------|---------|
| Thread-safe | ✅ Yes | ❌ No |
| Lifecycle | Application-scoped (singleton) | Request/transaction-scoped |
| Cost | Expensive to create | Cheap to create |
| Spring equivalent | `EntityManagerFactory` |

```text
Application Startup:
  SessionFactory created once (reads entities, builds metadata)
        ↓
Runtime:
  SessionFactory.openSession() → Session (per transaction)
        ↓
  Session closed after transaction
```

```java
// Spring Boot — auto-configured
@Autowired
EntityManagerFactory emf;  // wraps SessionFactory

EntityManager em = emf.createEntityManager();  // creates new Session
```

**Interview Point:**

> SessionFactory = expensive singleton at startup. Session = cheap per-transaction instance. Never create SessionFactory per request.

</details>

---

# 4. EntityManager?

<details>
<summary>Show Answer</summary>

**Answer:**

**EntityManager** is the JPA standard interface for entity lifecycle operations. In Hibernate, it wraps a `Session`.

| Operation | Method |
|-----------|--------|
| Find by ID | `find(Class, id)` |
| Persist new | `persist(entity)` |
| Merge detached | `merge(entity)` |
| Remove | `remove(entity)` |
| Flush | `flush()` |
| Clear context | `clear()` |
| Query | `createQuery("...")` |

```java
@PersistenceContext
private EntityManager em;

public Order createOrder(CreateOrderRequest req) {
    Order order = new Order();
    order.setStatus(OrderStatus.PENDING);
    em.persist(order);   // INSERT on flush
    return order;
}

public Order updateOrder(Long id, String status) {
    Order order = em.find(Order.class, id);
    order.setStatus(OrderStatus.valueOf(status));  // dirty checking
    return order;  // UPDATE on commit
}
```

| EntityManager | Session |
|---------------|---------|
| JPA standard | Hibernate native |
| `persist()` / `merge()` | `save()` / `update()` |
| Portable across JPA providers | Hibernate-specific features |

**Interview Point:**

> Use EntityManager in Spring apps. `em.unwrap(Session.class)` only when you need Hibernate-specific APIs.

</details>

---

# 5. Persistence Context?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Persistence Context** is a first-level cache — a set of **managed entity instances** that Hibernate tracks within a Session/EntityManager scope.

```text
Persistence Context (L1 Cache)
┌──────────────────────────────────┐
│  Employee(id=1) → managed        │
│  Order(id=5)    → managed        │
│  Order(id=9)    → managed        │
└──────────────────────────────────┘
        ↕ dirty checking
        ↕ identity map (same ID = same object)
```

```java
@Transactional
public void demo() {
    Employee e1 = em.find(Employee.class, 1L);  // SELECT → added to context
    Employee e2 = em.find(Employee.class, 1L);  // NO SELECT — returns same instance
    System.out.println(e1 == e2);  // true — identity map guarantee

    e1.setName("Alice");  // dirty → UPDATE on flush
}
```

| Feature | Benefit |
|---------|---------|
| Identity map | Same ID = same Java object within session |
| Dirty checking | Auto-detect changed fields → generate UPDATE |
| Write-behind | SQL deferred until flush/commit |

**Interview Point:**

> Persistence context = L1 cache + change tracker. Same entity loaded twice = one DB query. Changes auto-persisted at flush.

</details>

---

## Entity States

---

# 6. Transient

<details>
<summary>Show Answer</summary>

**Answer:**

**Transient** — a new Java object **not associated** with any persistence context. Hibernate doesn't know about it; no DB row exists.

```java
Employee emp = new Employee();  // transient
emp.setName("Bob");
emp.setSalary(new BigDecimal("50000"));
// No INSERT until persist() or save()
```

| State | In Context? | Has DB Row? |
|-------|-------------|-------------|
| Transient | ❌ | ❌ |

```java
em.persist(emp);  // transient → persistent
```

**Interview Point:**

> `new Entity()` = transient. Call `persist()` or `save()` inside `@Transactional` to make it persistent.

</details>

---

# 7. Persistent (Managed)

<details>
<summary>Show Answer</summary>

**Answer:**

**Persistent (Managed)** — entity is **attached** to a persistence context. Hibernate tracks changes and syncs to DB on flush.

```java
@Transactional
public void updateSalary(Long id) {
    Employee emp = em.find(Employee.class, id);  // persistent
    emp.setSalary(new BigDecimal("75000"));        // dirty — no save() needed
    // flush at commit → UPDATE employees SET salary=75000 WHERE id=?
}
```

| State | In Context? | Has DB Row? | Auto-sync? |
|-------|-------------|-------------|------------|
| Persistent | ✅ | ✅ | ✅ Dirty checking |

**Interview Point:**

> Managed entities don't need explicit `save()` — Hibernate dirty checking detects changes and generates UPDATE at flush.

</details>

---

# 8. Detached

<details>
<summary>Show Answer</summary>

**Answer:**

**Detached** — entity was persistent but the persistence context is **closed**. Hibernate no longer tracks changes.

```java
@Transactional
public Employee findEmployee(Long id) {
    return em.find(Employee.class, id);  // persistent inside tx
}  // tx ends → entity becomes DETACHED

// Outside transaction:
emp.setName("Charlie");  // ❌ change NOT persisted
employeeRepo.save(emp);  // merge() → re-attaches and updates
```

| State | In Context? | Changes Tracked? |
|-------|-------------|-----------------|
| Detached | ❌ | ❌ |

```java
// Re-attach detached entity
Employee merged = em.merge(detachedEmp);  // returns managed copy
```

**Interview Point:**

> Detached = outside session. Common in web apps (load in service, pass to controller). Use `merge()` or stay within `@Transactional` boundary.

</details>

---

# 9. Removed

<details>
<summary>Show Answer</summary>

**Answer:**

**Removed** — entity is scheduled for **deletion** from the database on next flush.

```java
@Transactional
public void deleteEmployee(Long id) {
    Employee emp = em.find(Employee.class, id);  // persistent
    em.remove(emp);  // persistent → removed
    // DELETE on flush/commit
}

// Spring Data JPA way
employeeRepo.deleteById(id);
```

```text
State Transitions:
  new()        → TRANSIENT
  persist()    → PERSISTENT
  remove()     → REMOVED → (flush) → gone
  tx close     → PERSISTENT → DETACHED
  merge()      → DETACHED → PERSISTENT
```

**Interview Point:**

> `remove()` marks for deletion. Actual DELETE SQL runs at flush. Removed entity becomes transient after flush.

</details>

---

## Fetching

---

# 10. Lazy Loading

<details>
<summary>Show Answer</summary>

**Answer:**

**Lazy loading** defers loading of associated entities until they are **accessed** for the first time. Hibernate creates a **proxy** instead of running a JOIN query.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id")
private Department department;

// Only department_id loaded initially
Employee emp = employeeRepo.findById(1L).get();
// SELECT * FROM employees WHERE id = 1

Department dept = emp.getDepartment();  // proxy triggered
// SELECT * FROM departments WHERE id = ?
```

| | Lazy | Eager |
|---|------|-------|
| Initial query | Parent only | Parent + JOIN children |
| Access child | Extra SELECT | Already loaded |
| Risk | LazyInitializationException | Performance waste |

**Interview Point:**

> Lazy = load on demand via proxy. Always use LAZY in production. Access lazy associations inside `@Transactional` boundary.

</details>

---

# 11. Eager Loading

<details>
<summary>Show Answer</summary>

**Answer:**

**Eager loading** fetches the association **immediately** with the parent entity (via JOIN).

```java
@ManyToMany(fetch = FetchType.EAGER)
private Set<Role> roles;  // loaded with every User query

// SELECT u.*, r.* FROM users u
// LEFT JOIN user_roles ur ON ...
// LEFT JOIN roles r ON ...
// WHERE u.id = 1
```

| Problem | Impact |
|---------|--------|
| Cartesian product | Multiple JOINs multiply rows |
| Load unnecessary data | Fetch roles even when not needed |
| Can't override per query | Always eager |

**Interview Point:**

> Default EAGER on `@ManyToOne` and `@OneToOne` — override to LAZY. EAGER causes performance issues in production.

</details>

---

# 12. FetchType.LAZY

<details>
<summary>Show Answer</summary>

**Answer:**

`FetchType.LAZY` tells Hibernate to load the association **only when accessed**.

```java
@Entity
public class Order {
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> items;  // proxy until .getItems() called
}
```

```properties
# Global default — Spring Boot 2.x+ sets this
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=false
```

| Rule | Why |
|------|-----|
| Set LAZY on all associations | Avoid unnecessary JOINs |
| Access inside `@Transactional` | Prevent LazyInitializationException |
| Use JOIN FETCH when needed | Explicit eager load per query |

**Interview Point:**

> `enable_lazy_load_no_trans=false` (default) — accessing lazy field outside transaction throws exception. This is correct behavior.

</details>

---

# 13. FetchType.EAGER

<details>
<summary>Show Answer</summary>

**Answer:**

`FetchType.EAGER` loads the association **with the parent** every time.

```java
// Default for @ManyToOne and @OneToOne — CHANGE THIS
@ManyToOne(fetch = FetchType.EAGER)  // ❌ bad default
private Category category;

// Fix
@ManyToOne(fetch = FetchType.LAZY)   // ✅ production default
private Category category;
```

| Default EAGER | Override To |
|---------------|-------------|
| `@ManyToOne` | `LAZY` |
| `@OneToOne` | `LAZY` |
| `@OneToMany` | Already LAZY |
| `@ManyToMany` | Already LAZY |

**Interview Point:**

> Hibernate defaults `@ManyToOne` to EAGER — a known footgun. Always explicitly set `fetch = LAZY`.

</details>

---

## Advanced

---

# 14. N+1 Query Problem?

<details>
<summary>Show Answer</summary>

**Answer:**

**N+1** occurs when loading N parent entities triggers **1 query for parents + N queries for each child association**.

```java
// 1 query: SELECT * FROM orders
List<Order> orders = orderRepo.findAll();

// N queries: one per order
for (Order order : orders) {
    order.getItems().size();  // SELECT * FROM order_items WHERE order_id = ?
}
// Total: 1 + N queries (101 queries for 100 orders!)
```

```text
Without N+1 fix (100 orders):
  Query 1:  SELECT * FROM orders
  Query 2:  SELECT * FROM order_items WHERE order_id = 1
  Query 3:  SELECT * FROM order_items WHERE order_id = 2
  ...
  Query 101: SELECT * FROM order_items WHERE order_id = 100
```

| Detection | Tool |
|-----------|------|
| Log SQL | `spring.jpa.show-sql=true` |
| Statistics | `hibernate.generate_statistics=true` |
| APM | DataDog, New Relic, p6spy |

**Interview Point:**

> N+1 is the #1 Hibernate performance bug. Detect via SQL logging. Fix with JOIN FETCH, EntityGraph, or batch fetching.

</details>

---

# 15. How to solve N+1 issue?

<details>
<summary>Show Answer</summary>

**Answer:**

| Solution | Code | When |
|----------|------|------|
| JOIN FETCH | `JOIN FETCH o.items` in JPQL | Known association needed |
| `@EntityGraph` | `@EntityGraph(attributePaths={"items"})` | Reusable, declarative |
| Batch fetching | `default_batch_fetch_size=16` | Multiple lazy collections |
| DTO projection | Return DTO, not entity | Read-only list views |

```java
// Solution 1: JOIN FETCH
@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

// Solution 2: EntityGraph
@EntityGraph(attributePaths = {"items", "customer"})
List<Order> findByStatus(OrderStatus status);

// Solution 3: Batch size (application.properties)
spring.jpa.properties.hibernate.default_batch_fetch_size=16
// Generates: SELECT * FROM order_items WHERE order_id IN (1,2,3...16)
```

```text
With batch fetch (100 orders, batch_size=16):
  Query 1: SELECT * FROM orders
  Query 2: SELECT * FROM order_items WHERE order_id IN (1..16)
  Query 3: SELECT * FROM order_items WHERE order_id IN (17..32)
  ...
  Total: 1 + 7 = 8 queries (vs 101)
```

**Interview Point:**

> JOIN FETCH for specific queries. `@EntityGraph` for reusable fetch plans. Batch size as global safety net.

</details>

---

# 16. Fetch Join?

<details>
<summary>Show Answer</summary>

**Answer:**

**Fetch Join** is a JPQL JOIN that eagerly loads associations in a **single query**.

```java
@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items JOIN FETCH o.customer WHERE o.id = :id")
Optional<Order> findByIdWithDetails(@Param("id") Long id);
```

| Join Type | Loads Association? |
|-----------|-------------------|
| `JOIN` | ❌ No (used in WHERE) |
| `JOIN FETCH` | ✅ Yes (eager in same query) |

```sql
-- Generated SQL
SELECT o.*, i.*, c.*
FROM orders o
INNER JOIN order_items i ON i.order_id = o.id
INNER JOIN customers c ON c.id = o.customer_id
WHERE o.id = ?
```

| Caution | Detail |
|---------|--------|
| Use DISTINCT | JOIN FETCH can duplicate parent rows |
| Pagination | JOIN FETCH + Pageable = broken (loads all in memory) |
| Multiple bags | Cannot JOIN FETCH two `List` collections |

**Interview Point:**

> `JOIN FETCH` = one SQL query loads parent + children. Use `DISTINCT` to deduplicate. Don't combine with `Pageable`.

</details>

---

# 17. Entity Graph?

<details>
<summary>Show Answer</summary>

**Answer:**

**Entity Graph** is a declarative way to define which associations to fetch, overriding default fetch plans.

```java
// Named entity graph on entity
@Entity
@NamedEntityGraph(
    name = "Order.withItemsAndCustomer",
    attributeNodes = {
        @NamedAttributeNode("items"),
        @NamedAttributeNode("customer")
    }
)
public class Order { ... }

// Use in repository
@EntityGraph("Order.withItemsAndCustomer")
List<Order> findByStatus(OrderStatus status);

// Or inline
@EntityGraph(attributePaths = {"items", "customer"})
@Query("SELECT o FROM Order o WHERE o.status = :status")
List<Order> findByStatusWithGraph(@Param("status") OrderStatus status);
```

| | JOIN FETCH | Entity Graph |
|---|-----------|--------------|
| Style | JPQL string | Annotation / attributePaths |
| Reusability | Per query | Named graph reusable |
| Override | Hard-coded in query | Overrides LAZY per query |

**Interview Point:**

> Entity Graph = clean, reusable alternative to JOIN FETCH. Define on entity, reference in repository method.

</details>

---

## Caching

---

# 18. First Level Cache?

<details>
<summary>Show Answer</summary>

**Answer:**

**First Level Cache** (L1) is the **persistence context** — scoped to a single Session/EntityManager. Enabled by default, cannot be disabled.

```java
@Transactional
public void demo() {
    Employee e1 = em.find(Employee.class, 1L);  // SELECT
    Employee e2 = em.find(Employee.class, 1L);  // cache hit — no SQL
    assert e1 == e2;  // same object reference
}
```

| Property | Value |
|----------|-------|
| Scope | Session / transaction |
| Shared across sessions? | ❌ No |
| Enabled by default? | ✅ Yes |
| Eviction | `em.clear()` or session close |

```java
em.clear();  // evict all entities from L1 cache
em.detach(emp);  // evict single entity
```

**Interview Point:**

> L1 = per-session identity map. Automatic, always on. `clear()` forces re-fetch from DB.

</details>

---

# 19. Second Level Cache?

<details>
<summary>Show Answer</summary>

**Answer:**

**Second Level Cache** (L2) is a **SessionFactory-scoped** cache shared across all sessions. Requires explicit configuration and cache provider.

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product {
    @Id
    private Long id;
    private String name;
    private BigDecimal price;
}
```

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider
```

| Strategy | Use Case |
|----------|----------|
| `READ_ONLY` | Immutable reference data (countries, categories) |
| `READ_WRITE` | Occasionally updated data |
| `NONSTRICT_READ_WRITE` | Tolerates stale reads |
| `TRANSACTIONAL` | Strict consistency (rare) |

| L1 vs L2 | L1 | L2 |
|----------|----|----|
| Scope | Session | SessionFactory (all sessions) |
| Default | On | Off |
| Data | Managed entities | Dehydrated entity state |

**Interview Point:**

> L2 cache for **read-heavy, rarely changed** reference data. Not for frequently updated transactional data. Invalidate on writes.

</details>

---

# 20. Query Cache?

<details>
<summary>Show Answer</summary>

**Answer:**

**Query Cache** stores **query result IDs** (not entities) so repeated identical queries skip the database.

```properties
spring.jpa.properties.hibernate.cache.use_query_cache=true
```

```java
@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Query("SELECT p FROM Product p WHERE p.category = :category")
List<Product> findByCategory(@Param("category") String category);
```

```text
Query Cache Flow:
  1. Run query → store result IDs in query cache
  2. Same query again → get IDs from query cache
  3. Load actual entities from L2 cache (or DB if not in L2)
```

| Requirement | Why |
|-------------|-----|
| L2 cache must be enabled | Query cache stores entity IDs, L2 stores entities |
| Immutable or READ_ONLY entities | Stale query results if data changes |
| Region invalidation | Any entity change invalidates related query cache entries |

**Interview Point:**

> Query cache is rarely needed. L2 cache + proper fetch strategies solve most problems. Query cache adds invalidation complexity.

</details>

---

## Production Depth — Hibernate

---

# P1. LazyInitializationException

<details>
<summary>Show Answer</summary>

**Answer:**

Thrown when accessing a **lazy association outside an active persistence context** (session closed).

```java
// ❌ Causes LazyInitializationException
public OrderDto getOrder(Long id) {
    Order order = orderRepo.findById(id).orElseThrow();
    return new OrderDto(order.getId(), order.getItems().size());  // session closed!
}

// ✅ Fix 1: Stay in transaction
@Transactional(readOnly = true)
public OrderDto getOrder(Long id) {
    Order order = orderRepo.findById(id).orElseThrow();
    return new OrderDto(order.getId(), order.getItems().size());
}

// ✅ Fix 2: Eager fetch in query
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);

// ✅ Fix 3: DTO projection (best for APIs)
@Query("SELECT new com.example.OrderDto(o.id, SIZE(o.items)) FROM Order o WHERE o.id = :id")
Optional<OrderDto> findDtoById(@Param("id") Long id);
```

**Interview Point:**

> #1 Hibernate production bug. Fix: `@Transactional(readOnly=true)`, JOIN FETCH, or DTO projection. Never use `OpenSessionInView`.

</details>

---

# P2. Open Session In View (Anti-pattern)

<details>
<summary>Show Answer</summary>

**Answer:**

**OSIV** keeps the Hibernate session open for the entire HTTP request (including view rendering).

```properties
# Default in Spring Boot — TRUE
spring.jpa.open-in-view=true
```

| OSIV = true | Problem |
|-------------|---------|
| Lazy loads work in controller/view | Hidden queries during rendering |
| Session open entire request | Connection held longer |
| Masks lazy loading bugs | N+1 in view layer undetected |

```properties
# Production recommendation
spring.jpa.open-in-view=false
```

**Interview Point:**

> Disable OSIV in production. Use explicit fetch strategies in service layer. OSIV hides performance problems.

</details>

---

# P3. Dirty Checking and Flush Modes

<details>
<summary>Show Answer</summary>

**Answer:**

Hibernate compares managed entity state with snapshot at flush time and generates UPDATE for changed fields only.

```java
@Transactional
public void update() {
    Employee emp = em.find(Employee.class, 1L);
    emp.setName("New Name");     // dirty
    emp.setSalary(sameSalary);   // not dirty if value unchanged

    em.flush();  // force SQL now (default: flush before commit)
}
```

| FlushMode | Behavior |
|-----------|----------|
| `AUTO` (default) | Flush before query and at commit |
| `COMMIT` | Flush only at commit |
| `MANUAL` | Flush only on explicit `em.flush()` |

**Interview Point:**

> Dirty checking = auto UPDATE without `save()`. Flush sends pending SQL to DB. Default AUTO flushes before JPQL queries.

</details>

---

# P4. Hibernate Statistics and Monitoring

<details>
<summary>Show Answer</summary>

**Answer:**

```properties
spring.jpa.properties.hibernate.generate_statistics=true
```

```java
// Programmatic check
SessionFactory sf = emf.unwrap(SessionFactory.class);
Statistics stats = sf.getStatistics();
log.info("Query count: {}", stats.getQueryExecutionCount());
log.info("Entity load count: {}", stats.getEntityLoadCount());
log.info("Second level cache hits: {}", stats.getSecondLevelCacheHitCount());
```

| Metric | Indicates |
|--------|-----------|
| High query count | N+1 problem |
| High entity load | Over-fetching |
| Low L2 hit ratio | Cache misconfiguration |

**Interview Point:**

> Enable statistics in staging to detect N+1. Use p6spy or datasource-proxy for SQL logging in production.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Session vs SessionFactory vs EntityManager?

<details>
<summary>Show Answer</summary>

**Answer:**

**SessionFactory** = thread-safe singleton factory. **Session** = per-transaction, not thread-safe. **EntityManager** = JPA wrapper around Session.

</details>

---

### Q: Entity states?

<details>
<summary>Show Answer</summary>

**Answer:**

**Transient** (new), **Persistent** (managed), **Detached** (session closed), **Removed** (scheduled for delete).

</details>

---

### Q: What causes N+1?

<details>
<summary>Show Answer</summary>

**Answer:**

Loading N parent entities, then accessing lazy association on each = 1 + N queries. Fix with JOIN FETCH, EntityGraph, or batch fetch size.

</details>

---

### Q: L1 vs L2 cache?

<details>
<summary>Show Answer</summary>

**Answer:**

**L1** = per-session persistence context (always on). **L2** = shared across sessions (opt-in, needs cache provider). L2 for read-heavy reference data.

</details>

---

### Q: LazyInitializationException fix?

<details>
<summary>Show Answer</summary>

**Answer:**

Access lazy fields inside `@Transactional`, use JOIN FETCH, or DTO projection. Disable OSIV (`open-in-view=false`).

</details>

---

### Q: Why is @ManyToOne default EAGER bad?

<details>
<summary>Show Answer</summary>

**Answer:**

Every parent query JOINs the child table even when association isn't needed. Always set `fetch = LAZY`.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> SessionFactory = singleton. Session/EntityManager = per transaction. Persistence context = L1 cache + dirty checking. States: transient → persist → persistent → close → detached. LAZY everywhere. N+1 = JOIN FETCH / EntityGraph / batch size. L2 cache for reference data. Disable OSIV. LazyInitializationException = access outside transaction.

</details>
