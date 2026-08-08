# 69. System Design (Backend Developer)

## 69. System Design (Backend Developer)

Scalability, data, caching, messaging — senior backend design patterns for 5–8 YOE interviews.

---

## Frequently Asked

---

# 1. Design URL Shortener.

<details>
<summary>Show Answer</summary>

**Answer:**

**Requirements:** Shorten long URL → unique code; redirect; high read, moderate write; analytics optional.

```text
POST /api/shorten  { "url": "https://very-long..." }
→ { "shortUrl": "https://go.ly/abc12X" }

GET /abc12X → 301 redirect to original URL
```

**High-level architecture:**

```text
Client → API Gateway → Shorten Service → DB
                    → Redirect Service → Cache (Redis) → DB
```

| Component | Choice |
|-----------|--------|
| ID generation | Base62 encode auto-increment ID, or hash (collision handling) |
| Storage | SQL (URL mappings) or NoSQL for scale |
| Cache | Redis — 80/20 read hot URLs |
| Scale | Stateless API + read replicas + CDN for redirects |

**Key design decisions:**

```text
7-char Base62 → 62^7 ≈ 3.5 trillion URLs
301 (permanent) vs 302 (track clicks) — trade SEO vs analytics
Custom alias — check uniqueness, reserved word blocklist
Rate limit shorten API — prevent abuse
```

```java
// Base62 encode DB sequence
public String encode(long id) {
    StringBuilder sb = new StringBuilder();
    while (id > 0) {
        sb.append(BASE62.charAt((int)(id % 62)));
        id /= 62;
    }
    return sb.reverse().toString();
}
```

**Capacity estimate (interview math):**

```text
100M URLs/month × 500 bytes = 50GB/month storage
Read:write ≈ 100:1 → cache + read replicas critical
```

**Interview Point:**

> URL shortener = ID generation + key-value store + cache-heavy reads. Discuss collision strategy, redirect code, and read scaling.

</details>

---

# 2. Design Notification Service.

<details>
<summary>Show Answer</summary>

**Answer:**

**Requirements:** Send email, SMS, push to millions of users; templates; retry; delivery status; priority channels.

```text
Order Service → "order confirmed" event → Notification Service → Email/SMS/Push providers
```

**Architecture:**

```text
Producer → Kafka topic (notifications) → Consumer workers
                                              ├── Email adapter (SendGrid)
                                              ├── SMS adapter (Twilio)
                                              └── Push adapter (FCM)
         → Status DB + retry queue (DLQ)
```

| Concern | Solution |
|---------|----------|
| Spike traffic | Kafka buffers; async processing |
| Provider failure | Retry with exponential backoff; circuit breaker |
| Idempotency | `notificationId` dedup key in DB |
| Templates | Template engine (Thymeleaf) + variable substitution |
| Preferences | User opt-in/opt-out per channel |
| Rate limits | Per-provider throttling queue |

```java
@KafkaListener(topics = "notifications")
public void process(NotificationEvent event) {
    if (dedupRepo.exists(event.getId())) return;
    ChannelAdapter adapter = adapterFactory.get(event.getChannel());
    adapter.send(event);
    dedupRepo.save(event.getId());
}
```

**Interview Point:**

> Notification = async event-driven + multi-channel adapters + idempotency + retry/DLQ. Never block order flow on email send.

</details>

---

# 3. Design Parking Lot.

<details>
<summary>Show Answer</summary>

**Answer:**

**Classic OOP + LLD** — often asked as low-level design with extensibility.

**Entities:**

```text
ParkingLot → Floors → ParkingSpots (Compact, Large, Handicapped)
Vehicle (Car, Truck, Motorcycle)
Ticket, Payment
```

**Key operations:**

```text
park(vehicle)   → find available spot for vehicle type → issue ticket
unpark(ticket)  → calculate fee → free spot
```

