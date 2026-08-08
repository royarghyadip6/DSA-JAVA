# 60. Spring Data JPA

## Basics

---

# 1. What is JPA?

<details>
<summary>Show Answer</summary>

**Answer:**

**JPA** (Java Persistence API) is a **Java specification** (JSR 338) for **ORM** — mapping Java objects to relational database tables. It defines annotations, EntityManager API, and query standards.

```text
Java Object (Entity)  ←→  JPA API  ←→  Database Table
```

| Concept | JPA Term | DB Term |
|---------|----------|---------|
| Class | Entity | Table |
| Field | Attribute | Column |
| Object | Entity instance | Row |
| Reference | Association | Foreign Key |

```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal salary;
}
```

**Interview Point:**

> JPA is a **specification**, not an implementation. Hibernate, EclipseLink, and OpenJPA are implementations.

</details>

---

# 2. What is Hibernate?

<details>
<summary>Show Answer</summary>

**Answer:**

**Hibernate** is the most popular **JPA implementation**. It provides the actual ORM engine — SQL generation, caching, lazy loading, dirty checking.

```text
Your Code → Spring Data JPA → Hibernate → JDBC → Database
```

| Feature | Hibernate Provides |
|---------|-------------------|
| SQL generation | Auto DDL, DML from entity state |
| Caching | L1 (session) + L2 (cluster) |
| Lazy loading | Proxy-based deferred loading |
| Dirty checking | Auto UPDATE on changed fields |

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Interview Point:**

> Hibernate does the heavy lifting. Spring Data JPA adds repository abstraction on top of Hibernate's EntityManager.

</details>

---

# 3. Difference between JPA and Hibernate?

<details>
<summary>Show Answer</summary>

**Answer:**

| | JPA | Hibernate |
|---|-----|-----------|
| Type | Specification (interface) | Implementation |
| Analogy | JDBC API | MySQL Driver |
| Defines | `@Entity`, `@Id`, EntityManager | Session, SessionFactory, extra features |
| Portability | Switch implementations easily | Hibernate-specific APIs lock you in |

```java
// JPA standard
@PersistenceContext
EntityManager em;

// Hibernate-specific (avoid in portable code)
Session session = em.unwrap(Session.class);
```

| JPA Only | Hibernate Extra |
|----------|-----------------|
| `@Entity`, `@Table` | `@Formula`, `@Where`, `@Filter` |
| `EntityManager` | `Session`, `StatelessSession` |
| JPQL | HQL (mostly same) |

**Interview Point:**

> Code against JPA annotations and APIs. Use Hibernate-specific features only when JPA doesn't cover your need.

</details>

---

# 4. Why JPA?

<details>
<summary>Show Answer</summary>

**Answer:**

JPA eliminates boilerplate JDBC code and provides object-oriented data access.

| Without JPA (JDBC) | With JPA |
|--------------------|----------|
| Manual SQL for every query | Auto-generated from entity |
| Manual row-to-object mapping | Automatic mapping |
| Manual connection management | Managed by Spring/Hibernate |
| No relationship navigation | `order.getCustomer().getName()` |

```java
// JDBC — verbose
String sql = "SELECT id, name, salary FROM employees WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
Employee emp = new Employee(rs.getLong("id"), rs.getString("name"), ...);

// JPA — one line
Employee emp = employeeRepo.findById(id).orElseThrow();
```

**Interview Point:**

> JPA trades control for productivity. For complex reporting queries, use native SQL or `@Query` alongside JPA.

</details>

---

## Entity

---

# 5. @Entity

<details>
<summary>Show Answer</summary>

**Answer:**

`@Entity` marks a class as a **JPA-managed entity** mapped to a database table.

```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;
}
```

| Rule | Detail |
|------|--------|
| No-arg constructor | Required (can be protected) |
| Not final class | Hibernate needs to subclass for proxies |
| Not final fields (for lazy) | Lazy loading needs proxy override |

**Interview Point:**

> Every entity needs `@Entity` + `@Id`. Class name defaults to table name (can override with `@Table`).

</details>

---

# 6. @Table

<details>
<summary>Show Answer</summary>

**Answer:**

`@Table` customizes the database table mapping for an entity.

```java
@Entity
@Table(
    name = "tbl_order",
    schema = "sales",
    uniqueConstraints = @UniqueConstraint(columnNames = {"order_number"}),
    indexes = @Index(name = "idx_order_status", columnList = "status")
)
public class Order {
    @Column(name = "order_number")
    private String orderNumber;
}
```

