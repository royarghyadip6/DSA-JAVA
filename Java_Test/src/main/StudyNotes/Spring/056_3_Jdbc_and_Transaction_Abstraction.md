# 56.3 JdbcTemplate and Transaction Abstraction

[← 056_2 MVC Internals](056_2_Spring_MVC_Internals.md) | [Course map](00_COURSE_MAP.md) | **Next:** [056_4 Spring Testing →](056_4_Spring_Testing.md)

Hibernate flush, N+1, locking, and Saga are **not** here. See [061](061_Hibernate_Internals.md) and [062](062_Transactions.md).

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

**JdbcTemplate** = a helper that runs SQL for you. It opens a connection, runs the statement, closes it, and turns ugly `SQLException` into an unchecked Spring exception you can catch if you want.

**Transaction** = a group of SQL statements that must **all succeed or all undo**. Like transferring money: minus from A and plus to B must happen together.

**`@Transactional`** does *not* magically talk to the database by itself. It is a **waiter (AOP proxy)** around your method:

```text
1. Start transaction (borrow a connection, bind it to this thread)
2. Run your method. JdbcTemplate reuses that same connection.
3. If the method throws a RuntimeException → rollback (undo)
4. If it returns normally → commit (save)
```

**Three beginner rules**

1. Put `@Transactional` on a **public** method of a **Spring bean**, called from **another** bean (not `this.otherMethod()`).
2. By default, **checked** exceptions (`throws Exception`) **commit**. That surprises everyone. Use `rollbackFor = Exception.class` if you need otherwise.
3. Catching an exception and not rethrowing → Spring thinks success → **commit**.

**Propagation in one picture:** “If a transaction already exists on this thread, what do I do?”

- `REQUIRED` (default) = join it, or start one
- `REQUIRES_NEW` = pause the old one, start a brand new one (only if called **through the proxy**)

---

## When you interview (5–8 years)

`@Transactional` is AOP. Seniors talk self-invocation vs inner `REQUIRES_NEW`, rollback rules, `DataSourceUtils`, and holding a connection during HTTP calls.

---

## 1. JdbcTemplate

`JdbcTemplate` is a template-method wrapper over JDBC: get connection, run SQL, translate exceptions, close/release.

```java
@Repository
public class OrderJdbcRepository {
    private final JdbcTemplate jdbc;

    public OrderJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Order find(long id) {
        return jdbc.queryForObject(
                "select id, status from orders where id = ?",
                (rs, rowNum) -> new Order(rs.getLong("id"), rs.getString("status")),
                id);
    }
}
```

| API | Use |
|-----|-----|
| `query` / `queryForObject` | SELECT + `RowMapper` |
| `update` | INSERT/UPDATE/DELETE, returns row count |
| `batchUpdate` | Batches (set `rewriteBatchedStatements` on the driver too) |
| `execute` | Generic / CallableStatement |
| `queryForList` | Quick maps — OK for ops, clumsy for domain |

`NamedParameterJdbcTemplate` wraps it when you want `:orderId` instead of `?`.

```java
named.queryForObject(
    "select ... where id = :id",
    new MapSqlParameterSource("id", id),
    mapper);
```

### Exception translation

JDBC `SQLException` is checked. Spring translates to **unchecked** `DataAccessException`:

| Example | Spring type |
|---------|-------------|
| Duplicate key | `DuplicateKeyException` (a `DataIntegrityViolationException`) |
| Bad SQL | `BadSqlGrammarException` |
| Empty result for `queryForObject` | `EmptyResultDataAccessException` |
| More than one row | `IncorrectResultSizeDataAccessException` |

Translation uses `SQLErrorCodeSQLExceptionTranslator` (vendor codes) or `SQLState`. `@Repository` on your DAO adds AOP translation for exceptions you throw from non-template code too.

You do **not** catch `SQLException` in services. You catch `DataAccessException` if you must.

### Connection handling

`JdbcTemplate` calls `DataSourceUtils.getConnection(dataSource)`:

- If a Spring transaction is bound to the thread → reuse that `Connection`
- Else → take one from the pool, and **release** after the statement (not hold for the whole service method)