```java
public class ParkingLot {
    private final List<Floor> floors;
    private final Map<VehicleType, Integer> availableSpots;

    public Ticket park(Vehicle vehicle) {
        ParkingSpot spot = findSpot(vehicle.getType())
            .orElseThrow(() -> new NoSpotAvailableException());
        spot.assign(vehicle);
        return new Ticket(spot, Instant.now());
    }
}
```

| Design pattern | Use |
|----------------|-----|
| Strategy | Pricing (hourly, flat, weekend) |
| Factory | Spot type by vehicle |
| Singleton | ParkingLot instance (one lot) |
| Observer | Display board updates on spot change |

**Concurrency:** `synchronized` or `ReentrantLock` per floor/spot for thread-safe booking.

**Interview Point:**

> Parking lot = OOP modeling + spot allocation algorithm + pricing strategy. Senior: mention concurrency for simultaneous park requests.

</details>

---

# 4. Design Rate Limiter.

<details>
<summary>Show Answer</summary>

**Answer:**

**Goal:** Limit requests per user/IP/API key — e.g., 100 req/min — protect backend from abuse and overload.

**Algorithms:**

| Algorithm | How | Pros/Cons |
|-----------|-----|-----------|
| **Token Bucket** | Tokens refill at fixed rate; request consumes 1 | Allows bursts |
| **Leaky Bucket** | Fixed outflow rate | Smooth output |
| **Fixed Window** | Count per minute window | Simple; boundary spike |
| **Sliding Window** | Rolling time window | More accurate |

```text
API Gateway / Filter → Rate Limiter (Redis) → Allow or 429 Too Many Requests
```

**Redis sliding window (production):**

```lua
-- INCR key, EXPIRE 60s, compare to limit
local count = redis.call('INCR', KEYS[1])
if count == 1 then redis.call('EXPIRE', KEYS[1], 60) end
return count
```

```java
@Component
public class RateLimitFilter implements Filter {
  public void doFilter(req, res, chain) {
    String key = "rl:" + clientIp;
    if (!limiter.tryAcquire(key, 100, Duration.ofMinutes(1))) {
      res.setStatus(429);
      return;
    }
    chain.doFilter(req, res);
  }
}
```

**Distributed:** Redis central counter — all API instances share state.

**Interview Point:**

> Rate limiter = algorithm choice + distributed store (Redis) + 429 response + headers (`X-RateLimit-Remaining`). Token bucket for burst tolerance.

</details>

---

# 5. Design Food Delivery System.

<details>
<summary>Show Answer</summary>

**Answer:**

**Actors:** Customer, Restaurant, Delivery Partner, Admin.

**Core flows:**

```text
1. Browse restaurants (geo search) → menu
2. Place order → payment → restaurant accepts
3. Assign delivery partner (nearest available)
4. Track order status → deliver → rate
```

**Services (microservices):**

| Service | Responsibility |
|---------|----------------|
| User Service | Auth, profiles |
| Restaurant Service | Menu, availability |
| Order Service | Order state machine |
| Payment Service | Payment gateway integration |
| Dispatch Service | Partner assignment (geo + load) |
| Notification Service | Status updates |
| Location Service | Real-time GPS tracking |

```text
Order states: PLACED → CONFIRMED → PREPARING → PICKED_UP → DELIVERED
              (Saga pattern for payment + inventory + dispatch)
```

**Data stores:**

```text
PostgreSQL — orders, payments (ACID)
Redis — session, restaurant cache, geo index
Elasticsearch — restaurant search
Kafka — order events, location stream
```

**Dispatch algorithm:** Nearest partner within radius + not overloaded + restaurant proximity.

**Interview Point:**

> Food delivery = geo search + order state machine + saga for distributed txn + real-time tracking. Discuss peak lunch scaling and partner assignment.

</details>

---

# 6. Design E-Commerce Cart.

<details>
<summary>Show Answer</summary>

**Answer:**

**Requirements:** Add/remove items, quantity, price snapshot, merge guest→logged-in cart, checkout, inventory check.

```text
POST /cart/items  { productId, qty }
GET  /cart        → items + prices + total
POST /cart/checkout → create order
```

