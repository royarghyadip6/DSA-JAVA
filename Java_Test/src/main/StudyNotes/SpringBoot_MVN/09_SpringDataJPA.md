# 09. Spring Data JPA

## Start here (simple English)

**In one sentence:** You write a Java class for a table row (**entity**) and a small interface (**repository**). Spring + Hibernate turn that into SQL.

**Three names:**

| Name | Role |
|------|------|
| **JPA** | The **rule book** (annotations like `@Entity`) |
| **Hibernate** | The **engine** that writes SQL |
| **Spring Data JPA** | The **shortcut**: you declare `interface OrderRepository extends JpaRepository<…>` and Spring **implements it** |

**Everyday picture:** A librarian.

- Entity = one book card (`Order` ↔ table `orders`)
- Repository = the desk: `findById`, `save`
- Hibernate = the person who walks to the shelves (SQL)
- HikariCP = a **limited set of library cards** (database connections). If all cards are out, the next person waits.

**Tiny example:**

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sku;
}

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findBySku(String sku);
}
```

You never write `class OrderRepositoryImpl`. Spring builds a **proxy** at startup.

**Two words that save you in production:**

- **LAZY** = “don’t load related rows until I ask”
- **N+1** = you loaded 100 orders, then accidentally ran 100 extra queries for their lines

Interview Q&A is **5–8 year standard**.

---

Stack, from your code down:

```text
Repository interface
  → Spring Data JPA proxy (FactoryBean)
    → EntityManager (JPA API)
      → Hibernate (implementation)
        → JDBC
          → HikariCP
            → Database
```

**JPA** is a specification. **Hibernate** is the usual implementation. **Spring Data JPA** is a repository abstraction on top of `EntityManager`.

---

## 1. Entity mapping (what you must get right)

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    @Version
    private long version;
}
```

| Topic | Senior default |
|-------|----------------|
| Fetch collections | **LAZY** |
| `@ManyToOne` | **LAZY** (JPA default is EAGER — override it) |
| IDs | `IDENTITY` (MySQL/Postgres serial) or `SEQUENCE` (better batching) |
| Equals/hashCode | Don’t use auto-id before persist; prefer business key or don’t put entities in HashSet until persisted |
| DTOs | Don’t expose entities on REST |
| `ddl-auto` | `validate` / `none` + Flyway |

**Inheritance:** `SINGLE_TABLE` (discriminator, fast, sparse columns), `JOINED` (normalized, extra joins), `TABLE_PER_CLASS` (usually avoid).

**`@Embedded` / `@Embeddable`:** value types, no own table.

**`@Version`:** optimistic locking. Concurrent update → `OptimisticLockException` → 409.

Enums: `@Enumerated(STRING)` never `ORDINAL` in prod.

---

