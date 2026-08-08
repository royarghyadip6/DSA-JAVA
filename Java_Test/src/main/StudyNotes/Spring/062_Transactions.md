# 62. Transactions

## Extremely Important

---

# 1. What is transaction?

<details>
<summary>Show Answer</summary>

**Answer:**

A **transaction** is a **unit of work** that either **completes entirely** or **rolls back entirely**. It groups multiple database operations into one atomic action.

```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId).orElseThrow();
    Account to = accountRepo.findById(toId).orElseThrow();

    from.debit(amount);   // UPDATE accounts SET balance = balance - 100
    to.credit(amount);    // UPDATE accounts SET balance = balance + 100
    // Both succeed or both rollback
}
```

```text
Without transaction:
  Debit succeeds → server crash → credit never happens → money lost!

With transaction:
  Debit succeeds → server crash → ROLLBACK → debit undone → money safe
```

| Property | Meaning |
|----------|---------|
| Atomic | All or nothing |
| Consistent | DB moves from valid state to valid state |
| Isolated | Concurrent transactions don't interfere |
| Durable | Committed data survives crashes |

**Interview Point:**

> Transaction = all-or-nothing. Spring `@Transactional` wraps method in begin/commit/rollback automatically.

</details>

---

# 2. ACID properties?

<details>
<summary>Show Answer</summary>

**Answer:**

ACID is the set of guarantees a transaction provides.

| Property | Meaning | Example |
|----------|---------|---------|
| **A**tomicity | All operations succeed or all fail | Transfer: debit + credit both happen or neither |
| **C**onsistency | DB rules always maintained | Balance never goes negative (constraint) |
| **I**solation | Concurrent txs don't see each other's uncommitted data | Two transfers don't corrupt same account |
| **D**urability | Committed data persists after crash | After COMMIT, data survives power failure |

```java
@Transactional
public void placeOrder(OrderRequest req) {
    Order order = orderRepo.save(new Order(req));       // A: both or neither
    inventoryService.reserve(req.getProductId(), req.getQty());  // C: stock >= 0
    paymentService.charge(req.getPayment());             // I: other txs don't see partial state
    // D: after commit, order is permanent
}
```

```text
Atomicity:     BEGIN → op1 → op2 → COMMIT (or ROLLBACK on any failure)
Consistency:   CHECK constraints, FK, business rules enforced
Isolation:     Tx1 and Tx2 run as if sequential (depending on level)
Durability:    WAL/redo logs ensure committed data survives crash
```

**Interview Point:**

> ACID = database reliability contract. Spring `@Transactional` provides atomicity. Isolation level controls concurrency behavior.

</details>

---

## Spring Transactions

---

# 3. @Transactional

<details>
<summary>Show Answer</summary>

**Answer:**

`@Transactional` declares that a method (or class) runs inside a **database transaction**. Spring creates a proxy that manages begin/commit/rollback.

```java
@Service
public class OrderService {

    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        Order order = orderRepo.save(new Order(req));
        inventoryService.reserve(req.getProductId(), req.getQuantity());
        return order;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepo.findById(id).orElseThrow();
    }
}
```

| Attribute | Default | Purpose |
|-----------|---------|---------|
| `propagation` | `REQUIRED` | How tx behaves with existing tx |
| `isolation` | `DEFAULT` | DB default isolation level |
| `readOnly` | `false` | Optimize for read-only |
| `rollbackFor` | RuntimeException | Which exceptions trigger rollback |
| `timeout` | -1 (none) | Max seconds before rollback |
| `noRollbackFor` | — | Exceptions that don't trigger rollback |

```java
@Transactional(rollbackFor = Exception.class)  // rollback on checked exceptions too
public void processPayment(Payment payment) throws PaymentException { ... }
```

### How It Works Internally

```text
Client → Proxy → begin tx → actual method → commit/rollback
                    ↑
              TransactionManager
              (PlatformTransactionManager)
```

**Interview Point:**

> `@Transactional` only works on **public methods called through Spring proxy**. Self-invocation (`this.method()`) bypasses proxy — tx won't start.

</details>

---

# 4. Propagation types?

<details>
<summary>Show Answer</summary>

**Answer:**

**Propagation** defines how a transactional method behaves when called from another transactional method.

