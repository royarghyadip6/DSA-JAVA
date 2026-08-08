# 70. Production Support Questions

## 70. Production Support Questions

Real-world troubleshooting for senior Java backend engineers — JVM, DB, microservices, incident response.

---

## Real Experience Based

---

# 1. How do you investigate a slow application?

<details>
<summary>Show Answer</summary>

**Answer:**

**Structured triage** — narrow the layer before deep diving.

```text
Step 1: WHEN + WHO + WHAT
  - Started suddenly or gradual? All users or one tenant?
  - Which API/endpoint? Error rate up or only latency?

Step 2: OBSERVE (top-down)
  ┌─ APM (Datadog/New Relic/AppDynamics) → slow traces, p99 spike
  ├─ Load balancer → backend health, connection count
  ├─ App metrics → thread pool queue, DB pool wait, GC pause
  ├─ Infrastructure → CPU, memory, disk I/O, network
  └─ Dependencies → DB, Redis, Kafka, external APIs

Step 3: CORRELATE
  - Deploy at same time? Traffic spike? Batch job running?
  - DB slow query log? Cache hit ratio drop?

Step 4: ISOLATE + FIX
  - Scale horizontally (if CPU-bound + stateless)
  - Kill runaway query / add index
  - Circuit break flaky downstream
  - Rollback bad deploy
```

| Layer   | Quick checks                                         |
|---------|------------------------------------------------------|
| App     | Actuator `/metrics`, thread dump, slow query in logs |
| JVM     | GC logs, heap usage trend                            |
| DB      | `pg_stat_activity`, execution plan, locks            |
| Network | DNS, TLS handshake, timeout config                   |
| Cache   | Redis latency, eviction rate, miss ratio             |

```bash
# Spring Boot actuator
curl localhost:8080/actuator/metrics/http.server.requests
curl localhost:8080/actuator/metrics/jvm.memory.used
curl localhost:8080/actuator/metrics/hikaricp.connections.active
```

**Interview Point:**

> Start with scope + APM traces, then drill JVM/DB/downstream. Senior answer: mention correlation with deploys, batch jobs, and always have baseline metrics.

</details>

---

# 2. How do you analyze high CPU usage?

<details>
<summary>Show Answer</summary>

**Answer:**

**Goal:** Find which **thread** burns CPU and **what code** it runs.

```text
1. Confirm CPU spike
   top / htop / kubectl top pod / cloud monitoring

2. Map process → threads
   top -H -p <pid>          # per-thread CPU
   ps -Lp <pid> -o pid,tid,pcpu,comm

3. Convert TID → hex → thread dump
   printf '%x\n' <tid>      # e.g. 0x1a2b
   jstack <pid> | grep -A 30 1a2b

4. Or: 3 thread dumps 10s apart — threads consistently in RUNNABLE at same stack = hot spot
```

```bash
# Kubernetes Java pod
kubectl top pod payment-api-xyz
kubectl exec -it payment-api-xyz -- top -H
kubectl exec payment-api-xyz -- jstack 1 > threaddump.txt

# Or Spring actuator
curl localhost:8080/actuator/threaddump
```

**Common Java causes:**

| Cause | Stack trace clue |
|-------|------------------|
| Infinite loop / bug | Same business method in RUNNABLE |
| Excessive GC | `GC task thread` high CPU |
| Regex catastrophic backtracking | `java.util.regex` |
| Serialization | `writeObject`, Jackson `writeValue` |
| Crypto | `sun.security` in hot path |
| Log flooding | Appender thread busy |

```text
Fix paths:
  Code bug → hotfix deploy
  GC thrashing → tune heap / fix memory leak
  Traffic spike → scale out
  Expensive endpoint → cache / optimize query
```

**Interview Point:**

> High CPU = find hot thread via top -H + jstack. Take 3 dumps spaced apart. Distinguish app code vs GC threads.

</details>

---

# 3. How do you analyze memory leak?

<details>
<summary>Show Answer</summary>

**Answer:**

**Memory leak** = objects no longer needed but still **referenced** — heap grows until OOM.