## 2. Repository abstraction

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findBySku(String sku);

    @Query("select o from Order o where o.customer.id = :id")
    List<Order> findForCustomer(@Param("id") Long customerId);

    @Query(value = "select * from orders where sku = :sku", nativeQuery = true)
    Optional<Order> findNative(@Param("sku") String sku);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Order o set o.status = :s where o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("s") Status s);

    @EntityGraph(attributePaths = "lines")
    Optional<Order> findWithLinesById(Long id);
}
```

Spring Data generates a **JDK proxy**. Method names are parsed (`findBySku`, `existsBy…`, `countBy…`, `deleteBy…`).

`JpaRepository` extends `PagingAndSortingRepository` / `CrudRepository`: `save`, `findById`, `findAll(Pageable)`, `flush`.

**`save`:** if the entity is new → persist; else merge. With generated ids, “new” means id is null.

**`@Modifying`:** bulk JPQL update/delete. Bypasses persistence context dirty checking. You **must** `@Transactional`. `clearAutomatically` avoids stale cache. Does not fire `@PreUpdate` on entities.

**Query derivation pitfalls:** `findByCustomerName` may surprise you on nested properties. Prefer `@Query` when the name gets long.

---

## 3. Persistence context (L1 cache)

`EntityManager` / Hibernate `Session` is a **first-level cache** for the duration of a persistence context (usually the transaction).

```text
findById(1) → SQL SELECT
findById(1) again in same TX → no SQL, same instance
change field → dirty check at flush → UPDATE
```

Flush happens: before query that might need your changes, at commit, on `flush()`.

**Managed vs detached:** after TX ends, entities are detached (unless OSIV). Accessing LAZY association after detach → `LazyInitializationException`.

---

## 4. N+1 and how you actually fix it

```text
List<Order> orders = orderRepo.findAll();
orders.forEach(o -> o.getLines().size());  // 1 + N selects
```

Fixes:

1. **`@EntityGraph`** / `join fetch` in JPQL for **that** use case
2. **Batch fetching:** `hibernate.default_batch_fetch_size=16` — still extra queries, fewer round trips
3. **DTO query** — don’t load the graph if you only need columns:

```java
@Query("select new com.acme.OrderSummary(o.id, o.sku) from Order o")
List<OrderSummary> summaries();
```

4. **Two queries** (ids then `findAllById`) sometimes beat a cartesian join fetch

**Join fetch + pagination:** Hibernate may warn / apply pagination in memory. Classic trap. Don’t `join fetch` a collection and `Pageable` the same query without knowing this.

---

## 5. Open Session in View (OSIV)

```yaml
spring.jpa.open-in-view: false
```

Default in Boot is **true** for MVC: session stays open until the view/JSON is written. Controllers can touch lazy fields without `LazyInitializationException`. Cost:

- DB connection held during JSON serialization
- N+1 hidden in production logs
- Transactions longer than the service method

**Senior default for JSON APIs: `false`.** Load what you need in the service (graph or DTO) inside `@Transactional`.

---

## 6. Transactions at the repository edge

Spring Data methods are `@Transactional` (read-only for `find*`). **Business** transactions belong on the **service** so multiple repos participate in one TX.

```java
@Service
public class OrderService {
    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        // several repo calls, one commit
    }
}
```

Details: chapter 10.

---

## 7. EntityManager vs repository

Inject `EntityManager` for Criteria, `persist` vs `merge` control, bulk operations, `flush`/`clear` in jobs.

```java
@PersistenceContext
private EntityManager em;
```

Repository is enough for 90% of CRUD. Don’t drop to EM to look clever.

---

## 8. Auditing

```java
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    @CreatedBy
    private String createdBy;
}
```

`@EnableJpaAuditing` + `AuditorAware<String>` from Security context.

Hibernate: `@CreationTimestamp` / `@UpdateTimestamp` if you don’t need Spring Security principal.

---

## 9. Migrations: Flyway / Liquibase

```yaml
spring:
  flyway:
    enabled: true
  jpa:
    hibernate:
      ddl-auto: validate