| Propagation | Behavior |
|-------------|----------|
| `REQUIRED` | Join existing tx, or create new (default) |
| `REQUIRES_NEW` | Always create new tx, suspend current |
| `SUPPORTS` | Join if exists, non-tx if not |
| `MANDATORY` | Must run in existing tx, else exception |
| `NOT_SUPPORTED` | Run non-tx, suspend existing |
| `NEVER` | Must NOT run in tx, else exception |
| `NESTED` | Nested tx with savepoint (if supported) |

```java
@Transactional(propagation = Propagation.REQUIRED)
public void processOrder(Order order) {
    orderRepo.save(order);                    // in tx A
    auditService.log("Order created");        // joins tx A
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(String message) {
    auditRepo.save(new AuditLog(message));    // separate tx B
    // commits independently — survives even if outer tx rolls back
}
```

```text
REQUIRED (default):
  Caller has tx? → join it
  No tx?         → create new

REQUIRES_NEW:
  Always new tx → suspend caller's tx
  Commits/rolls back independently
```

**Interview Point:**

> `REQUIRED` = default, most common. `REQUIRES_NEW` for audit logging that must survive parent rollback.

</details>

---

# 5. Isolation levels?

<details>
<summary>Show Answer</summary>

**Answer:**

**Isolation level** controls how much one transaction can see of another concurrent transaction's uncommitted changes.

| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|-------|-----------|--------------------|--------------|
| READ_UNCOMMITTED | ✅ Possible | ✅ Possible | ✅ Possible |
| READ_COMMITTED | ❌ Prevented | ✅ Possible | ✅ Possible |
| REPEATABLE_READ | ❌ Prevented | ❌ Prevented | ✅ Possible |
| SERIALIZABLE | ❌ Prevented | ❌ Prevented | ❌ Prevented |

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public BigDecimal getBalance(Long accountId) {
    return accountRepo.findById(accountId).map(Account::getBalance).orElseThrow();
}

@Transactional(isolation = Isolation.REPEATABLE_READ)
public void reconcileAccount(Long accountId) {
    BigDecimal balance1 = accountRepo.getBalance(accountId);
    // ... other work ...
    BigDecimal balance2 = accountRepo.getBalance(accountId);
    // balance1 == balance2 guaranteed
}
```

| DB Default | Level |
|------------|-------|
| PostgreSQL | READ_COMMITTED |
| MySQL (InnoDB) | REPEATABLE_READ |
| Oracle | READ_COMMITTED |

**Interview Point:**

> Higher isolation = more consistency, less concurrency. Most apps use READ_COMMITTED (DB default). Use REPEATABLE_READ for financial reconciliation.

</details>

---

## Propagation

---

# 6. REQUIRED

<details>
<summary>Show Answer</summary>

**Answer:**

**REQUIRED** (default) — join the existing transaction if one exists; otherwise create a new one.

```java
@Transactional(propagation = Propagation.REQUIRED)  // default
public void serviceA() {
    repo.save(entity1);       // tx-1
    serviceB();               // joins tx-1
}

