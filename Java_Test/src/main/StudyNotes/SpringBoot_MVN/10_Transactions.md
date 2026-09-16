# 10. Transactions

## Start here (simple English)

**In one sentence:** A transaction means **all of these database steps succeed together, or none of them stay**. Transfer money: debit A and credit B — never debit without credit.

**Everyday picture:** An ATM.

- You ask for ₹500. The machine either gives cash **and** reduces the balance, or it does **nothing**. It must not reduce the balance and then jam with no cash.

**In Spring you write:**

```java
@Service
public class TransferService {
    @Transactional
    public void transfer(long from, long to, Money amount) {
        // several repository calls — one commit at the end
    }
}
```

`@Transactional` is **not** a database keyword by itself. It is a **proxy** (chapter 08) that:

1. Borrows a DB connection
2. Runs your method
3. **Commit** if the method returns
4. **Rollback** if a `RuntimeException` escapes (default)

**Must be true:**

- Public method
- Call from **another bean** (not `this.transfer()`)
- The class is a Spring bean (`@Service`)

**ACID in plain words:** All-or-nothing, rules stay valid, concurrent users don’t scramble rows, committed data survives a crash.

Interview Q&A is **5–8 year standard** (propagation, isolation, `UnexpectedRollbackException`).

---

A transaction is a unit of work: **all commit or all roll back**.

If you cannot explain **self-invocation**, **propagation**, and **rollback rules**, you are not done.

---

## 1. ACID

| | Meaning |
|--|---------|
| Atomicity | All ops or none |
| Consistency | Constraints hold after commit |
| Isolation | Concurrent TXs don’t see illegal intermediate states |
| Durability | After commit, data survives crash |

Isolation is where interviews go next (dirty read, non-repeatable, phantom).

---

## 2. How `@Transactional` works

```java
@Service
public class TransferService {
    private final AccountRepository accounts;

    @Transactional
    public void transfer(long from, long to, Money amount) {
        Account a = accounts.findById(from).orElseThrow();
        Account b = accounts.findById(to).orElseThrow();
        a.debit(amount);
        b.credit(amount);
    }
}
```

Call through the **Spring proxy**:

```text
begin (get connection from Hikari, set autocommit false)
  persist context open
  method body
  flush
commit  (or rollback on RuntimeException)
release connection
```

Infrastructure: `TransactionInterceptor` + `JpaTransactionManager` (Boot auto-config) or `DataSourceTransactionManager` for JDBC-only.

**Thread-bound:** `TransactionSynchronizationManager`. The connection is **not** passed as a method argument; it sits in a ThreadLocal. That’s why `@Async` methods **do not** join the caller’s transaction — new thread, empty ThreadLocal.

---

## 3. Where to put it

| Layer | `@Transactional`? |
|-------|-------------------|
| Controller | No — holds TX while HTTP runs |
| Service | **Yes — business boundary** |
| Repository | Spring Data already has it per method; don’t rely on it as the business TX |
| Entity | No |

One service method = one business TX unless you explicitly nest.

---

## 4. Rollback rules

**Default:** rollback on **unchecked** (`RuntimeException`, `Error`). **Not** on checked exceptions.

```java
@Transactional(rollbackFor = Exception.class)
public void save() throws Exception { ... }
```

`noRollbackFor` for exceptions you want to commit despite throw (rare; usually a design smell).

If you **catch** an exception inside the method and don’t rethrow, Spring thinks success → **commit**. The interceptor only sees what **leaves** the method.

`setRollbackOnly()` / `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()` when you catch but still want rollback.

After a rollback-only TX, attempting commit throws `UnexpectedRollbackException`. Classic: inner `REQUIRES_NEW` vs `REQUIRED` catching inner failure incorrectly.

---

## 5. Propagation

| Propagation | Behavior |
|-------------|----------|
| `REQUIRED` (default) | Join existing TX, or start one |
| `REQUIRES_NEW` | Suspend current, start a **new** TX (new connection from pool) |
| `SUPPORTS` | Join if exists, else non-transactional |
| `NOT_SUPPORTED` | Suspend current, run without TX |
| `MANDATORY` | Must already have a TX, else fail |
| `NEVER` | Fail if TX exists |
| `NESTED` | JDBC savepoint inside the same TX (not a second DB TX). JPA support is limited / vendor-specific |