**Architecture:**

```text
Cart Service (Redis) ←→ Catalog Service (product details)
                     ←→ Inventory Service (stock check)
                     ←→ Pricing Service (discounts, coupons)
```

| Decision | Approach |
|----------|----------|
| Storage | Redis hash per userId/sessionId — fast, TTL 30 days |
| Price | Snapshot price at add-to-cart (price may change) |
| Guest cart | sessionId cookie → merge on login |
| Concurrency | Optimistic lock on checkout; reserve inventory |
| Consistency | Eventual for cart; strong for checkout/payment |

```java
// Redis cart structure
// HSET cart:user123 item:SKU001 {"qty":2,"price":29.99,"addedAt":"..."}
public void addItem(String userId, CartItem item) {
    redis.hSet("cart:" + userId, "item:" + item.sku(), serialize(item));
    redis.expire("cart:" + userId, 30, DAYS);
}
```

**Checkout saga:**

```text
1. Validate cart + inventory reserve
2. Create order (pending)
3. Charge payment
4. Confirm order / release inventory on failure
```

**Interview Point:**

> Cart = Redis for speed + price snapshot + guest merge. Checkout needs inventory reservation and distributed transaction (saga).

</details>

---

## Scalability

---

# 7. Horizontal Scaling?

<details>
<summary>Show Answer</summary>

**Answer:**

**Horizontal scaling** = add **more machines/instances** to handle increased load (scale out).

```text
1 server (4 CPU) → 10 servers (40 CPU total)
Load Balancer distributes requests across instances
```

| Aspect | Detail |
|--------|--------|
| Requirement | **Stateless** application tier |
| Session | Sticky session OR Redis session store |
| DB | Becomes bottleneck — read replicas, sharding |
| Cost | Linear with instances; commodity hardware |

```text
Spring Boot: deploy N identical pods behind K8s Service + HPA
Auto-scale on CPU/RPS/custom metrics
```

**Interview Point:**

> Horizontal = more nodes. Requires stateless services + externalized session/cache/DB. Preferred over vertical at cloud scale.

</details>

---

# 8. Vertical Scaling?

<details>
<summary>Show Answer</summary>

**Answer:**

**Vertical scaling** = increase **resources on single machine** (more CPU, RAM, disk) — scale up.

```text
4 vCPU, 8GB RAM → 32 vCPU, 128GB RAM (bigger instance type)
```

| Pros | Cons |
|------|------|
| Simple — no code change | Hard ceiling (max instance size) |
| No distributed complexity | Single point of failure |
| Good for DB initially | Expensive at top tier |
| | Downtime during resize (non-live) |

**When to use:**

```text
✅ Database primary (until sharding needed)
✅ Monolith quick fix
✅ JVM heap tuning on bigger box
❌ Long-term app tier strategy — prefer horizontal
```

**Interview Point:**

> Vertical = bigger box. Quick win but limited ceiling. DB often scaled vertically first; app tier horizontally.

</details>

---

# 9. Load Balancer?

<details>
<summary>Show Answer</summary>

**Answer:**

**Load balancer** distributes incoming traffic across **multiple backend servers** — improves availability and throughput.

```text
Clients → LB (VIP) → [Server1, Server2, Server3]
```

| Type | Layer | Examples |
|------|-------|----------|
| L4 | TCP/UDP | AWS NLB, HAProxy TCP |
| L7 | HTTP | nginx, ALB, Ingress |

**Algorithms:**

| Algorithm | Behavior |
|-----------|----------|
| Round Robin | Rotate evenly |
| Least Connections | Send to least busy |
| Weighted | More traffic to powerful nodes |
| IP Hash | Same client → same server (sticky) |

```text
Health checks: GET /actuator/health → remove unhealthy instance
SSL termination at LB — backends use HTTP internally
```

**Java microservices:** API Gateway (Kong, Spring Cloud Gateway) + K8s Service for internal LB.

**Interview Point:**

> LB = distribute + health check + optional SSL termination. L7 for path routing; L4 for raw TCP. Always health-check Spring actuator.