```text
Investigation flow:
  1. Monitor heap trend — gradual climb after GC = leak (not one-time spike)
  2. Enable GC logging — Full GC frequent but heap not reclaimed
  3. Capture heap dumps at interval (baseline vs grown)
  4. Compare in Eclipse MAT — dominator tree, leak suspects
  5. Find GC root path holding leaked objects
```

```bash
# Live monitoring
jcmd <pid> GC.heap_info
jstat -gcutil <pid> 5000    # every 5s — Old gen climbing?

# Heap dump
jmap -dump:live,format=b,file=heap1.hprof <pid>
# Compare heap1 vs heap2 taken 30 min later in MAT
```

**Top Java leak causes:**

| Cause                              | Example                          |
|------------------------------------|----------------------------------|
| Static collection growing          | `static Map<>` never evicts      |
| ThreadLocal not removed            | Pool thread retains user context |
| Unclosed resources                 | Connection, stream, HttpClient   |
| Listener/callback not deregistered | Observer pattern leak            |
| Custom cache no TTL                | `ConcurrentHashMap` unbounded    |
| ClassLoader leak                   | hot redeploy without cleanup     |

```java
// Classic leak
private static final Map<String, byte[]> cache = new HashMap<>();
public void handle(Request r) {
    cache.put(r.getSessionId(), r.getBody()); // never removed
}

// Fix: bounded cache with TTL
Cache<String, byte[]> cache = Caffeine.newBuilder()
    .maximumSize(10_000).expireAfterAccess(30, MINUTES).build();
```

**Interview Point:**

> Leak = heap grows, Full GC doesn't help. Two heap dumps + MAT dominator tree. Top causes: static maps, ThreadLocal, unclosed connections.

</details>

---

# 4. How do you collect thread dump?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread dump** = snapshot of all threads — state, stack trace, locks held/waiting.

```bash
# Standard tools
jstack <pid> > threaddump.txt
jcmd <pid> Thread.print > threaddump.txt
kill -3 <pid>              # Linux — JVM prints to stdout/catalina.out

# Spring Boot actuator (preferred in K8s — no JDK tools needed)
curl -s localhost:8080/actuator/threaddump > threaddump.json

# Kubernetes
kubectl exec <pod> -- jstack 1
kubectl exec <pod> -- jcmd 1 Thread.print
```

**Best practice — take 3 dumps:**

```text
jstack pid > dump1.txt
sleep 10
jstack pid > dump2.txt
sleep 10
jstack pid > dump3.txt
→ Compare: stuck threads in same state = deadlock or blocked on resource
```

**What to look for:**

| State           | Meaning                               |
|-----------------|---------------------------------------|
| `BLOCKED`       | Waiting for monitor lock — contention |
| `WAITING`       | Waiting indefinitely (pool exhausted) |
| `TIMED_WAITING` | sleep, park, join timeout             |
| `RUNNABLE`      | Executing or waiting for OS (I/O)     |

```text
"Found one Java-level deadlock" → jstack reports automatically
"BLOCKED on HikariPool" → DB connection pool exhausted
"WAITING on ThreadPoolExecutor" → all worker threads busy
```

**Interview Point:**

> Thread dump for deadlock, stuck threads, pool exhaustion. 3 dumps 10s apart. Actuator `/threaddump` in containerized Spring Boot.

</details>

---

# 5. How do you collect heap dump?

<details>
<summary>Show Answer</summary>

**Answer:**

**Heap dump** = snapshot of all objects on heap — for OOM analysis and leak detection.

```bash
# On-demand (live objects only — smaller file)
jmap -dump:live,format=b,file=heap.hprof <pid>

# jcmd alternative
jcmd <pid> GC.heap_dump /tmp/heap.hprof

# Auto on OOM (always enable in prod JVM flags)
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/heapdump.hprof
-XX:+ExitOnOutOfMemoryError    # let K8s restart pod

# Kubernetes — copy out
kubectl cp <pod>:/tmp/heap.hprof ./heap.hprof
```

| Tool        | Use                                      |
|-------------|------------------------------------------|
| Eclipse MAT | Dominator tree, leak suspects, histogram |
| VisualVM    | Basic analysis                           |
| jhat        | Deprecated — avoid                       |

**Analysis steps in MAT:**

```text
1. Leak Suspects Report — auto-detect suspicious retainers
2. Dominator Tree — what holds the most memory
3. Compare two dumps — what grew between snapshots
4. Find GC roots → path to leaked collection
```