Without `@Transactional`, each template call can be **auto-commit** on a different connection. Multi-step writes need a transaction.

`DataSource` in production is a pool (HikariCP). JdbcTemplate is thread-safe as a singleton; the pool is shared.

---

## 2. Transaction abstraction

Spring does **not** implement a database. It defines:

```text
PlatformTransactionManager
  ├── DataSourceTransactionManager     JDBC
  ├── JpaTransactionManager            JPA/Hibernate
  ├── JtaTransactionManager            JTA (rare today)
  └── others (Kafka, Mongo, … in Spring Data)
```

```java
@Bean
public PlatformTransactionManager txManager(DataSource ds) {
    return new DataSourceTransactionManager(ds);
}
```

`@EnableTransactionManagement` registers the AOP advisor. **You still must expose a `PlatformTransactionManager` bean.** Boot auto-config does that; Core interviews expect you to know it.

### Declarative: `@Transactional`

```java
@Service
public class TransferService {
    private final JdbcTemplate jdbc;

    public TransferService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void transfer(long from, long to, long cents) {
        jdbc.update("update account set bal = bal - ? where id = ?", cents, from);
        jdbc.update("update account set bal = bal + ? where id = ?", cents, to);
    }
}
```

Rules that come from AOP ([056](056_Spring_AOP.md)):

- Public method (default)
- Call through the **proxy**
- Class or method annotation; method wins
- Interface annotation works only with **JDK proxies** and is fragile — put `@Transactional` on the **class/implementation**

### Programmatic: `TransactionTemplate`

```java
@Service
public class TransferService {
    private final TransactionTemplate tx;

    public TransferService(PlatformTransactionManager tm) {
        this.tx = new TransactionTemplate(tm);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public void transfer(...) {
        tx.executeWithoutResult(status -> {
            // jdbc work
        });
    }
}
```

Use when the tx boundary is dynamic (only some branches), or when you cannot add a proxy (self-invocation you refuse to split — still better to split).

`TransactionCallback` can return a value; `status.setRollbackOnly()`.

---

## 3. Propagation

**Plain English:** your method is about to run. A transaction may already exist on this thread. Propagation is the rule for that situation.

| Type | Behavior |
|------|----------|
| `REQUIRED` (default) | Join existing, else start new |
| `REQUIRES_NEW` | Suspend existing, start a **new** physical transaction, resume after |
| `NESTED` | JDBC savepoint inside the current tx (`DataSourceTransactionManager` supports this). Rollback to savepoint, outer can continue |
| `SUPPORTS` | Use tx if present, else run non-transactional |
| `NOT_SUPPORTED` | Suspend existing, run without tx |
| `MANDATORY` | Must already have a tx, else exception |
| `NEVER` | Must **not** have a tx, else exception |

```text
REQUIRED is 95% of business methods.

REQUIRES_NEW is for:
  audit log that must commit even if business rolls back
  independent unit (careful: now you have two connections/tx)

NESTED is for:
  partial rollback of a batch item without killing the whole batch
  (ORM + nested is trickier — JDBC savepoints are the clean story)
```

**Self-invocation counter-question:** `a()` `@Transactional` calls `this.b()` with `REQUIRES_NEW`. `b`’s annotation is **ignored**. Both run in `a`’s transaction. To get a real new transaction, `b` must be on **another bean** (or `self.b()` through the proxy).

---

## 4. Isolation (Spring’s names)

**Plain English:** isolation = “how much can two people using the database at the same time see of each other’s work?”

Spring only **passes a name** to JDBC. The **database** enforces it. Default `DEFAULT` = “use whatever the database already uses” (often READ_COMMITTED).

| Spring / JDBC | Dirty read | Non-repeatable | Phantom | Typical DB default |
|---------------|------------|----------------|---------|-------------------|
| `READ_UNCOMMITTED` | possible | possible | possible | rarely used |
| `READ_COMMITTED` | no | possible | possible | Oracle, PostgreSQL, SQL Server |
| `REPEATABLE_READ` | no | no | possible (MySQL InnoDB: no via MVCC) | MySQL InnoDB |
| `SERIALIZABLE` | no | no | no | expensive |