</details>

---

## Database

---

# 10. SQL vs NoSQL?

<details>
<summary>Show Answer</summary>

**Answer:**

| | SQL (RDBMS) | NoSQL |
|---|-------------|-------|
| Schema | Fixed, normalized | Flexible / schema-less |
| Transactions | ACID (strong) | Eventual (often BASE) |
| Scale | Vertical + read replicas + sharding | Horizontal partition native |
| Joins | Rich JOIN support | Denormalize / app-side join |
| Examples | PostgreSQL, MySQL, Oracle | MongoDB, Cassandra, DynamoDB |
| Best for | Orders, payments, ledger | Logs, catalog, sessions, feeds |

**Decision framework:**

```text
Need ACID + complex queries + relations → SQL
Need massive write scale + flexible schema → NoSQL
Polyglot persistence: PostgreSQL (orders) + Redis (cache) + Elasticsearch (search)
```

**CAP theorem (senior):**

```text
CP: Consistency + Partition tolerance (PostgreSQL, Zookeeper)
AP: Availability + Partition tolerance (Cassandra, DynamoDB)
```

**Interview Point:**

> Not either/or — polyglot persistence. SQL for money/orders; NoSQL for scale/flexibility. Mention ACID vs BASE trade-off.

</details>

---

# 11. Sharding?

<details>
<summary>Show Answer</summary>

**Answer:**

**Sharding** = **horizontal partitioning** of data across multiple database instances — each shard holds a subset of rows.

```text
user_id % 4 = 0 → Shard 0
user_id % 4 = 1 → Shard 1
...
```

| Strategy | When |
|----------|------|
| Hash-based | Even distribution |
| Range-based | Time-series (orders by month) |
| Directory-based | Lookup table maps key → shard |
| Geo-based | Users by region |

**Challenges:**

```text
❌ Cross-shard JOIN — avoid or fan-out queries
❌ Rebalancing when adding shards
❌ Global unique ID — snowflake ID, not auto-increment
❌ Transactions across shards — saga, 2PC (avoid)
```

```java
// Application-level routing
int shard = (int) (userId % NUM_SHARDS);
DataSource ds = shardRouter.get(shard);
```

**Interview Point:**

> Sharding = split data when single DB can't scale writes. Choose shard key carefully (high cardinality, even distribution). Avoid cross-shard transactions.

</details>

---

# 12. Replication?

<details>
<summary>Show Answer</summary>

**Answer:**

**Replication** = copy data from **primary** to **replica(s)** — improves read scale and availability.

```text
         Writes
Primary (Leader) ──async/sync──► Replica 1 (read)
              └──────────────────► Replica 2 (read)
```

| Mode | Trade-off |
|------|-----------|
| Sync | Strong consistency; write latency |
| Async | Fast writes; replication lag |
| Semi-sync | At least one replica ack |

**Use cases:**

```text
Read replicas — offload SELECT queries (reporting, search)
Failover — promote replica if primary dies
Geo-replication — read local, write central
```

**Java app pattern:**

```text
@Transactional write → primary datasource
@ReadOnly → replica datasource (routing via @Transactional(readOnly=true))
```

**Replication lag issue:**

```text
Write order → read immediately from replica → stale data
Fix: read-your-writes (route recent writes to primary)
```

**Interview Point:**

> Replication = read scale + HA. Watch replication lag. Spring: separate read/write datasources with routing.

</details>

---

## Caching

---

# 13. Redis?

<details>
<summary>Show Answer</summary>

**Answer:**

**Redis** = in-memory **key-value data store** — used as cache, session store, rate limiter, pub/sub, distributed lock.

| Data structure | Use |
|----------------|-----|
| String | Cache JSON, counters |
| Hash | Object fields (cart items) |
| List | Queue, recent items |
| Set | Unique tags, followers |
| Sorted Set | Leaderboard, geo radius |
| Pub/Sub | Simple messaging |