**`REQUIRED` (almost always):** inner `@Transactional` on another bean joins. One commit.

**`REQUIRES_NEW`:** logging, audit that **must** persist even if the outer rolls back. Uses a **second** connection — deadlock risk if both lock the same rows.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void audit(String event) { auditRepo.save(...); }
```

Call `audit` through **its own bean proxy**. `this.audit()` does nothing special.

There is no true nested independent TX with `NESTED` on JPA the way people hope. Prefer `REQUIRES_NEW` or outbox pattern.

---

## 6. Isolation

| Level | Dirty read | Non-repeatable | Phantom | Typical |
|-------|------------|----------------|---------|---------|
| `READ_UNCOMMITTED` | yes | yes | yes | almost never |
| `READ_COMMITTED` | no | yes | yes | Postgres/Oracle default |
| `REPEATABLE_READ` | no | no | depends (MySQL RR ≈ snapshot) | MySQL InnoDB default |
| `SERIALIZABLE` | no | no | no | rare, slow |

Spring: `@Transactional(isolation = Isolation.REPEATABLE_READ)`. If the DB can’t do it, you get an error or a silent upgrade depending on driver.

**Lost update:** two TXs read-modify-write the same row. Fix with `@Version`, `UPDATE … SET bal = bal - 10 WHERE bal >= 10`, or pessimistic lock.

Don’t raise isolation “to be safe” globally. Cost is locks and latency.

---

## 7. `readOnly = true`

```java
@Transactional(readOnly = true)
public Order get(long id) { ... }
```

Hints:

- Hibernate flush mode MANUAL / skip dirty checks
- JDBC connection `setReadOnly(true)` — some DBs route to replicas

It is **not** a security guarantee. Writes might still go through and fail late. Don’t rely on it instead of not calling `save`.

---

## 8. Timeout, labels

`@Transactional(timeout = 5)` — seconds. Cancels according to the manager; don’t assume it kills a stuck SQL immediately on every database.

`transactionManager = "ordersTxManager"` when you have **two** DataSources. Boot’s default name is `transactionManager`. Chained / JTA (Atomikos, Narayana) for true XA — avoid if a **saga / outbox** will do.

---

## 9. Self-invocation and proxy rules (short)

Same as chapter 08:

- `this.transfer()` → no interceptor → no TX (autocommit per statement)
- `private` / `final` methods → not intercepted
- `new TransferService()` → no TX
- Class vs interface proxy: call through the interface

**Checked:** if you enable `spring.aop.proxy-target-class=true` (Boot default), CGLIB subclass intercepts public methods on the class.

---

## 10. JPA + TX: flush, Lazy, OSIV

The persistence context is bound to the TX (or to the request if OSIV).

- Commit → flush → SQL → commit connection
- Rollback → discard context, rollback connection
- Lazy load needs an open EM → still in TX (or OSIV)

`@Transactional` on a **read** that returns an entity with lazy collections, then the controller touches them with OSIV **false** → `LazyInitializationException`. Fetch inside the TX or map to DTO inside the TX.

---

## 11. Programmatic transactions

```java
private final TransactionTemplate tx;