| Attribute | Purpose |
|-----------|---------|
| `name` | Table name (default = class name) |
| `schema` | DB schema |
| `uniqueConstraints` | Composite unique keys |
| `indexes` | Index definitions |

**Interview Point:**

> Use `@Table(name=...)` when DB table name differs from Java class name (legacy databases).

</details>

---

# 7. @Id

<details>
<summary>Show Answer</summary>

**Answer:**

`@Id` marks the **primary key** field of an entity.

```java
@Entity
public class Customer {
    @Id
    private Long id;  // assigned manually

    // or auto-generated:
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

| Strategy | How | Best For |
|----------|-----|----------|
| `IDENTITY` | DB auto-increment | MySQL, PostgreSQL serial |
| `SEQUENCE` | DB sequence | Oracle, PostgreSQL |
| `TABLE` | Separate ID table | Portable but slow |
| `AUTO` | Provider decides | Default, let Hibernate choose |

**Interview Point:**

> `IDENTITY` is most common with MySQL/PostgreSQL. `SEQUENCE` preferred for Oracle. Composite keys use `@IdClass` or `@EmbeddedId`.

</details>

---

# 8. @GeneratedValue

<details>
<summary>Show Answer</summary>

**Answer:**

`@GeneratedValue` configures **automatic primary key generation**.

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
@SequenceGenerator(name = "order_seq", sequenceName = "order_id_seq", allocationSize = 50)
private Long id;
```

| Strategy | SQL Generated | Notes |
|----------|----------------|-------|
| `IDENTITY` | `INSERT ... RETURNING id` | Batch insert breaks with IDENTITY |
| `SEQUENCE` | `SELECT nextval('seq')` | `allocationSize` reduces round trips |
| `TABLE` | `SELECT id FROM id_table` | Portable, poor performance |
| `AUTO` | Provider-specific | Hibernate picks best for dialect |

```properties
# Production tip — use SEQUENCE with allocationSize for bulk inserts
spring.jpa.properties.hibernate.id.new_generator_mappings=true
```

**Interview Point:**

> `allocationSize=50` fetches 50 IDs at once — fewer DB round trips. Must match DB sequence increment.

</details>

---

## Relationships

---

# 9. OneToOne

<details>
<summary>Show Answer</summary>

**Answer:**

`@OneToOne` maps a one-to-one relationship between two entities.

```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private UserProfile profile;
}

@Entity
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;
    private String avatarUrl;

    @OneToOne(mappedBy = "profile")
    private User user;
}
```

| Side | Annotation | Has FK? |
|------|-----------|---------|
| Owning | `@JoinColumn` | ✅ Yes |
| Inverse | `mappedBy` | ❌ No |

**Interview Point:**

> One side owns the FK (`@JoinColumn`). Other side uses `mappedBy`. Default fetch = EAGER for `@OneToOne` — set LAZY explicitly.

</details>

---

# 10. OneToMany

<details>
<summary>Show Answer</summary>

**Answer:**

`@OneToMany` maps a parent entity to a collection of child entities.

```java
@Entity
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();
}

@Entity
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
```

| Annotation | Side | FK Location |
|------------|------|-------------|
| `@OneToMany` | Parent (collection) | Child table |
| `@ManyToOne` | Child (single ref) | Child table |

**Interview Point:**

> `@OneToMany` without `mappedBy` creates a join table — usually not what you want. Always pair with `@ManyToOne` on child.

</details>

---

# 11. ManyToOne

<details>
<summary>Show Answer</summary>

**Answer:**

`@ManyToOne` is the **owning side** of a many-to-one relationship — multiple entities reference one parent.

```java
@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}
```

| Attribute | Purpose |
|-----------|---------|
| `fetch = LAZY` | Load parent only when accessed |
| `optional = false` | NOT NULL FK constraint |
| `@JoinColumn` | FK column name |

**Interview Point:**

> `@ManyToOne` is the owning side — it has the FK column. Default fetch is EAGER — always set LAZY in production.

</details>

---

# 12. ManyToMany

<details>
<summary>Show Answer</summary>

**Answer:**

`@ManyToMany` maps entities with a many-to-many relationship via a **join table**.

```java
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}

@Entity
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

| Approach | When |
|----------|------|
| `@ManyToMany` | Simple associations, no extra columns |
| **Explicit join entity** | Need extra fields (enrolledDate, grade) |

```java
// Production preferred — explicit join entity
@Entity
public class Enrollment {
    @EmbeddedId
    private EnrollmentId id;

    private LocalDate enrolledDate;
    private String grade;