**Caution:**

```text
⚠ Heap dump pauses JVM (can be seconds–minutes on large heap)
⚠ File size ≈ heap used (4GB heap → ~4GB .hprof)
✅ Take during maintenance window or from pod restart with OOM flag
```

**Interview Point:**

> Heap dump via jmap/jcmd or auto on OOM. Analyze with MAT dominator tree. Enable HeapDumpOnOutOfMemoryError in all prod Java apps.

</details>

---

## JVM

---

# 6. GC log analysis?

<details>
<summary>Show Answer</summary>

**Answer:**

**GC logs** reveal pause times, frequency, and whether heap is properly sized.

```bash
# Java 11+ unified logging
-Xlog:gc*,gc+heap=info,gc+age=trace:file=/var/log/gc.log:time,uptime,level,tags:filecount=5,filesize=20m
```

**Key metrics to extract:**

| Metric             | Healthy             | Problem              |
|--------------------|---------------------|----------------------|
| Young GC pause     | < 50ms              | > 200ms consistently |
| Full GC frequency  | Rare (hours/days)   | Every few minutes    |
| Full GC pause      | < 1s                | Multi-second stops   |
| Heap after Full GC | Drops significantly | Stays high → leak    |
| Allocation rate    | Stable              | Sudden spike         |

```text
Sample log reading:
[2.5s][info][gc] GC(15) Pause Young (G1 Evacuation Pause) 120M→40M(512M) 12.3ms
[45s][info][gc] GC(42) Pause Full (Allocation Failure) 480M→470M(512M) 2100ms  ← BAD
```

**Tools:**

```text
GCEasy.io — upload log, get report
GCViewer — visual timeline
Prometheus jvm_gc metrics via Micrometer
```

**Red flags:**

```text
❌ Frequent Full GC + heap not reclaimed → memory leak or heap too small
❌ Long G1 pause > 200ms → tune -XX:MaxGCPauseMillis or increase heap
❌ Metaspace OOM → classloader leak or too many dynamic classes
```

**Interview Point:**

> GC log = pause time + heap reclaimed. Full GC every few min with no reclaim = leak. Use unified GC logging (Java 11+) and GCEasy for analysis.

</details>

---

# 7. Full GC issue troubleshooting?

<details>
<summary>Show Answer</summary>

**Answer:**

**Full GC** = stop-the-world collection of **entire heap** — frequent/long Full GC kills latency.

```text
Symptoms:
  - p99 latency spikes every N minutes
  - Throughput drops periodically
  - GC log: "Pause Full" frequent
  - Application freezes briefly
```

**Diagnosis tree:**

```text
Full GC frequent?
├── Heap too small → increase -Xmx (after confirming not a leak)
├── Memory leak → heap dump + MAT (Old gen only grows)
├── Metaspace full → -XX:MaxMetaspaceSize, fix classloader leak
├── System.gc() called → find RMI/code calling System.gc()
├── Promotion failure → young gen too small, tune G1 regions
└── Huge objects → direct buffers, large arrays bypassing young gen
```

```bash
# JVM flags for G1 (default Java 9+)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-Xms2g -Xmx2g          # same Xms and Xmx avoids resize pauses
-XX:InitiatingHeapOccupancyPercent=45
```

| Action             | When                             |
|--------------------|----------------------------------|
| Increase heap      | Legitimate high load, no leak    |
| Fix leak           | Old gen grows unbounded          |
| Tune G1            | Pauses too long on adequate heap |
| Remove System.gc() | Explicit calls in libraries      |
| Scale out          | Reduce per-instance load         |

**Interview Point:**

> Full GC = entire heap STW pause. Distinguish leak (heap not freed) vs undersized heap vs mis-tuning. Never just increase heap without analysis.

</details>

---

# 8. OutOfMemoryError troubleshooting?

<details>
<summary>Show Answer</summary>

**Answer:**

**OOM** = JVM cannot allocate object — process may crash or hang.