Default in Spring: `ISOLATION_DEFAULT` = **whatever the DataSource/DB uses**. You often never set it.

Dirty / non-repeatable / phantom — definitions belong in [062](062_Transactions.md). For Core: know the four names, know default is DB default, know raising isolation increases lock/MVCC cost.

---

## 5. Rollback rules

Default:

```text
RuntimeException and Error → rollback
checked Exception (Exception not Runtime) → COMMIT  ← surprise
```

```java
@Transactional(rollbackFor = Exception.class)
public void process() throws BusinessException { }
```

`noRollbackFor = SomeRuntimeException.class` — commit even though it is unchecked (rare; be explicit in review).

`rollbackFor` uses **class hierarchy**. `rollbackFor = Exception.class` covers checked and runtime.

If you **catch** an exception inside the method and do not rethrow, Spring sees a **normal return → commit**. Catching without `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()` is a common bug.

---

## 6. `readOnly`, timeout, `transactionManager` qualifier

```java
@Transactional(readOnly = true, timeout = 5, transactionManager = "reportingTx")
public List<Row> report() { }
```

**`readOnly = true`:**

- JDBC: `connection.setReadOnly(true)` — driver/DB may optimize; **not a security guarantee**
- Hibernate: skip dirty checking (flush mode) — real benefit, later chapter
- Some teams use it as documentation for reviewers

**`timeout`:** seconds until Spring marks the tx for rollback (and the DB may abort). Default: no timeout (`-1`). Long `@Transactional` holding a connection is a pool-starvation bug.

**Multiple managers:** `@Transactional("txManagerBeanName")` or `@Transactional(transactionManager = "...")`. Qualifier when you have two DataSources.

---

## 7. Thread binding and isolation from async

Transaction resources are stored in `TransactionSynchronizationManager` **ThreadLocal**.

`@Async` / new thread = **no** transaction (unless the async method has its own `@Transactional`).

Never pass a JDBC `Connection` to another thread.

`TransactionSynchronization.afterCommit(Runnable)` — same idea as `@TransactionalEventListener(AFTER_COMMIT)`.

---

## 8. JTA / distributed (one honest paragraph)

`JtaTransactionManager` + XA DataSources can commit two resources atomically. It is operationally heavy. Microservices usually **do not** use JTA; they use Saga/outbox ([062](062_Transactions.md)). For Core, name JTA, say you would not default to it.

---

## Production pitfalls

1. Self-invocation / non-public `@Transactional`.
2. Checked exception → accidental commit.
3. Catching exceptions and still committing.
4. Huge method `@Transactional` that calls HTTP — holds a connection for the network RTT.
5. `REQUIRES_NEW` in a loop — N connections, N commits, pool exhaustion.
6. Assuming `readOnly` blocks writes (it does not reliably).
7. JdbcTemplate without a transaction for two updates.
8. `queryForObject` when 0 rows — exception vs `query` + empty list.
9. SQL string concatenation — injection; always bind parameters.
10. Sharing one `@Transactional` across two DataSources — does **not** make one atomic tx without JTA.

---

## Interview Ready Q&A (5–8 year standard)

The notes used money transfer. **Here, name PlatformTransactionManager, default rollback, NESTED vs REQUIRES_NEW, and test `@Transactional` vs inner commits.**

### Q1. What does JdbcTemplate buy you vs raw JDBC?

**Answer:** Connection/statement cleanup, exception translation to `DataAccessException`, `RowMapper`, batch helpers. You still write SQL.

**Counter:** Why not always JPA?

**Counter-answer:** Bulk updates, reports, CTE/window functions, DBA-tuned SQL, simple apps. JPA is for aggregate graphs. Seniors use both.

---

### Q2. How does JdbcTemplate participate in `@Transactional`?

**Answer:** `DataSourceUtils.getConnection` looks at `TransactionSynchronizationManager`. Same thread-bound connection for all template calls inside the tx.

**Counter:** Two `JdbcTemplate` beans, two DataSources, one `@Transactional`?

**Counter-answer:** Only the DataSource of the **active** `PlatformTransactionManager` is bound. The other template uses auto-commit connections. Not atomic. Need JTA or two explicit transactions.