```java
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) {
    return productRepo.findById(id);
}

// Distributed lock
Boolean acquired = redisTemplate.opsForValue()
    .setIfAbsent("lock:order:" + orderId, "1", Duration.ofSeconds(30));
```

**Production considerations:**

```text
TTL on all cache keys — prevent unbounded growth
Eviction: allkeys-lru when memory full
Cluster mode for HA + horizontal scale
Persistence: RDB snapshots + AOF for durability (if needed)
Never cache as source of truth for financial data without TTL + invalidation
```

**Interview Point:**

> Redis = fast in-memory cache + more (locks, rate limit, session). Always set TTL. Cache aside is default pattern.

</details>

---

# 14. Cache Aside Pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

**Cache Aside (Lazy Loading)** — application manages cache; read on miss loads from DB and populates cache.

```text
READ:
  1. Check cache → hit? return
  2. Miss → read DB → write cache → return

WRITE:
  1. Update DB
  2. Invalidate (delete) cache entry
```

```java
public Product getProduct(Long id) {
    String key = "product:" + id;
    Product cached = redis.get(key);
    if (cached != null) return cached;
    Product db = repo.findById(id);
    if (db != null) redis.set(key, db, TTL_1_HOUR);
    return db;
}

public void updateProduct(Product p) {
    repo.save(p);
    redis.delete("product:" + p.getId());  // invalidate
}
```

| Pros | Cons |
|------|------|
| Simple, flexible | Cache miss = 2 round trips |
| Only cache what's read | Stale data if invalidation missed |
| Cache failure ≠ DB down | Thundering herd on popular key expiry |

**Thundering herd fix:** mutex lock on cache miss, or probabilistic early expiry.

**Interview Point:**

> Cache aside = app owns cache logic. Read-through on miss; invalidate on write. Most common pattern in Java (Spring @Cacheable).

</details>

---

# 15. Write Through?

<details>
<summary>Show Answer</summary>

**Answer:**

**Write Through** — write goes to **cache and DB synchronously** — cache always consistent with DB.

```text
WRITE:
  1. Application writes to cache
  2. Cache synchronously writes to DB
  3. Ack after both succeed

READ:
  1. Read from cache (always warm for written data)
```

| Pros | Cons |
|------|------|
| Cache always fresh | Write latency = cache + DB |
| No stale reads | Wasted cache for rarely read data |
| Simple read path | Cache layer must support write-through |

```text
Use when: read-after-write consistency critical
Example: user profile update must reflect immediately
```

**vs Cache Aside:**

```text
Cache Aside:  write DB → invalidate cache (lazy reload)
Write Through: write cache → cache writes DB (eager sync)
```

**Interview Point:**

> Write through = synchronous dual write. Strong consistency on reads but higher write latency. Less common than cache aside in Java apps.

</details>

---

# 16. Write Back?

<details>
<summary>Show Answer</summary>

**Answer:**

**Write Back (Write Behind)** — write to **cache first**, async flush to DB later.

```text
WRITE:
  1. Write to cache → return immediately
  2. Background batch/sync writes to DB

READ:
  1. Read from cache
```

| Pros | Cons |
|------|------|
| Fastest writes | Data loss risk if cache crashes before flush |
| Batch DB writes | Complexity — ordering, failure handling |
| Reduces DB load | Temporary inconsistency |

```text
Use when: write-heavy, eventual consistency OK
Example: click counters, analytics, page views
NOT for: payments, inventory (use sync write)
```

```java
// Risky for critical data — only for metrics
redis.incr("pageviews:product:" + id);
// async worker flushes to DB every 5 seconds
```

**Interview Point:**

> Write back = fast writes, async DB persist. High performance but data loss risk. Never for financial/critical data — only metrics and counters.

</details>

---

## Messaging

---