| OOM type                         | Cause                      | Fix                                        |
|----------------------------------|----------------------------|--------------------------------------------|
| `Java heap space`                | Heap full / leak           | Heap dump, fix leak, increase heap         |
| `GC overhead limit exceeded`     | >98% time in GC, <2% freed | Leak or heap too small                     |
| `Metaspace`                      | Too many classes loaded    | Increase metaspace, fix classloader leak   |
| `Unable to create native thread` | Too many threads           | Reduce threads, increase ulimit, scale out |
| `Direct buffer memory`           | NIO off-heap exhausted     | Limit direct memory, fix buffer leak       |
| `Compressed class space`         | Class metadata limit       | Tune or fix dynamic class generation       |

```text
Troubleshooting steps:
  1. Read exact OOM message + stack trace in logs
  2. Check heap dump (HeapDumpOnOutOfMemoryError)
  3. MAT: dominator tree for heap OOM
  4. jstack for thread OOM (count threads)
  5. Correlate with deploy / traffic change
```

```bash
# Prod JVM flags (minimum)
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/heapdump.hprof
-XX:+ExitOnOutOfMemoryError
-XX:MaxRAMPercentage=75.0          # containers
-XX:NativeMemoryTracking=summary    # for native OOM investigation
```

```java
// Thread OOM — unbounded thread creation
while(true) { new Thread(() -> { Thread.sleep(Long.MAX_VALUE); }).start(); }
// Fix: use ExecutorService with bounded pool
```

**Interview Point:**

> OOM type tells you which memory region failed. Heap OOM → MAT. Thread OOM → count threads + bounded pools. Always enable heap dump on OOM in prod.

</details>

---

## Database

---

# 9. Slow SQL troubleshooting?

<details>
<summary>Show Answer</summary>

**Answer:**

**Systematic SQL performance triage** for production incidents.

```text
Step 1: IDENTIFY slow query
  - APM trace (which query in which API)
  - DB slow query log (log_min_duration_statement)
  - pg_stat_statements / Oracle AWR / MySQL slow log

Step 2: ANALYZE execution plan
  EXPLAIN (ANALYZE, BUFFERS) SELECT ...

Step 3: ROOT CAUSE
  - Missing index → seq scan on large table
  - Bad index → wrong column order
  - Stale statistics → optimizer wrong choice
  - Lock wait → another txn holds row lock
  - N+1 queries → ORM fetching in loop
  - Full table scan on growing table

Step 4: FIX
  - Add/fix index
  - Rewrite query (avoid SELECT *, subquery → join)
  - Batch fetch (JOIN FETCH, @EntityGraph)
  - Pagination (LIMIT/OFFSET or keyset)
  - Archive old data
```

| Red flag in plan               | Action                   |
|--------------------------------|--------------------------|
| `Seq Scan` on millions of rows | Add index                |
| `Nested Loop` with huge outer  | Check join order / stats |
| `Sort` + high cost             | Index covers ORDER BY    |
| `Rows Removed by Filter` high  | Index or rewrite WHERE   |

```java
// N+1 fix in JPA
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.userId = :id")
List<Order> findWithItems(@Param("id") Long userId);
```

```sql
-- PostgreSQL: find blocking queries
SELECT pid, wait_event_type, query
FROM pg_stat_activity WHERE wait_event_type = 'Lock';
```

**Interview Point:**

> Slow SQL = find query → EXPLAIN plan → missing index / N+1 / lock. Hibernate: enable `show_sql` only in dev; use APM + pg_stat_statements in prod.

</details>

---

# 10. Deadlock troubleshooting?

<details>
<summary>Show Answer</summary>

**Answer:**

**Deadlock** = two or more transactions **wait for each other's locks** — DB or JVM level.

**Database deadlock:**

```text
Txn A: UPDATE orders WHERE id=1  → locks row 1
Txn B: UPDATE orders WHERE id=2  → locks row 2
Txn A: UPDATE orders WHERE id=2  → WAIT (B holds lock)
Txn B: UPDATE orders WHERE id=1  → WAIT (A holds lock) → DEADLOCK
DB kills one txn (victim) → SQLException to app
```

```sql
-- PostgreSQL deadlock detail in log
-- log_lock_waits = on, deadlock_timeout = 1s

-- Oracle
SELECT * FROM v$lock WHERE block > 0;

-- MySQL
SHOW ENGINE INNODB STATUS;  -- LATEST DETECTED DEADLOCK section
```