public void run() {
    tx.executeWithoutResult(status -> {
        // ...
    });
}
```

Use when TX boundaries are dynamic (loop of independent TXs in a job). Don’t mix annotations and templates on the same method without a clear story.

`PlatformTransactionManager.getTransaction` / `commit` / `rollback` is the low-level API.

---

## 12. Multi-thread / `@Async`

New thread → no TX context. `@Transactional` on the `@Async` method starts a **new** TX on that thread. The caller’s TX may already have committed.

Don’t pass a managed entity to another thread. Detached or IDs only.

---

## 13. Production pitfalls

1. Giant `@Transactional` around HTTP + remote calls — holds Hikari connections during `WebClient` I/O → pool exhaustion.
2. Catching `Exception` and not rethrowing → accidental commit.
3. `REQUIRES_NEW` for everything “to be safe” → deadlocks, pool pressure.
4. Assuming checked exceptions roll back.
5. Two DataSources without specifying `transactionManager`.
6. `@Transactional` on a `@RestController` class.
7. Long read-only TX + streaming millions of rows — still holds a connection; use pagination / JDBC cursor with care.

---

# Interview Q&A (5–8 year bar)

A fresher says “all or nothing.” A 5–8 year answer covers rollback rules, `REQUIRED` vs `REQUIRES_NEW`, and why catching an exception caused a commit.

### Q1. What does `@Transactional` do?

**Answer:** A proxy starts a Spring transaction before the method and commits or rolls back after. The JDBC connection (and JPA EM) is bound to the thread.

**Counter:** Is it a database feature or a Spring feature?  
**Answer:** Both. Spring demarcates; the DB enforces ACID on that connection.

---

### Q2. Why is my `@Transactional` ignored?

**Answer:** Self-invocation, non-public method, not a Spring bean, or calling from the same class. Chapter 08.

**Counter:** Does it work on `protected` with CGLIB?  
**Answer:** CGLIB can override protected; Spring’s TX interceptor historically targets **public** methods for `@Transactional` (proxy mode). In **proxy mode**, only public. AspectJ mode can intercept protected/private. Boot default = proxy → **public only**.

---

### Q3. Default rollback?

**Answer:** Unchecked exceptions. Checked do **not** roll back unless `rollbackFor`.

**Counter:** I caught `RuntimeException` inside the method. Commit or rollback?  
**Answer:** Commit — the proxy never saw the exception. Use `setRollbackOnly` or rethrow.

---

### Q4. `REQUIRED` vs `REQUIRES_NEW`?

**Answer:** `REQUIRED` joins. `REQUIRES_NEW` suspends and opens a second TX/connection.

**Counter:** Inner `REQUIRES_NEW` throws, outer catches. What is committed?  
**Answer:** Inner rolled back (its own TX). Outer still **active** and can commit its own work. If inner was `REQUIRED`, marking rollback-only poisons the **shared** TX → outer commit fails with `UnexpectedRollbackException`.

---

### Q5. Isolation levels — dirty vs non-repeatable vs phantom?

**Answer:** Dirty = read uncommitted. Non-repeatable = same row changes between reads. Phantom = new rows appear in a range. `READ_COMMITTED` prevents dirty. `REPEATABLE_READ` / snapshots prevent non-repeatable. Phantoms need serializable or predicate locks / snapshot depending on DB.

---

### Q6. `readOnly = true`?

**Answer:** Optimization hint (flush skip, possible read replica). Not a substitute for not writing.

---

### Q7. Optimistic locking?

**Answer:** `@Version` column. Concurrent writers: one commits, the other fails on flush. Map to 409.

**Counter:** vs pessimistic?  
**Answer:** Pessimistic `FOR UPDATE` blocks. Use for hot rows with high contention when retries are worse than waiting.

---

### Q8. Can one `@Transactional` span two databases?

**Answer:** Not with a single `JpaTransactionManager`. You need JTA/XA or (better) choreography: outbox, saga. Two managers without XA = two independent TXs pretending.

---

### Q9. `@Transactional` on a controller?

**Answer:** Works technically (proxy). Bad: long TX, lazy loads during JSON, mixed layers. Put it on the service.

---

### Q10. How does this interact with Hikari?

**Answer:** TX start **borrows** a connection; commit/rollback **returns** it. Nested `REQUIRED` reuses the same connection. `REQUIRES_NEW` borrows a second. Slow TX = smaller effective pool.

---

### Q11. `NESTED` propagation?

**Answer:** Savepoints on the **same** connection. Rollback to savepoint without aborting outer. JPA/Hibernate support is not the headline feature; many teams never use it.

---

### Q12. Programmatic vs declarative?

**Answer:** Annotation for static boundaries. `TransactionTemplate` for loops / conditional TX. Same manager underneath.

---

### Q13. What is `UnexpectedRollbackException`?

**Answer:** Outer TX was marked rollback-only (often inner `REQUIRED` failed) but code still tried to commit. Don’t catch inner failures without understanding propagation.

---

### Q14. Does `@Transactional` work with Mongo / Kafka?

**Answer:** Different managers. Spring Kafka has TX with `KafkaTransactionManager`; combining with JDBC is a dedicated pattern (`ChainedTransactionManager` is deprecated; use synchronization or outbox). Don’t assume one annotation covers Kafka + DB.

---

### Q15. ThreadLocal and `@Async`?

**Answer:** TX context does not propagate to the async thread. The async method starts its own TX if annotated. Entities from the caller are detached or illegally used across threads — pass ids.