@Transactional(propagation = Propagation.REQUIRED)
public void serviceB() {
    repo.save(entity2);       // same tx-1
}
// Both saves commit or rollback together
```

| Scenario | Behavior |
|----------|----------|
| No existing tx | Create new tx |
| Existing tx | Join it |
| Exception in either | Both rollback |

**Interview Point:**

> REQUIRED = 95% of use cases. All operations in the call chain share one transaction.

</details>

---

# 7. REQUIRES_NEW

<details>
<summary>Show Answer</summary>

**Answer:**

**REQUIRES_NEW** — always starts a **new independent transaction**, suspending the caller's transaction.

```java
@Transactional
public void processPayment(Payment payment) {
    paymentRepo.save(payment);
    try {
        auditService.logActivity("Payment processed");  // separate tx
    } catch (Exception e) {
        log.error("Audit failed", e);  // payment tx still commits
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logActivity(String message) {
    auditRepo.save(new AuditLog(message));  // commits independently
}
```

| | REQUIRED | REQUIRES_NEW |
|---|----------|-------------|
| Shares tx | ✅ Yes | ❌ No |
| Survives parent rollback | ❌ No | ✅ Yes |
| Use case | Normal flow | Audit, logging, notifications |

```text
Parent tx: BEGIN → save payment → call audit (REQUIRES_NEW)
                                    → suspend parent
                                    → BEGIN new → save audit → COMMIT
                                    → resume parent → COMMIT
```

**Interview Point:**

> REQUIRES_NEW = independent tx. Audit logs, event publishing — must survive even if business tx rolls back.

</details>

---

# 8. SUPPORTS

<details>
<summary>Show Answer</summary>

**Answer:**

**SUPPORTS** — run in a transaction if one exists; otherwise run **without** a transaction.

```java
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public List<Report> generateReport(ReportFilter filter) {
    return reportRepo.findByFilter(filter);
    // Uses tx if caller has one; runs non-tx if called standalone
}
```

| Scenario | Behavior |
|----------|----------|
| Caller has tx | Join it |
| No tx | Execute non-transactionally |

**Interview Point:**

> SUPPORTS for read-only methods that work both inside and outside a transaction. Rarely used explicitly.

</details>

---

# 9. MANDATORY

<details>
<summary>Show Answer</summary>

**Answer:**

**MANDATORY** — method **must** be called within an existing transaction. Throws `IllegalTransactionStateException` if no tx exists.

```java
@Transactional(propagation = Propagation.MANDATORY)
public void updateInventory(Long productId, int qty) {
    // MUST be called from a transactional method
    inventoryRepo.decrementStock(productId, qty);
}

// Called from:
@Transactional
public void placeOrder(OrderRequest req) {
    orderRepo.save(order);
    inventoryService.updateInventory(req.getProductId(), req.getQty());  // ✅
}

// NOT from:
public void someController() {
    inventoryService.updateInventory(1L, 5);  // ❌ IllegalTransactionStateException
}
```

**Interview Point:**

> MANDATORY = enforce that caller manages the transaction. Safety check — prevents accidental non-transactional calls.

</details>

---

# 10. NEVER

<details>
<summary>Show Answer</summary>

**Answer:**

**NEVER** — method must **not** run inside a transaction. Throws exception if a tx exists.

```java
@Transactional(propagation = Propagation.NEVER)
public String fetchExternalApiData() {
    // Long-running external call — don't hold DB connection
    return restTemplate.getForObject("https://api.example.com/data", String.class);
}
```

| Scenario | Behavior |
|----------|----------|
| No tx | Execute normally |
| Tx exists | Throw `IllegalTransactionStateException` |

**Interview Point:**

> NEVER for operations that must not hold a DB connection (external API calls, file I/O). Ensures no connection pool waste.

</details>

---

## Isolation

---

# 11. READ_UNCOMMITTED

<details>
<summary>Show Answer</summary>

**Answer:**

**READ_UNCOMMITTED** — lowest isolation. Can read **uncommitted data** from other transactions (dirty reads).

```text
Tx1: UPDATE accounts SET balance = 900 WHERE id = 1  (not committed)
Tx2: SELECT balance FROM accounts WHERE id = 1       → reads 900 (dirty!)
Tx1: ROLLBACK                                         → balance is actually 1000
Tx2: acted on wrong data (900)
```

| Anomaly | Possible? |
|---------|-----------|
| Dirty read | ✅ Yes |
| Performance | Fastest (no locks on reads) |

**Interview Point:**

> Almost never used in production. Dirty reads cause data corruption. Most databases treat READ_UNCOMMITTED as READ_COMMITTED.

</details>

---

# 12. READ_COMMITTED

<details>
<summary>Show Answer</summary>

**Answer:**

**READ_COMMITTED** — only reads **committed data**. Prevents dirty reads but allows non-repeatable reads.

```text
Tx1: SELECT balance FROM accounts WHERE id = 1  → 1000
Tx2: UPDATE accounts SET balance = 500 WHERE id = 1; COMMIT;
Tx1: SELECT balance FROM accounts WHERE id = 1  → 500 (different!)
```

| Anomaly | Possible? |
|---------|-----------|
| Dirty read | ❌ No |
| Non-repeatable read | ✅ Yes |
| Default in | PostgreSQL, Oracle, SQL Server |

**Interview Point:**

> Default for most databases. Good balance of consistency and performance. Most Spring apps use this (DB default).

</details>

---

# 13. REPEATABLE_READ

<details>
<summary>Show Answer</summary>

**Answer:**

**REPEATABLE_READ** — same query within a transaction returns the **same rows** every time. Prevents non-repeatable reads.

```text
Tx1: SELECT balance FROM accounts WHERE id = 1  → 1000
Tx2: UPDATE accounts SET balance = 500 WHERE id = 1; COMMIT;
Tx1: SELECT balance FROM accounts WHERE id = 1  → 1000 (still!)
```

| Anomaly | Possible? |
|---------|-----------|
| Dirty read | ❌ No |
| Non-repeatable read | ❌ No |
| Phantom read | ✅ Yes (new rows can appear) |
| Default in | MySQL InnoDB |

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void reconcile(Long accountId) {
    BigDecimal b1 = accountRepo.getBalance(accountId);
    // complex calculation ...
    BigDecimal b2 = accountRepo.getBalance(accountId);
    assert b1.equals(b2);  // guaranteed
}
```

**Interview Point:**

> MySQL default. Use for financial operations where reading same data twice must be consistent.

</details>

---

# 14. SERIALIZABLE

<details>
<summary>Show Answer</summary>

**Answer:**

**SERIALIZABLE** — highest isolation. Transactions execute as if they ran **one after another**. Prevents all anomalies.

```text
Tx1: SELECT COUNT(*) FROM orders WHERE status = 'PENDING'  → 10
Tx2: INSERT INTO orders (status) VALUES ('PENDING'); COMMIT;
Tx1: SELECT COUNT(*) FROM orders WHERE status = 'PENDING'  → 10 (not 11!)
```

| Anomaly | Possible? |
|---------|-----------|
| All anomalies | ❌ Prevented |
| Performance | Slowest (range locks) |
| Deadlock risk | Higher |

**Interview Point:**

> SERIALIZABLE = safest but slowest. Use only when absolute consistency is critical (banking reconciliation). Prefer optimistic locking (`@Version`) instead.

</details>

---

## Advanced

---

# 15. Dirty Read?

<details>
<summary>Show Answer</summary>

**Answer:**

A **dirty read** occurs when a transaction reads data that another transaction has **modified but not yet committed**.

```text
Time  Tx1 (Transfer)              Tx2 (Report)
────  ──────────────────────      ──────────────────────
t1    BEGIN
t2    UPDATE balance = 900
t3                                  SELECT balance → 900  (dirty read!)
t4    ROLLBACK (balance = 1000)
t5                                  Report shows 900 — WRONG!
```

| Prevention | Isolation Level |
|------------|----------------|
| Dirty read | READ_COMMITTED or higher |

**Interview Point:**

> Dirty read = reading uncommitted data. Prevented at READ_COMMITTED and above. Never acceptable in financial systems.

</details>

---

# 16. Non-repeatable Read?

<details>
<summary>Show Answer</summary>

**Answer:**

A **non-repeatable read** occurs when a transaction reads the **same row twice** and gets **different values** because another transaction committed an update in between.

```text
Time  Tx1                           Tx2
────  ──────────────────────      ──────────────────────
t1    SELECT salary WHERE id=1 → 50000
t2                                  UPDATE salary = 60000; COMMIT
t3    SELECT salary WHERE id=1 → 60000  (different!)
```

| Prevention | Isolation Level |
|------------|----------------|
| Non-repeatable read | REPEATABLE_READ or higher |
| Alternative | Optimistic locking with `@Version` |

```java
@Entity
public class Account {
    @Version
    private Long version;  // optimistic lock — alternative to SERIALIZABLE
}
```

**Interview Point:**

> Non-repeatable read = same row, different value on re-read. Fix with REPEATABLE_READ or `@Version` optimistic locking.

</details>

---

# 17. Phantom Read?

<details>
<summary>Show Answer</summary>

**Answer:**

A **phantom read** occurs when a transaction re-runs a query and finds **new rows** that another transaction inserted and committed.

```text
Time  Tx1                              Tx2
────  ──────────────────────           ──────────────────────
t1    SELECT COUNT(*) WHERE status='ACTIVE' → 5
t2                                       INSERT new ACTIVE row; COMMIT
t3    SELECT COUNT(*) WHERE status='ACTIVE' → 6  (phantom row!)
```

| Prevention | Isolation Level |
|------------|----------------|
| Phantom read | SERIALIZABLE |
| Partial fix | REPEATABLE_READ (InnoDB prevents phantoms via MVCC) |

| Anomaly | What Changes | Level to Prevent |
|---------|-------------|-----------------|
| Dirty read | Uncommitted data visible | READ_COMMITTED |
| Non-repeatable read | Same row, different value | REPEATABLE_READ |
| Phantom read | New rows appear | SERIALIZABLE |

**Interview Point:**

> Phantom = new rows appear on re-query. SERIALIZABLE prevents it. InnoDB REPEATABLE_READ also prevents phantoms via gap locks.

</details>

---

## Production Depth — Transactions

---

# P1. @Transactional Pitfalls

<details>
<summary>Show Answer</summary>

**Answer:**

Common production mistakes with `@Transactional`:

| Pitfall | Problem | Fix |
|---------|---------|-----|
| Self-invocation | `this.method()` bypasses proxy | Inject self or extract to another bean |
| Private method | Proxy can't intercept | Make public |
| Checked exception | Default: no rollback | `rollbackFor = Exception.class` |
| Wrong propagation | Audit lost on rollback | `REQUIRES_NEW` for audit |
| Long transaction | Holds DB connection | Keep tx short, external calls outside |

```java
@Service
public class OrderService {

    @Autowired
    private OrderService self;  // inject proxy for self-invocation

    public void createOrder(OrderRequest req) {
        self.doCreate(req);  // goes through proxy → tx starts
    }

    @Transactional
    public void doCreate(OrderRequest req) { ... }
}
```

```java
// Checked exception — won't rollback by default!
@Transactional(rollbackFor = Exception.class)
public void process() throws BusinessException { ... }
```

**Interview Point:**

> #1 pitfall: self-invocation. `@Transactional` on `this.method()` does nothing. Inject self or use `AopContext.currentProxy()`.

</details>

---

# P2. Programmatic vs Declarative Transactions

<details>
<summary>Show Answer</summary>

**Answer:**

| | Declarative | Programmatic |
|---|------------|-------------|
| Style | `@Transactional` annotation | `TransactionTemplate` / `PlatformTransactionManager` |
| Use | 95% of cases | Complex tx boundaries |
| Readability | Clean | Verbose |

```java
// Declarative (preferred)
@Transactional
public void transfer(Long from, Long to, BigDecimal amount) {
    accountService.debit(from, amount);
    accountService.credit(to, amount);
}

// Programmatic — when you need fine control
@Service
public class PaymentService {
  private final TransactionTemplate txTemplate;

  public PaymentService(PlatformTransactionManager txManager) {
    this.txTemplate = new TransactionTemplate(txManager);
  }

  public void processWithRetry(Payment payment) {
    txTemplate.execute(status -> {
      paymentRepo.save(payment);
      if (payment.getAmount().signum() < 0) {
        status.setRollbackOnly();  // mark for rollback
      }
      return null;
    });
  }
}
```

**Interview Point:**

> Prefer declarative (`@Transactional`). Use programmatic only when tx boundaries don't align with method boundaries.

</details>

---

# P3. Optimistic vs Pessimistic Locking

<details>
<summary>Show Answer</summary>

**Answer:**

| | Optimistic | Pessimistic |
|---|-----------|------------|
| Strategy | Check version at commit | Lock row at read |
| Concurrency | High | Low |
| Conflict handling | Retry on failure | Blocks other txs |
| Annotation | `@Version` | `@Lock(PESSIMISTIC_WRITE)` |

```java
// Optimistic — default for most apps
@Entity
public class Account {
    @Id
    private Long id;
    private BigDecimal balance;

    @Version
    private Long version;  // auto-incremented on update
}

// Concurrent update → OptimisticLockException → retry or return 409

// Pessimistic — for critical sections
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") Long id);
// SELECT ... FOR UPDATE — blocks other writers
```

```java
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findByIdForUpdate(fromId).orElseThrow();
    Account to = accountRepo.findByIdForUpdate(toId).orElseThrow();
    from.debit(amount);
    to.credit(amount);
}
```

| Use Optimistic | Use Pessimistic |
|---------------|----------------|
| Low contention (most web apps) | High contention (ticket booking) |
| `@Version` field | `SELECT FOR UPDATE` |
| Retry on conflict | Block until lock released |

**Interview Point:**

> Optimistic = `@Version`, retry on conflict. Pessimistic = `FOR UPDATE`, blocks concurrent access. Optimistic for most apps.

</details>

---

# P4. Distributed Transactions and Saga

<details>
<summary>Show Answer</summary>

**Answer:**

In microservices, a single `@Transactional` only covers **one database**. Cross-service operations need distributed transaction patterns.

| Pattern | How | Trade-off |
|---------|-----|-----------|
| 2PC (Two-Phase Commit) | XA transactions | Slow, tight coupling |
| **Saga** | Sequence of local txs + compensating actions | Eventual consistency |
| Outbox Pattern | Write to DB + event in same tx | Reliable event publishing |

```java
// Saga — Choreography (events)
// OrderService: create order → publish OrderCreated
// PaymentService: listen → charge → publish PaymentCompleted
// InventoryService: listen → reserve → publish StockReserved
// On failure: publish PaymentFailed → OrderService cancels order

// Saga — Orchestration (central coordinator)
@Transactional
public void createOrder(OrderRequest req) {
    Order order = orderRepo.save(new Order(req));
    sagaOrchestrator.start("order-saga", order.getId());
}

// Outbox Pattern
@Transactional
public void createOrder(OrderRequest req) {
    Order order = orderRepo.save(new Order(req));
    outboxRepo.save(new OutboxEvent("OrderCreated", order.getId()));  // same tx
    // Separate poller publishes event to Kafka
}
```

**Interview Point:**

> `@Transactional` = single DB only. Microservices use Saga (choreography/orchestration) or Outbox pattern for cross-service consistency.

</details>

---

# P5. Transaction Timeout and Read-Only

<details>
<summary>Show Answer</summary>

**Answer:**

```java
@Transactional(readOnly = true)  // optimization hint
public List<OrderDto> listOrders() {
    return orderRepo.findAll().stream()
            .map(OrderDto::from)
            .toList();
}

@Transactional(timeout = 30)  // rollback after 30 seconds
public void batchProcess(List<Long> ids) {
    ids.forEach(this::processOne);
}
```

| Attribute | Effect |
|-----------|--------|
| `readOnly = true` | No flush, no dirty checking, DB may route to replica |
| `timeout = N` | Auto-rollback after N seconds — prevents hung transactions |

```properties
# Spring Boot default transaction timeout
spring.transaction.default-timeout=30s
```

**Interview Point:**

> `readOnly=true` on all query methods — Hibernate skips dirty checking and flush. `timeout` prevents connection pool exhaustion from hung txs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: @Transactional on private method?

<details>
<summary>Show Answer</summary>

**Answer:**

**Does not work.** Spring AOP proxy only intercepts public methods. Transaction won't start.

</details>

---

### Q: Self-invocation problem?

<details>
<summary>Show Answer</summary>

**Answer:**

Calling `this.transactionalMethod()` from within the same class bypasses the proxy. Inject self or move method to another bean.

</details>

---

### Q: Default rollback behavior?

<details>
<summary>Show Answer</summary>

**Answer:**

Rolls back on **unchecked exceptions** (RuntimeException). Does **not** rollback on checked exceptions unless `rollbackFor = Exception.class`.

</details>

---

### Q: REQUIRED vs REQUIRES_NEW?

<details>
<summary>Show Answer</summary>

**Answer:**

**REQUIRED** joins existing tx. **REQUIRES_NEW** creates independent tx that commits/rolls back separately — use for audit logs.

</details>

---

### Q: Which isolation level prevents dirty reads?

<details>
<summary>Show Answer</summary>

**Answer:**

**READ_COMMITTED** and above. Dirty reads only possible at READ_UNCOMMITTED.

</details>

---

### Q: Optimistic vs pessimistic locking?

<details>
<summary>Show Answer</summary>

**Answer:**

**Optimistic** (`@Version`) — check at commit, retry on conflict. **Pessimistic** (`FOR UPDATE`) — lock row at read. Optimistic for most apps.

</details>

---

### Q: Does @Transactional work across microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** Single database only. Cross-service consistency needs Saga pattern or Outbox pattern.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Transaction = ACID unit of work. `@Transactional` = declarative tx management via proxy. REQUIRED = join/create. REQUIRES_NEW = independent tx. Isolation: READ_COMMITTED (default, most apps) → REPEATABLE_READ → SERIALIZABLE. Dirty/non-repeatable/phantom reads prevented at increasing levels. Self-invocation bypasses proxy. `readOnly=true` for queries. `@Version` for optimistic locking. Microservices = Saga/Outbox, not distributed `@Transactional`.

</details>