**JVM deadlock (jstack):**

```text
"pool-1-thread-1":
  waiting to lock monitor 0x... (Object A)
  which is held by "pool-1-thread-2"
"pool-1-thread-2":
  waiting to lock monitor 0x... (Object B)
  which is held by "pool-1-thread-1"

Found one Java-level deadlock
```

**Prevention:**

| DB                                     | App                                |
|----------------------------------------|------------------------------------|
| Lock rows in consistent order (id ASC) | Avoid nested locks across services |
| Keep transactions short                | `@Transactional` scope minimal     |
| Lower isolation if safe                | Timeout on lock wait               |
| Retry on deadlock (SQLState 40001)     | Fixed lock ordering in code        |

```java
@Retryable(retryFor = DeadlockLoserDataAccessException.class, maxAttempts = 3)
@Transactional
public void transfer(Long from, Long to, BigDecimal amount) {
    // Always lock account with lower ID first
    Long first = Math.min(from, to), second = Math.max(from, to);
    accountRepo.findByIdForUpdate(first);
    accountRepo.findByIdForUpdate(second);
    // ... debit/credit
}
```

**Interview Point:**

> DB deadlock = circular lock wait; DB kills victim — retry with backoff. JVM deadlock = jstack. Prevention: consistent lock ordering + short transactions.

</details>

---

## Microservices

---

# 11. Service-to-service timeout troubleshooting?

<details>
<summary>Show Answer</summary>

**Answer:**

**Timeout cascade** = one slow downstream causes caller threads to block → thread pool exhaustion → entire service down.

```text
Symptom chain:
  Payment slow (30s) → Order service threads blocked waiting
  → Order thread pool full → Order API returns 503/timeout
  → User sees "Order service down" (root cause: Payment)
```

**Investigation:**

```text
1. Distributed trace (Jaeger/Zipkin) — find slowest span
2. Check downstream health + latency metrics
3. Thread dump — threads WAITING on HTTP client read
4. Connection pool stats — all connections in use
```

| Config          | Typical value           |
|-----------------|-------------------------|
| Connect timeout | 2–5s                    |
| Read timeout    | 5–30s (per SLA)         |
| Total timeout   | < client-facing timeout |
| Retry           | Only idempotent ops     |

```java
// RestTemplate / WebClient timeout
@Bean
public RestTemplate restTemplate() {
    HttpComponentsClientHttpRequestFactory factory =
        new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(10000);
    return new RestTemplate(factory);
}

// Resilience4j
@CircuitBreaker(name = "payment")
@TimeLimiter(name = "payment")
public CompletableFuture<PaymentResult> pay(Order order) { ... }
```

**Fix strategy:**

```text
✅ Set aggressive timeouts < upstream timeout
✅ Circuit breaker — fail fast when downstream unhealthy
✅ Bulkhead — separate thread pool per dependency
✅ Async where possible (CompletableFuture, messaging)
✅ Fix root cause downstream (slow query, GC pause)
```

**Interview Point:**

> Timeout issues = cascade failure. Distributed tracing finds real culprit. Fail fast with circuit breaker; never infinite wait. Thread pool exhaustion is the killer.

</details>

---

# 12. Circuit breaker use cases?

<details>
<summary>Show Answer</summary>

**Answer:**

**Circuit breaker** stops calls to a **failing dependency** — fail fast instead of waiting/retrying into exhaustion.

```text
States:
  CLOSED   → normal calls pass through
  OPEN     → fail immediately (downstream sick)
  HALF_OPEN → test call; success → CLOSED, fail → OPEN
```

```text
Payment API failing:
  Without CB: 100 threads block 30s each → Order service dead
  With CB:    After 5 failures → OPEN → instant fallback → Order stays alive
```

**Use cases:**

| Scenario                      | Circuit breaker action            |
|-------------------------------|-----------------------------------|
| External payment gateway down | Open → return "try later"         |
| Recommendation service slow   | Open → show default items         |
| Legacy mainframe timeout      | Open → queue for retry            |
| DB replica lagging            | Open read path → route to primary |