    @ManyToOne @MapsId("studentId") @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne @MapsId("courseId") @JoinColumn(name = "course_id")
    private Course course;
}
```

**Interview Point:**

> Avoid `@ManyToMany` in production when join table has extra columns. Use explicit join entity with `@EmbeddedId`.

</details>

---

## Frequently Asked

---

# 13. mappedBy?

<details>
<summary>Show Answer</summary>

**Answer:**

`mappedBy` marks the **inverse (non-owning) side** of a bidirectional relationship. The owning side manages the FK.

```java
// OWNING side — has FK, no mappedBy
@ManyToOne
@JoinColumn(name = "department_id")
private Department department;

// INVERSE side — mappedBy points to owning field name
@OneToMany(mappedBy = "department")
private List<Employee> employees;
```

| Side | Has FK? | Has mappedBy? | Persists changes? |
|------|---------|---------------|-------------------|
| Owning | ✅ | ❌ | ✅ Yes |
| Inverse | ❌ | ✅ | ❌ No (unless synced) |

```java
// Bidirectional sync helper — production best practice
public void addEmployee(Employee emp) {
    employees.add(emp);
    emp.setDepartment(this);  // sync both sides
}
```

**Interview Point:**

> `mappedBy` value = field name on the owning side. Changes on inverse side alone won't update FK — sync both sides.

</details>

---

# 14. Owning side?

<details>
<summary>Show Answer</summary>

**Answer:**

The **owning side** is the entity that has the **foreign key column** in the database. Only the owning side's changes are persisted.

| Relationship | Owning Side | FK Location |
|-------------|-------------|-------------|
| `@OneToOne` | Side with `@JoinColumn` | That entity's table |
| `@OneToMany` / `@ManyToOne` | `@ManyToOne` side | Many side's table |
| `@ManyToMany` | Side without `mappedBy` | Join table |

```java
// OWNING — this UPDATE actually changes department_id in DB
employee.setDepartment(newDept);
employeeRepo.save(employee);

// INVERSE only — this does NOT update FK!
dept.getEmployees().add(employee);  // ❌ FK not updated
dept.getEmployees().add(employee);
employee.setDepartment(dept);       // ✅ sync owning side
```

**Interview Point:**

> Always modify the owning side to persist relationship changes. Use helper methods to keep both sides in sync.

</details>

---

# 15. Cascade types?

<details>
<summary>Show Answer</summary>

**Answer:**

**Cascade** propagates persistence operations from parent to child entities.

| Cascade Type | Effect |
|-------------|--------|
| `PERSIST` | `save(parent)` also persists new children |
| `MERGE` | `merge(parent)` also merges children |
| `REMOVE` | `delete(parent)` also deletes children |
| `REFRESH` | `refresh(parent)` also refreshes children |
| `DETACH` | `detach(parent)` also detaches children |
| `ALL` | All of the above |

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
private List<OrderItem> items = new ArrayList<>();

// Saving order also persists items
Order order = new Order();
order.addItem(new OrderItem("Product A", 2));
orderRepo.save(order);  // INSERT order + INSERT items
```

| Use | Don't Use |
|-----|-----------|
| Parent-child composition (Order → Items) | `@ManyToOne` with `CascadeType.REMOVE` on shared entities |
| `CascadeType.ALL` on true aggregates | Cascade on `@ManyToMany` |

**Interview Point:**

> `CascadeType.REMOVE` on `@ManyToOne` is dangerous — deleting one OrderItem could delete the shared Product. Use only on composition.

</details>

---

# 16. orphanRemoval?

<details>
<summary>Show Answer</summary>

**Answer:**

`orphanRemoval = true` automatically **deletes child entities** removed from the parent's collection.

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();

// Remove item from collection → DELETE from DB
order.getItems().remove(item);  // item row deleted on flush
orderRepo.save(order);
```

| | `cascade = REMOVE` | `orphanRemoval = true` |
|---|-------------------|----------------------|
| Trigger | `delete(parent)` | Remove child from collection |
| Scope | All children when parent deleted | Only removed children |

```java
// orphanRemoval requires owning side management
public void removeItem(OrderItem item) {
    items.remove(item);
    item.setOrder(null);  // break bidirectional link
}
```

**Interview Point:**

> `orphanRemoval` = "delete children nobody wants anymore". Only works when parent owns the lifecycle (composition, not association).

</details>

---

## Production Depth — Spring Data JPA

---

# P1. Spring Data JPA Repository Hierarchy

<details>
<summary>Show Answer</summary>

**Answer:**

Spring Data JPA provides repository interfaces that eliminate boilerplate DAO code.

```text
Repository (marker)
  └── CrudRepository<T, ID>
       └── PagingAndSortingRepository<T, ID>
            └── JpaRepository<T, ID>