```

`V1__init.sql`, `V2__add_index.sql`. No one edits V1 after it ran in prod. Repeatable migrations `R__` for views.

Liquibase: XML/YAML/JSON changelogs, same idea.

**Never** `ddl-auto=update` in production. It won’t drop columns the way you think, and it races with Flyway.

---

## 10. Connection pool (Hikari) — preview of chapter 15

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

Pool size is **not** “number of users”. It is concurrent **in-flight queries**. Tomcat threads 200 + pool 10 → 190 threads blocked on `getConnection`. Size pool from DB capacity, not from Tomcat max.

---

## 11. Production pitfalls

1. EAGER `@OneToMany` on an entity used in many screens.
2. `FetchType.EAGER` `@ManyToOne` pulling half the DB.
3. `cascade = ALL` + `orphanRemoval` on the wrong relation — deleting a parent wipes children you wanted to keep.
4. Bidirectional `equals`/`hashCode` infinite recursion.
5. `@Transactional` on a REST controller + OSIV true → connection during HTTP write.
6. Native query returning entities without mapping, or modifying without `@Modifying`.
7. `save` in a loop (N inserts) instead of `saveAll` / JDBC batch (`hibernate.jdbc.batch_size` + SEQUENCE ids).
8. `findAll()` on a million-row table.

---

# Interview Q&A (5–8 year bar)

A fresher knows `@Entity` and `JpaRepository`. A 5–8 year answer explains N+1, OSIV, `IDENTITY` vs batching, and Flyway vs `ddl-auto`.

### Q1. JPA vs Hibernate vs Spring Data JPA?

**Answer:** JPA = spec. Hibernate = impl. Spring Data JPA = repository proxies + query derivation on top of `EntityManager`.

**Counter:** Can you use Hibernate without Spring Data?  
**Answer:** Yes — inject `EntityManager` / `SessionFactory`. You lose derived queries.

---

### Q2. How does `findByEmail` work?

**Answer:** At startup Spring Data parses the method name, builds a query (Criteria/JPQL). A JDK proxy implements the interface.

**Counter:** What if the property doesn’t exist?  
**Answer:** **Startup** failure (`PropertyReferenceException`), not first call. Fail fast.

---

### Q3. What is the N+1 problem?

**Answer:** One query for parents, then one per parent for children. Fix with fetch join / entity graph / batch size / DTO query.

**Counter:** Why did it only show in prod?  
**Answer:** OSIV + small local data, or lazy not triggered in tests. Logging SQL in prod-like tests.

---

### Q4. LAZY vs EAGER?

**Answer:** LAZY = load on access (proxy). EAGER = load with parent. Collections default LAZY; many-to-one default EAGER in JPA — **override many-to-one to LAZY**.

**Counter:** Is EAGER ever OK?  
**Answer:** Rare, when the association is always needed and cardinality is 1 and cheap. Still prefer explicit graphs per use case.

---

### Q5. `LazyInitializationException`?

**Answer:** Lazy association accessed with no persistence context (TX closed, OSIV false). Fix: fetch in the service, DTO query, or (worse) OSIV.

---

### Q6. What does `open-in-view` do?

**Answer:** Binds the session to the HTTP request. Convenient, hides N+1, holds connections. Set **false** for APIs.

---

### Q7. `@Modifying` query didn’t update the loaded entity?

**Answer:** Bulk JPQL skips the persistence context. `clearAutomatically` / `flushAutomatically`, or don’t mix bulk update with managed instances of the same rows.

---

### Q8. `persist` vs `merge` vs `save`?

**Answer:** `persist` new managed. `merge` copies state onto a managed instance, returns it. Spring Data `save` chooses based on id/isNew.

**Counter:** Why did `merge` return a different instance?  
**Answer:** That’s the contract. Always use the returned reference.

---

### Q9. Optimistic vs pessimistic locking?

**Answer:** `@Version` / ETag — detect lost update. `LockModeType.PESSIMISTIC_WRITE` — `SELECT … FOR UPDATE`. Prefer optimistic for user edits; pessimistic for tight inventory counters if required.

---

### Q10. How do you paginate without blowing memory?

**Answer:** `Pageable` + `Slice` (no count) for infinite scroll. Don’t join-fetch collections on a paged query. Keyset pagination for large offsets.

---

### Q11. Flyway vs `ddl-auto=update`?

**Answer:** Flyway is versioned, reviewable, repeatable in all envs. `update` is not a migration strategy.

---

### Q12. First-level vs second-level cache?

**Answer:** L1 = persistence context, mandatory, per session. L2 = shared across sessions (Hibernate cache, Redis via provider). Spring Data doesn’t magically enable L2. Don’t confuse with `@Cacheable` (chapter 14).

---

### Q13. Why `IDENTITY` hurts batching?

**Answer:** The insert must happen immediately to get the id, so JDBC batching is limited. `SEQUENCE` (or UUID assigned in app) batches better.

---

### Q14. `@Transactional(readOnly = true)` on a repository `find`?

**Answer:** Spring Data already marks query methods transactional read-only. Extra on service is still useful as the **boundary** for several reads and to hint flush mode.

---

### Q15. Entity vs DTO in controllers?

**Answer:** DTO. Entities leak lazy proxies, internals, and persistence annotations, and couple API to schema.