```java
@CircuitBreaker(name = "inventory", fallbackMethod = "inventoryFallback")
public StockInfo checkStock(String sku) {
    return inventoryClient.getStock(sku);
}

public StockInfo inventoryFallback(String sku, CallNotPermittedException e) {
    return StockInfo.unknown(sku);  // degrade gracefully
}
```

```yaml
# Resilience4j config
resilience4j.circuitbreaker:
  instances:
    payment:
      slidingWindowSize: 10
      failureRateThreshold: 50
      waitDurationInOpenState: 30s
      permittedNumberOfCallsInHalfOpenState: 3
```

**Interview Point:**

> Circuit breaker = fail fast + protect caller. Use for every external dependency. Pair with fallback/degraded response. Resilience4j in Spring Boot.

</details>

---

# 13. Retry strategy?

<details>
<summary>Show Answer</summary>

**Answer:**

**Retry** re-attempts failed operations — must be designed carefully to avoid amplifying outages.

```text
Retry rules:
  ✅ Idempotent operations only (GET, PUT with same payload, dedup key)
  ❌ Non-idempotent without dedup (POST payment — double charge risk)
  ✅ Transient failures (timeout, 503, connection reset)
  ❌ Permanent failures (400 bad request, 404)
```

| Strategy     | Config                                       |
|--------------|----------------------------------------------|
| Max attempts | 3 (including first)                          |
| Backoff      | Exponential: 1s, 2s, 4s + jitter             |
| Jitter       | Random ±30% — prevent thundering herd        |
| Timeout      | Per-attempt timeout, not cumulative infinite |

```java
@Retry(name = "payment")
@CircuitBreaker(name = "payment")
public PaymentResult charge(PaymentRequest req) {
    return paymentGateway.charge(req);
}

// Idempotency key for safe retry
headers.set("Idempotency-Key", orderId);
```

```yaml
resilience4j.retry:
  instances:
    payment:
      maxAttempts: 3
      waitDuration: 1s
      exponentialBackoffMultiplier: 2
      retryExceptions:
        - java.net.SocketTimeoutException
        - org.springframework.web.client.HttpServerErrorException
      ignoreExceptions:
        - org.springframework.web.client.HttpClientErrorException
```

**Anti-patterns:**

```text
❌ Retry on 500 without backoff → DDoS your own downstream
❌ Retry non-idempotent POST → duplicate orders
❌ Infinite retry → thread pool exhaustion
❌ Retry without circuit breaker → retry into failing service
```

**Stack order:**

```text
Request → Retry (transient) → Circuit Breaker (sustained failure) → Timeout → Call
```

**Interview Point:**

> Retry = exponential backoff + jitter + max attempts + idempotency key. Only transient errors. Always combine with circuit breaker. Never retry payment without dedup.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: First 3 steps for prod slowness?

<details>
<summary>Show Answer</summary>

**Answer:**

1. Scope — which API, when started, all users? 2. APM/metrics — find slow layer (app/DB/downstream). 3. Correlate with deploy, traffic, batch job.

</details>

---

### Q: Thread dump vs heap dump?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread dump** = thread states, stacks, deadlocks (CPU/blocking issues). **Heap dump** = all objects on heap (memory leak, OOM).

</details>

---

### Q: Full GC every 2 minutes — what next?

<details>
<summary>Show Answer</summary>

**Answer:**

Check if heap is reclaimed after Full GC. Not reclaimed → leak (heap dump/MAT). Reclaimed → heap too small or traffic spike. Don't blindly increase -Xmx.

</details>

---

### Q: Circuit breaker vs retry?

<details>
<summary>Show Answer</summary>

**Answer:**

**Retry** = re-attempt transient failure with backoff. **Circuit breaker** = stop calling failing service entirely (fail fast). Use both: retry for blips, CB for sustained outage.

</details>

---

### Q: How to find which SQL is slow in prod?

<details>
<summary>Show Answer</summary>

**Answer:**

APM trace → `pg_stat_statements` / slow query log → `EXPLAIN ANALYZE` → fix index/query/N+1.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Prod support = scope → observe (APM/metrics) → correlate (deploy/traffic) → isolate layer. Thread dump for CPU/deadlock; heap dump + MAT for leak; GC logs for pause; EXPLAIN for SQL; circuit breaker + retry with idempotency for microservices resilience.

</details>