# 17. Kafka vs RabbitMQ?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Apache Kafka | RabbitMQ |
|---|--------------|----------|
| Model | Distributed **log** (pub-sub) | **Message broker** (queue) |
| Throughput | Millions msg/sec | Thousands–hundreds of thousands |
| Retention | Configurable (days/forever) | Delete after consume (default) |
| Ordering | Per partition | Per queue |
| Replay | Yes — consumers re-read offset | No (ack = gone) |
| Routing | Topic + partition | Exchanges (direct, topic, fanout) |
| Use case | Event streaming, audit log | Task queues, RPC-style |

```text
Kafka: order-events topic → Order Service, Analytics, Notification (all read same stream)
RabbitMQ: send-email queue → one worker consumes each message
```

**Java integration:**

```java
// Kafka producer
kafkaTemplate.send("order-events", orderId, event);

// RabbitMQ
rabbitTemplate.convertAndSend("email.exchange", "email.send", payload);
```

**When to choose:**

```text
Kafka → event sourcing, high volume, replay, microservices choreography
RabbitMQ → job queues, complex routing, lower volume, per-message ack
```

**Interview Point:**

> Kafka = durable log, replay, high throughput event bus. RabbitMQ = traditional broker, task queues. Kafka dominates microservices event streaming.

</details>

---

# 18. Async communication benefits?

<details>
<summary>Show Answer</summary>

**Answer:**

**Async messaging** decouples services — producer sends event/message; consumer processes later.

```text
Sync:  Order → Payment (HTTP wait 2s) → Notification (wait 1s) = 3s user wait
Async: Order → publish event → return 200ms; Payment + Notification consume independently
```

| Benefit | Example |
|---------|---------|
| **Decoupling** | Order service doesn't know email provider |
| **Resilience** | Consumer down → messages buffered in Kafka |
| **Scalability** | Scale consumers independently |
| **Peak handling** | Black Friday spike buffered in queue |
| **Eventual consistency** | Acceptable for non-critical paths |

**Patterns:**

```text
Event notification — fire and forget
Event-carried state transfer — event contains data
Choreography — services react to events (no central orchestrator)
Saga — distributed txn via compensating events
```

```java
@Transactional
public Order placeOrder(OrderRequest req) {
    Order order = repo.save(new Order(req));
    eventPublisher.publish(new OrderPlacedEvent(order)); // after commit
    return order;
}
```

**Trade-offs:**

```text
❌ Harder debugging (distributed tracing needed)
❌ Eventual consistency complexity
❌ Duplicate messages → idempotent consumers
```

**Interview Point:**

> Async = decouple, buffer, scale. Use for notifications, analytics, non-blocking flows. Always design idempotent consumers + outbox pattern for reliability.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Cache aside vs write through?

<details>
<summary>Show Answer</summary>

**Answer:**

**Cache aside:** app writes DB, invalidates cache; lazy load on read. **Write through:** write cache synchronously writes DB — always consistent, slower writes.

</details>

---

### Q: When SQL vs NoSQL?

<details>
<summary>Show Answer</summary>

**Answer:**

**SQL** for ACID, relations, payments/orders. **NoSQL** for massive scale, flexible schema, logs/feeds. Real systems use both (polyglot persistence).

</details>

---

### Q: Kafka vs RabbitMQ one line?

<details>
<summary>Show Answer</summary>

**Answer:**

**Kafka** = durable replayable event log, high throughput. **RabbitMQ** = message broker with routing, task queues, consume-and-delete.

</details>

---

### Q: Horizontal vs vertical scaling?

<details>
<summary>Show Answer</summary>

**Answer:**

**Horizontal** = more servers (scale out, stateless). **Vertical** = bigger server (scale up, limited ceiling). App tier horizontal; DB often vertical first then shard.

</details>

---

### Q: How to handle duplicate Kafka messages?

<details>
<summary>Show Answer</summary>

**Answer:**

**Idempotent consumer** — dedup by message ID in DB/Redis; upsert instead of insert; transactional outbox for exactly-once publish.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> System design = clarify requirements, estimate capacity, pick SQL/NoSQL/cache/messaging, scale horizontally with LB, cache aside for reads, Kafka for event streaming, saga for distributed transactions, idempotent async consumers.

</details>