---

### Q3. How does `@Transactional` work internally?

**Answer:** `@EnableTransactionManagement` registers a `BeanPostProcessor` advisor. Calls through the proxy hit `TransactionInterceptor`, which uses `PlatformTransactionManager` to begin/commit/rollback around `invoke`.

**Counter:** Annotation on the interface vs the class?

**Counter-answer:** Class/impl is reliable. Interface-only works with JDK proxies that intercept the interface method; CGLIB + interface-only is a common “it doesn’t work.” Put it on the concrete class.

---

### Q4. Default rollback rules?

**Answer:** Unchecked (`RuntimeException`, `Error`) rollback. Checked commit.

**Counter:** Why was that chosen?

**Counter-answer:** Historical: checked exceptions were often “business, caller should handle, maybe still commit.” In 2026 many teams use only unchecked and set `rollbackFor = Exception.class` on a composed `@ApplicationService` annotation. Know the default; do not rely on it silently.

---

### Q5. `REQUIRED` vs `REQUIRES_NEW`?

**Answer:** `REQUIRED` joins or starts. `REQUIRES_NEW` always starts a new tx, suspending the outer.

**Counter:** Inner `REQUIRES_NEW` rolls back. What happens to the outer?

**Counter-answer:** Inner is independent — already rolled back. Outer is still active (suspended then resumed). Outer can commit unless you throw from inner **after** resume and the exception hits the outer interceptor. If you **catch** the inner exception, outer may still commit — now you have committed outer + rolled-back inner (audit vs business split). Design that on purpose.

---

### Q6. Self-invocation and `REQUIRES_NEW`?

**Answer:** Inner annotation ignored. No suspend, no new tx.

**Counter:** How do you prove it in a test?

**Counter-answer:** `TransactionSynchronizationManager.getCurrentTransactionName()` / isActualTransactionActive, or a JDBC audit table: if inner insert rolls back with outer, they were the same tx.

---

### Q7. What is `NESTED`?

**Answer:** Savepoint inside the same physical JDBC transaction. Inner rollback returns to savepoint; outer continues.

**Counter:** Does `JpaTransactionManager` nested always work?

**Counter-answer:** It can, via JDBC savepoints on the same connection, but mixing JPA flush and savepoints is easy to get wrong. Nested is the cleanest story with `DataSourceTransactionManager` + JdbcTemplate. For JPA, prefer explicit service splits.

---

### Q8. Programmatic vs declarative transactions?

**Answer:** Declarative (`@Transactional`) for method boundaries. `TransactionTemplate` for dynamic / partial methods or non-bean code.

**Counter:** Can you mix?

**Counter-answer:** Yes. A `@Transactional` method can call `template.execute` with `REQUIRES_NEW` for a nested unit — the template goes through the **manager**, not through `this`, so it **does** start a new tx even inside the same class. That is a valid self-invocation **escape hatch**.

---

### Q9. Isolation `DEFAULT`?

**Answer:** Do not override; use the database’s default (Postgres/Oracle `READ_COMMITTED`, MySQL InnoDB `REPEATABLE_READ`).

**Counter:** When would you set `SERIALIZABLE` in Spring?

**Counter-answer:** Rare, short, proven contention bugs. Prefer explicit `SELECT … FOR UPDATE` or optimistic versions ([062](062_Transactions.md)). Serializing whole methods does not scale.

---

### Q10. `readOnly = true` — does it prevent writes?

**Answer:** Not a guarantee. Hint to driver and to JPA flush. A `jdbc.update` may still succeed depending on DB/driver.

**Counter:** Why set it then?

**Counter-answer:** JPA dirty-checking skip (real), documentation, some replica routing libraries send read-only tx to replicas (Boot/routing DataSource). Never as the only write-protection.

---

### Q11. You caught `Exception` inside `@Transactional` and logged it. DB still changed. Why?

**Answer:** No exception escaped the proxy → commit. Catching swallows the rollback trigger.

**Counter:** How to rollback without rethrowing?

**Counter-answer:** `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()` or rethrow a runtime exception. Rethrow is clearer.

---

### Q12. `EmptyResultDataAccessException` — is that a tx rollback?