```

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Derived query — Spring generates JPQL from method name
    List<Order> findByStatusAndCreatedAtAfter(OrderStatus status, Instant after);

    // Custom JPQL
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // Native SQL
    @Query(value = "SELECT * FROM orders WHERE status = :status", nativeQuery = true)
    List<Order> findByStatusNative(@Param("status") String status);

    // Projection — only fetch needed columns
    @Query("SELECT new com.example.OrderSummary(o.id, o.status, o.total) FROM Order o")
    List<OrderSummary> findAllSummaries();
}
```

| Method | Source |
|--------|--------|
| `findByStatus(...)` | Derived from method name |
| `@Query("...")` | Custom JPQL or native SQL |
| `@Modifying @Query` | UPDATE/DELETE queries |

**Interview Point:**

> Start with derived queries. Move to `@Query` when method names get too long. Use projections/DTOs to avoid loading full entities.

</details>

---

# P2. N+1 Prevention at Repository Level

<details>
<summary>Show Answer</summary>

**Answer:**

| Technique | How | When |
|-----------|-----|------|
| `JOIN FETCH` | `@Query("SELECT o FROM Order o JOIN FETCH o.items")` | Known association needed |
| `@EntityGraph` | `@EntityGraph(attributePaths = {"items"})` | Reusable fetch plan |
| Batch fetching | `hibernate.default_batch_fetch_size=16` | Lazy collections accessed in loop |
| DTO projection | `@Query` returning DTO | Read-only list views |

```java
@EntityGraph(attributePaths = {"customer", "items"})
@Query("SELECT o FROM Order o WHERE o.status = :status")
List<Order> findByStatusWithDetails(@Param("status") OrderStatus status);
```

```properties
# Batch fetch — when lazy collections accessed in loop
spring.jpa.properties.hibernate.default_batch_fetch_size=16
```

**Interview Point:**

> N+1 = 1 query for list + N queries for each association. Fix with JOIN FETCH, EntityGraph, or batch size.

</details>

---

# P3. Auditing and Projections

<details>
<summary>Show Answer</summary>

**Answer:**

```java
// Auditing — auto-populate created/updated timestamps
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    @CreatedBy
    private String createdBy;
}

// Enable in config
@EnableJpaAuditing
@SpringBootApplication
public class Application { }

// Interface-based projection
public interface OrderSummary {
    Long getId();
    OrderStatus getStatus();
    BigDecimal getTotal();
}

List<OrderSummary> findByStatus(OrderStatus status);
```

**Interview Point:**

> Use interface projections for read-only APIs — Hibernate generates SELECT only needed columns. `@EnableJpaAuditing` for audit fields.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: JPA vs Hibernate vs Spring Data JPA?

<details>
<summary>Show Answer</summary>

**Answer:**

**JPA** = spec. **Hibernate** = implementation. **Spring Data JPA** = repository abstraction on top of Hibernate.

</details>

---

### Q: Which side owns the FK in @OneToMany?

<details>
<summary>Show Answer</summary>

**Answer:**

The `@ManyToOne` side (child) owns the FK. `@OneToMany` is the inverse side with `mappedBy`.

</details>

---

### Q: cascade vs orphanRemoval?

<details>
<summary>Show Answer</summary>

**Answer:**

**Cascade** propagates operations (save/delete) to children. **orphanRemoval** deletes children removed from parent's collection.

</details>

---

### Q: IDENTITY vs SEQUENCE?

<details>
<summary>Show Answer</summary>

**Answer:**

**IDENTITY** = DB auto-increment (MySQL/PostgreSQL). **SEQUENCE** = separate sequence object (Oracle, PostgreSQL). SEQUENCE with `allocationSize` is better for batch inserts.

</details>

---

### Q: Why avoid @ManyToMany in production?

<details>
<summary>Show Answer</summary>

**Answer:**

Join table often needs extra columns (date, status). Use explicit join entity with `@EmbeddedId` instead.

</details>

---

### Q: Default fetch type for @ManyToOne?

<details>
<summary>Show Answer</summary>

**Answer:**

**EAGER** — always override to `LAZY` in production to avoid unnecessary joins.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> JPA = spec, Hibernate = impl, Spring Data JPA = repos. @Entity + @Id + @GeneratedValue. @ManyToOne owns FK. mappedBy = inverse side. Cascade propagates ops; orphanRemoval deletes removed children. LAZY fetch everywhere. Avoid @ManyToMany — use join entity. JOIN FETCH / EntityGraph for N+1. Projections for read-only views.

</details>