**Answer:** It is a `DataAccessException` → **runtime** → **yes**, it rolls back if it leaves the `@Transactional` method. Do not use `queryForObject` for optional rows; `query` + empty list or `Optional`.

**Counter:** Is that exception checked?

**Counter-answer:** No. All `DataAccessException` are unchecked.

---

### Q13. Where should `@Transactional` live — controller or service?

**Answer:** Service (application) layer. Controllers orchestrate HTTP. Transactions around use-cases, not around JSON mapping.

**Counter:** Two services in one controller method, each `@Transactional`?

**Counter-answer:** Two transactions unless the controller is also transactional (usually it should not be) or you have a facade service that wraps both. That facade is the use-case boundary.

---

### Q14. Timeout — what actually happens?

**Answer:** Spring’s transaction timer can mark rollback-only if the method exceeds `timeout`. The DB may also kill the statement. You still hold a connection until the method returns.

**Counter:** External HTTP call inside `@Transactional`?

**Counter-answer:** Anti-pattern. Connection sits idle in the pool’s “active” set. Do I/O outside, then a short tx for writes. Or outbox after commit.

---

### Q15. `@Repository` vs JdbcTemplate translation — redundant?

**Answer:** JdbcTemplate already translates. `@Repository` helps for exceptions from your extra JDBC code and marks the layer. Keep `@Repository`.

**Counter:** Service with JdbcTemplate injected, no repository class?

**Counter-answer:** Works, worse structure. Translation still happens inside the template.

---

### Q16. `SUPPORTS` use case?

**Answer:** A method that can run in or out of a tx (read that should join if the caller has one). Rare. Default `REQUIRED` even for reads is common so you get a consistent snapshot.

**Counter:** `NOT_SUPPORTED`?

**Counter-answer:** Force non-transactional (e.g. send a JMS message that must not enlist). Uncommon in JDBC services.

---

### Q17. Multiple `@Transactional` on class and method?

**Answer:** Method-level **overrides** class-level (does not merge in surprising ways — the method annotation is the source of truth for that method). Unspecified attributes on the method use the annotation’s defaults, **not** the class values, unless you use a composed annotation carefully.

**Counter:** Class `readOnly = true`, method without annotation does a write?

**Counter-answer:** The write method still has the class annotation → readOnly still true. Put `@Transactional(readOnly = false)` on the writer, or do not put readOnly on the class.

---

### Q18. Is `PlatformTransactionManager` thread-safe?

**Answer:** The manager bean is a singleton and thread-safe. State lives in ThreadLocal synchronizations, not on the manager fields.

**Counter:** Same as JdbcTemplate?

**Counter-answer:** Yes — both are stateless facades over thread-bound resources and a pool.

---

### Q19. How do you test that a method is transactional without a real DB?

**Answer:** Integration test with an embedded DB is honest. Unit test: you cannot see AOP without a Spring context. `@SpringJUnitConfig` + `DataSourceTransactionManager` + H2, or assert interceptor with a mock manager. See [056_4](056_4_Spring_Testing.md).

**Counter:** Mockito `@InjectMocks` on the service?

**Counter-answer:** No proxy → `@Transactional` is a no-op. Tests pass and production rolls back differently.

---

### Q20. Spring 6 `TransactionTemplate` vs `TransactionalOperator` (reactive)?

**Answer:** `TransactionalOperator` is WebFlux/R2DBC. Servlet + JDBC still `PlatformTransactionManager`. Do not mix reactive tx with JdbcTemplate on a servlet thread.

**Counter:** Virtual threads (Java 21) — does ThreadLocal tx still work?

**Counter-answer:** Yes — virtual threads still have ThreadLocal. Pinning is a different issue. Do not share connections across threads.

---

### Interview one-liner

> JdbcTemplate runs SQL and translates `SQLException` to `DataAccessException`; inside a tx it reuses the thread-bound connection. `@Transactional` is `TransactionInterceptor` + `PlatformTransactionManager`. Default propagation `REQUIRED`; default rollback = runtime only. Self-invocation ignores inner `REQUIRES_NEW`. `readOnly` is a hint. Catch-and-not-rethrow commits.
