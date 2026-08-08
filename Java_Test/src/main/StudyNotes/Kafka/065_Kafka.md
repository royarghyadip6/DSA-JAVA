# 65. Kafka

## Extremely Important

---

## Basics

---

# 1. What is Kafka?

<details>
<summary>Show Answer</summary>

**Answer:**

**Apache Kafka** is a **distributed event streaming platform** used to publish, store, and process streams of records in real time. It acts as a **high-throughput message broker** and **event log**.

```text
Producer → Kafka Cluster (Topics/Partitions) → Consumer
              ↑
         durable log (not just a queue)
```

| Concept | Meaning |
|---------|---------|
| **Event / Record** | A message (key, value, timestamp, headers) |
| **Topic** | Named category/stream of events |
| **Partition** | Ordered, immutable log inside a topic |
| **Broker** | Kafka server that stores data |
| **Cluster** | Multiple brokers working together |

```java
// Spring Kafka producer — sends an event
kafkaTemplate.send("order-events", orderId, new OrderCreatedEvent(orderId, amount));

// Spring Kafka consumer — reads events
@KafkaListener(topics = "order-events", groupId = "notification-service")
public void handle(OrderCreatedEvent event) {
    emailService.sendConfirmation(event);
}
```

**Interview Point:**

> Kafka = distributed commit log + pub/sub. Producers write to topics; consumers read at their own pace. Data is persisted (not deleted after read).

</details>

---

# 2. Why Kafka?

<details>
<summary>Show Answer</summary>

**Answer:**

Kafka solves problems that REST/RPC and traditional message queues struggle with at scale.

| Problem | Kafka Solution |
|---------|----------------|
| Tight coupling (sync REST) | Async decoupling — producer doesn't wait for consumer |
| Slow cascading calls | Fire-and-forget; consumers process independently |
| Burst traffic | Buffer events in the log; consumers catch up later |
| Need replay | Consumers can re-read from any offset |
| High throughput | Sequential disk writes, zero-copy, batching |
| Multiple consumers | Same topic, different consumer groups |

```text
REST (sync):     Order → Payment → Inventory → Notify  (all must be up, slow chain)

Kafka (async):   Order → [Kafka topic] → Payment service
                              ↓
                         Inventory service
                              ↓
                         Notification service
              (each reads independently, can scale, replay, survive outages)
```

**Use cases:** event-driven microservices, log aggregation, real-time analytics, CDC (Change Data Capture), stream processing (Kafka Streams).

**Interview Point:**

> Kafka = decouple + buffer + scale + replay. Built for millions of events/sec with durability.

</details>

---

# 3. Kafka architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
                    ┌─────────────────────────────────────┐
                    │         Kafka Cluster               │
                    │  ┌─────────┐  ┌─────────┐         │
                    │  │ Broker 1│  │ Broker 2│  ...    │
                    │  │ Topic A │  │ Topic A │         │
                    │  │ P0(lead)│  │ P1(lead)│         │
                    │  │ P1(rep) │  │ P0(rep) │         │
                    │  └─────────┘  └─────────┘         │
                    └─────────────────────────────────────┘
           ↑ produce                              ↑ consume
    ┌──────────────┐                    ┌──────────────────┐
    │  Producers   │                    │  Consumer Groups │
    └──────────────┘                    └──────────────────┘

    ZooKeeper / KRaft (metadata: leaders, ISR, offsets)
```

| Layer | Role |
|-------|------|
| **Producer** | Publishes records to topic partitions |
| **Broker** | Stores partitions on disk; serves read/write |
| **Topic** | Logical stream; split into partitions |
| **Partition** | Ordered log with offsets 0, 1, 2, … |
| **Consumer Group** | Group of consumers sharing partition assignment |
| **ZooKeeper / KRaft** | Cluster metadata, controller election (KRaft replaces ZK in modern Kafka) |

**Data flow:**

1. Producer picks partition (key hash or sticky partitioner).
2. Record appended to leader partition on a broker.
3. Followers replicate from leader (ISR).
4. Consumer polls broker, reads from assigned partition offset.

**Interview Point:**

> Architecture = distributed log on brokers, partitioned topics, consumer groups for parallel consumption, controller for metadata/leader election.

</details>

---

## Components

---

# 4. Producer

<details>
<summary>Show Answer</summary>

**Answer:**

A **Producer** is a client application that **publishes records** to Kafka topics.

```java
Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
props.put(ProducerConfig.ACKS_CONFIG, "all");           // durability
props.put(ProducerConfig.RETRIES_CONFIG, 3);
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // exactly-once per partition

KafkaProducer<String, OrderEvent> producer = new KafkaProducer<>(props);

producer.send(new ProducerRecord<>("orders", orderId, event), (metadata, ex) -> {
    if (ex != null) {
        log.error("Send failed", ex);
    } else {
        log.info("Sent to partition {} offset {}", metadata.partition(), metadata.offset());
    }
});
```

| Responsibility | Detail |
|----------------|--------|
| Partitioning | Key → hash → partition; no key → sticky/batch partitioner |
| Batching | `linger.ms`, `batch.size` — groups records before send |
| Compression | `gzip`, `snappy`, `lz4`, `zstd` |
| Retries | On transient failure; risk of duplicates without idempotence |
| Acks | `0`, `1`, `all` — trade speed vs durability |

**Interview Point:**

> Producer = client that writes to topics. Controls partitioning, batching, compression, acks, retries, and idempotence.

</details>

---

# 5. Consumer

<details>
<summary>Show Answer</summary>

**Answer:**

A **Consumer** is a client that **reads records** from topic partitions by polling the broker.

```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-processor");
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // manual commit for safety

KafkaConsumer<String, OrderEvent> consumer = new KafkaConsumer<>(props);
consumer.subscribe(List.of("orders"));

while (true) {
    ConsumerRecords<String, OrderEvent> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, OrderEvent> record : records) {
        process(record.value());
    }
    consumer.commitSync(); // commit offsets after successful processing
}
```

| Concept | Meaning |
|---------|---------|
| **Poll** | Consumer pulls data (Kafka is pull-based) |
| **Offset** | Position in partition log |
| **Commit** | Store consumed offset (auto or manual) |
| **Group** | Consumers in same group split partitions |

**Interview Point:**

> Consumer = pull-based reader. Tracks offset per partition. Commits offset after processing (auto or manual).

</details>

---

# 6. Broker

<details>
<summary>Show Answer</summary>

**Answer:**

A **Broker** is a single Kafka server that **stores topic partitions** and serves producer/consumer requests.

```text
Broker 1:  orders-0 (leader), orders-1 (replica), payments-0 (replica)
Broker 2:  orders-1 (leader), orders-0 (replica), payments-0 (leader)
Broker 3:  orders-2 (leader), users-0 (leader)
```

| Role | Detail |
|------|--------|
| **Leader** | Handles all reads/writes for a partition |
| **Follower** | Replicates leader's log; can become leader |
| **Controller** | One broker manages leader election, partition assignment |
| **Storage** | Segments on disk (`*.log` files per partition) |

**Scaling:** add more brokers → more storage and throughput. Partitions distributed across brokers.

**Interview Point:**

> Broker = Kafka server node. Each partition has one leader broker; followers replicate. Cluster = multiple brokers.

</details>

---

# 7. Topic

<details>
<summary>Show Answer</summary>

**Answer:**

A **Topic** is a **logical category** or **stream name** to which producers publish and from which consumers read.

```text
Topic: "order-events"
  ├── Partition 0: [offset 0] [offset 1] [offset 2] ...
  ├── Partition 1: [offset 0] [offset 1] ...
  └── Partition 2: [offset 0] [offset 1] [offset 2] ...
```

```java
// Spring — listener on a topic
@KafkaListener(topics = "order-events", groupId = "inventory-service")
public void onOrder(OrderEvent event) { ... }
```

| Property | Example | Purpose |
|----------|---------|---------|
| `num.partitions` | 6 | Parallelism |
| `retention.ms` | 604800000 (7 days) | How long data kept |
| `cleanup.policy` | `delete` or `compact` | Retention behavior |
| `replication.factor` | 3 | Fault tolerance |

**Interview Point:**

> Topic = named stream. Physically split into partitions across brokers. Producers/consumers use topic name, not broker address.

</details>

---

# 8. Partition

<details>
<summary>Show Answer</summary>

**Answer:**

A **Partition** is an **ordered, immutable sequence** of records within a topic. It is the unit of **parallelism** and **ordering**.

```text
Topic "payments" — 3 partitions

Partition 0:  [0: pay-A] [1: pay-B] [2: pay-C]   ← ordered within partition
Partition 1:  [0: pay-D] [1: pay-E]
Partition 2:  [0: pay-F] [1: pay-G] [2: pay-H]
```

| Rule | Detail |
|------|--------|
| Ordering | Guaranteed **only within one partition** |
| Key routing | Same key → same partition → ordered for that key |
| Parallelism | More partitions = more parallel consumers (up to partition count) |
| Leader | One broker leads each partition |

```java
// Same orderId always goes to same partition → ordered per order
producer.send(new ProducerRecord<>("orders", orderId, event));
```

**Interview Point:**

> Partition = ordered log shard. Ordering per partition only. Partition count caps consumer parallelism in a group.

</details>

---

# 9. Offset

<details>
<summary>Show Answer</summary>

**Answer:**

An **Offset** is a **sequential ID** (0, 1, 2, …) assigned to each record **within a partition**. It marks the consumer's position in the log.

```text
Partition 0:  offset 0   offset 1   offset 2   offset 3
              [event-A]  [event-B]  [event-C]  [event-D]
                              ↑
                    consumer committed offset = 2
                    (processed 0,1; next read starts at 2)
```

| Concept | Meaning |
|---------|---------|
| **Current offset** | Next record to read |
| **Committed offset** | Stored in `__consumer_offsets` topic |
| **Log end offset** | Latest offset written |
| **Lag** | `log end offset - committed offset` |

```java
// Manual commit after processing
consumer.commitSync(); // stores offset in __consumer_offsets

// Reset offset (reprocess)
consumer.seek(partition, 0); // read from beginning
```

**Interview Point:**

> Offset = position in partition log. Consumers commit offsets to track progress. Lag = how far behind a consumer is.

</details>

---

## Frequently Asked

---

# 10. Why partitioning?

<details>
<summary>Show Answer</summary>

**Answer:**

Partitioning enables **scalability**, **parallelism**, and **key-based ordering**.

| Reason | Explanation |
|--------|-------------|
| **Scale throughput** | Multiple partitions on multiple brokers → parallel writes/reads |
| **Parallel consumers** | One consumer per partition in a group → N partitions = up to N consumers |
| **Ordering per key** | `hash(key) % numPartitions` → same key always same partition |
| **Fault isolation** | Partition failure doesn't take down entire topic |
| **Retention granularity** | Old segments deleted per partition |

```text
Without partitions:  1 log → 1 consumer → bottleneck

With 6 partitions:   6 logs → 6 consumers in group → 6x parallelism
```

**Choosing partition count:**

- Start with target throughput / single-partition throughput.
- Too few → can't scale consumers.
- Too many → more metadata, longer rebalances, more open files.
- **Hard to change later** (increase only; decrease not supported).

**Interview Point:**

> Partition = unit of parallelism and per-key ordering. More partitions = more scale, but don't over-partition.

</details>

---

# 11. Consumer Group?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Consumer Group** is a set of consumers that **jointly consume** a topic. Each partition is assigned to **exactly one consumer** in the group.

```text
Topic "orders" — 4 partitions

Consumer Group "payment-service":
  Consumer A → Partition 0, Partition 1
  Consumer B → Partition 2, Partition 3

Consumer Group "analytics-service" (separate group):
  Consumer X → Partition 0, 1, 2, 3  (all partitions — independent read)
```

| Rule | Detail |
|------|--------|
| Same group | Partitions split among members — **load balancing** |
| Different groups | Each group gets **all** messages — **pub/sub fan-out** |
| Max consumers | **≤ number of partitions** (extra consumers sit idle) |
| Group ID | `group.id` config — defines membership |

```java
@KafkaListener(topics = "orders", groupId = "payment-service")
public void processPayment(OrderEvent event) { ... }

@KafkaListener(topics = "orders", groupId = "analytics-service")
public void trackAnalytics(OrderEvent event) { ... }
// Both receive every message — different groups
```

**Production depth:**

- **Static membership** (`group.instance.id`) — reduces unnecessary rebalances on restart.
- **Partition assignment** — `range`, `roundrobin`, `sticky`, `cooperative-sticky` (incremental rebalance).
- **Session timeout** / **heartbeat** — failed consumer detected → rebalance.
- **Max poll interval** — if processing too slow, consumer kicked from group.

**Interview Point:**

> Consumer group = cooperative consumers sharing partitions. Same group = divide work; different groups = independent copies of stream.

</details>

---

# 12. Consumer Group Rebalancing?

<details>
<summary>Show Answer</summary>

**Answer:**

**Rebalancing** is the process of **redistributing partition assignments** among consumers in a group when membership changes.

**Triggers:**

```text
1. New consumer joins group
2. Consumer leaves (shutdown or crash)
3. Consumer exceeds session timeout (no heartbeat)
4. Consumer exceeds max.poll.interval.ms (processing too slow)
5. Topic partitions added
6. Subscription changed
```

```text
Before rebalance (3 consumers, 6 partitions):
  C1: P0, P1    C2: P2, P3    C3: P4, P5

C2 crashes → REBALANCE

After rebalance (2 consumers):
  C1: P0, P1, P2    C3: P3, P4, P5
```

| Strategy | Behavior |
|----------|----------|
| **Eager (range/roundrobin)** | Revoke ALL partitions → reassign — stop-the-world pause |
| **Cooperative-sticky** | Incrementally move partitions — less disruption |

**Problems during rebalance:**

- Processing paused while partitions revoked.
- Duplicate processing if offset committed after revoke.
- **Rebalance storm** — many consumers restarting → constant rebalances.

**Production mitigations:**

```java
props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
    "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000);
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15000);
props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
// Static member — same instance id on restart avoids rebalance
props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "payment-worker-1");
```

**Interview Point:**

> Rebalance = redistribute partitions on membership change. Causes pause and risk of duplicates. Use cooperative assignor, static membership, tune timeouts.

</details>

---

## Advanced

---

# 13. How ordering works?

<details>
<summary>Show Answer</summary>

**Answer:**

Kafka guarantees **ordering only within a single partition**, not across partitions or the entire topic.

```text
Partition 0:  [A1] [A2] [A3]  ← strict order A1 before A2 before A3

Partition 1:  [B1] [B2]        ← strict order within partition 1

Across partitions: A2 might be consumed before B1 — NO global order
```

**How to get ordering you need:**

| Requirement | Approach |
|-------------|----------|
| Order per user | Key = `userId` → same partition |
| Order per order | Key = `orderId` |
| Global order | **1 partition only** (limits throughput) |
| Order in processing | Single-threaded consumer per partition |

```java
// All events for order-123 go to same partition → ordered
producer.send(new ProducerRecord<>("orders", "order-123", event));
```

**Consumer side:** one consumer thread processes one partition sequentially → preserves order.

**Interview Point:**

> Ordering = per partition only. Use key-based partitioning for entity-level order. Global order needs single partition.

</details>

---

# 14. How Kafka achieves high throughput?

<details>
<summary>Show Answer</summary>

**Answer:**

Kafka is optimized for **sequential disk I/O** and **minimal data copying**.

| Technique | How it helps |
|-----------|--------------|
| **Sequential writes** | Append-only log → disk writes are fast (no random I/O) |
| **Page cache** | OS caches log segments; producers/consumers often hit memory |
| **Zero-copy** | `sendfile()` — data goes disk → socket without app buffer copy |
| **Batching** | Producer batches records; consumer fetches in batches |
| **Compression** | Compress batches (producer or broker) — less network/disk |
| **Partition parallelism** | Many partitions across brokers → horizontal scale |
| **No per-message ACK to consumer** | Pull large batches, not one-at-a-time |
| **Minimal indexing** | Offset → file position; no heavy B-tree per message |

```text
Traditional queue:  message → DB row → delete on read → random I/O

Kafka:              append to log segment → read from tail → sequential I/O
                      data retained, multiple consumers, replay friendly
```

**Producer tuning:**

```java
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
```

**Interview Point:**

> High throughput = sequential disk + page cache + zero-copy + batching + compression + partition parallelism.

</details>

---

# 15. Retention policy?

<details>
<summary>Show Answer</summary>

**Answer:**

**Retention** defines how long Kafka **keeps data** before deleting or compacting it.

| Policy | Config | Behavior |
|--------|--------|----------|
| **Delete** | `cleanup.policy=delete` | Remove old segments after `retention.ms` or `retention.bytes` |
| **Compact** | `cleanup.policy=compact` | Keep latest record per key; delete older duplicates |

```text
delete policy (event log):
  Day 1 ── Day 2 ── Day 3 ── Day 4 ── Day 5 ── Day 6 ── Day 7
  [kept]   [kept]   [kept]   [kept]   [kept]   [kept]   [deleted if retention=7d]

compact policy (changelog / CDC):
  key=user1: v1 → v2 → v3  →  only v3 kept eventually
```

```properties
# Topic config
retention.ms=604800000        # 7 days
retention.bytes=1073741824    # 1 GB per partition
segment.ms=86400000           # roll new segment daily
```

**Use cases:**

- **Delete** — event streams, logs, metrics (time-bound).
- **Compact** — topic stores latest state per key (KTable, config, user profile).

**Interview Point:**

> Retention = how long data lives. Delete = time/size based removal. Compact = keep latest per key for changelog topics.

</details>

---

# 16. Replication factor?

<details>
<summary>Show Answer</summary>

**Answer:**

**Replication factor** is the number of **copies** of each partition across brokers (leader + followers).

```text
Replication factor = 3

Partition orders-0:
  Broker 1: LEADER  (handles read/write)
  Broker 2: FOLLOWER (replica)
  Broker 3: FOLLOWER (replica)
```

| Factor | Trade-off |
|--------|-----------|
| **1** | No fault tolerance — broker death = data loss |
| **3** | Production standard — survives 1 broker failure |
| **5** | Higher durability — more storage/network cost |

```properties
# Broker default
default.replication.factor=3

# Topic creation
replication.factor=3
min.insync.replicas=2   # minimum replicas that must ack before write considered safe
```

**Rule:** `replication.factor ≤ number of brokers`.

**With acks=all + min.insync.replicas=2:** write succeeds only when leader + at least one follower have the record.

**Interview Point:**

> Replication factor = copies per partition. RF=3 is production norm. Pair with `min.insync.replicas` and `acks=all` for durability.

</details>

---

## Reliability

---

# 17. ISR?

<details>
<summary>Show Answer</summary>

**Answer:**

**ISR (In-Sync Replicas)** is the set of replicas that are **fully caught up** with the partition leader.

```text
Partition orders-0 (RF=3):
  Leader:   Broker 1  ← in ISR
  Follower: Broker 2  ← in ISR (lag = 0)
  Follower: Broker 3  ← OUT of ISR (lagging / offline)
```

| Concept | Meaning |
|---------|---------|
| **ISR** | Replicas eligible to become leader |
| **Out of ISR** | Replica too far behind (`replica.lag.time.max.ms`) |
| **acks=all** | Wait for all ISR replicas to acknowledge |
| **min.insync.replicas** | Minimum ISR size required for producer write |

```text
Producer acks=all, min.insync.replicas=2:
  Write succeeds → leader + ≥1 ISR follower have record
  ISR shrinks to 1 → producer gets NotEnoughReplicasException
```

**Why ISR matters:**

- Leader election picks from ISR only (by default) → no data loss from unclean election.
- `unclean.leader.election.enable=false` (default) — won't elect out-of-sync replica.

**Interview Point:**

> ISR = caught-up replicas. Leader writes replicated to ISR. acks=all waits for ISR acks. Leader elected from ISR.

</details>

---

# 18. Leader election?

<details>
<summary>Show Answer</summary>

**Answer:**

Each partition has one **leader** broker. If the leader fails, the **controller** elects a new leader from the **ISR**.

```text
Normal:
  orders-0 leader = Broker 1, followers = Broker 2, 3

Broker 1 dies:
  Controller detects failure
  → elects Broker 2 (ISR member) as new leader
  → producers/consumers redirect to Broker 2
```

| Setting | Effect |
|---------|--------|
| `unclean.leader.election.enable=false` | Only ISR replicas can become leader — **no data loss** but partition unavailable if ISR empty |
| `unclean.leader.election.enable=true` | Non-ISR replica can become leader — **possible data loss** |

**Controller role:**

- One broker is cluster controller.
- Handles partition leader election, ISR changes, partition reassignment.
- In KRaft mode, metadata quorum handles this without ZooKeeper.

**During election:**

- Partition briefly unavailable (leadership epoch changes).
- Producers retry; consumers may get `NOT_LEADER_FOR_PARTITION`.

**Interview Point:**

> Leader election = controller picks new leader from ISR when current leader fails. Unclean election trades availability for possible data loss.

</details>

---

# 19. Producer ACKS?

<details>
<summary>Show Answer</summary>

**Answer:**

**`acks`** controls how many replicas must acknowledge a write before the producer considers it successful.

| `acks` | Behavior | Durability | Latency |
|--------|----------|------------|---------|
| **`0`** | Fire-and-forget; no wait | Lowest — may lose data | Fastest |
| **`1`** | Leader ack only | Medium — lost if leader dies before replication | Medium |
| **`all` / `-1`** | All ISR replicas ack | Highest — survives broker failure | Slowest |

```java
props.put(ProducerConfig.ACKS_CONFIG, "all");
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
// idempotence requires acks=all, retries > 0, max.in.flight <= 5
```

```text
acks=0:  Producer → Broker (no wait) → "sent" (maybe lost)

acks=1:  Producer → Leader writes → Leader acks → "ok"
         (if leader crashes before replicate → data lost)

acks=all: Producer → Leader writes → ISR replicates → all ISR ack → "ok"
          + min.insync.replicas=2 → at least 2 brokers have data
```

**Production recommendation:**

```properties
acks=all
min.insync.replicas=2
replication.factor=3
enable.idempotence=true
```

**Interview Point:**

> acks = producer durability knob. Production: `acks=all` + `min.insync.replicas=2` + RF=3. acks=0 only for metrics where loss is acceptable.

</details>

---

## Exactly Once

---

# 20. At-most-once?

<details>
<summary>Show Answer</summary>

**Answer:**

**At-most-once** delivery means a message is **delivered zero or one time** — **never redelivered**, but **may be lost**.

```text
Producer:  acks=0, no retries
Consumer:  commit offset BEFORE processing

Flow:
  1. Consumer reads message offset 5
  2. Consumer commits offset 6 immediately
  3. Processing fails → message LOST (offset already advanced)
```

```java
// At-most-once consumer pattern (NOT recommended for critical data)
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
// Process after auto-commit may have already advanced offset
```

| When to use | Example |
|-------------|---------|
| Loss acceptable | Metrics, click streams where approximate counts OK |
| Never use | Payments, orders, financial events |

**Interview Point:**

> At-most-once = may lose, never duplicate. Fast but unsafe. acks=0 producer + early offset commit consumer.

</details>

---

# 21. At-least-once?

<details>
<summary>Show Answer</summary>

**Answer:**

**At-least-once** delivery means every message is **delivered one or more times** — **no loss**, but **duplicates possible**.

```text
Producer:  acks=all, retries enabled
Consumer:  process message → THEN commit offset

Failure scenario:
  1. Consumer processes message offset 5 (payment charged)
  2. Crash BEFORE commit
  3. Restart → re-read offset 5 → DUPLICATE processing
```

```java
// At-least-once (default safe pattern)
while (true) {
    ConsumerRecords<String, OrderEvent> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, OrderEvent> record : records) {
        processPayment(record.value());  // process first
    }
    consumer.commitSync();               // commit after success
}
```

**Spring Kafka:**

```java
@KafkaListener(topics = "orders", groupId = "payment")
public void handle(OrderEvent event) {
    paymentService.charge(event);  // if exception → offset not committed → retry
}
// Default: MANUAL_IMMEDIATE or BATCH ack after listener returns
```

**Fix duplicates:** idempotent consumer, dedup table, or exactly-once semantics.

**Interview Point:**

> At-least-once = no loss, duplicates possible. Default production pattern: retry producer + commit after process. Handle duplicates in app layer.

</details>

---

# 22. Exactly-once delivery?

<details>
<summary>Show Answer</summary>

**Answer:**

**Exactly-once** means each message is **processed exactly one time** — no loss, no duplicates — even across failures and retries.

Kafka provides **exactly-once semantics (EOS)** at two levels:

| Level | Mechanism |
|-------|-----------|
| **Producer → Kafka** | Idempotent producer (`enable.idempotence=true`) |
| **Consume → Process → Produce** | Transactions (`transactional.id`) |
| **Kafka Streams** | Built-in EOS with `processing.guarantee=exactly_once_v2` |

```java
// Idempotent producer — dedupes retries per partition
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
// Sets: acks=all, retries=MAX, max.in.flight.requests.per.connection=5

// Transactional producer — atomic write across partitions
props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "payment-tx-1");
producer.initTransactions();
producer.beginTransaction();
try {
    producer.send(new ProducerRecord<>("output-topic", key, result));
    producer.sendOffsetsToTransaction(offsets, consumerGroupMetadata);
    producer.commitTransaction();
} catch (Exception e) {
    producer.abortTransaction();
}
```

```text
Without EOS:
  Process msg → write DB → crash before commit offset → reprocess → duplicate DB write

With transactional EOS:
  Read offsets + produce output + commit offsets = ONE atomic transaction
```

**Spring Kafka EOS:**

```java
@KafkaListener(topics = "input")
public void listen(ConsumerRecord<String, String> record) {
    kafkaTemplate.send("output", record.key(), transform(record.value()));
    // With @Transactional on listener + transactional producer → EOS
}
```

**Interview Point:**

> Exactly-once = idempotent producer + transactional writes + offset commit in same transaction. Kafka Streams simplifies this. App DB still needs idempotent writes for external stores.

</details>

---

## Production

---

# 23. How to handle duplicate messages?

<details>
<summary>Show Answer</summary>

**Answer:**

Duplicates arise from **producer retries**, **consumer rebalance**, or **at-least-once** processing. Handle them explicitly.

**Strategies:**

| Strategy | How |
|----------|-----|
| **Idempotent producer** | Kafka dedupes by `producerId + sequenceNumber` per partition |
| **Business idempotency key** | Store `eventId` in DB; skip if already processed |
| **Upsert / natural idempotency** | `UPDATE balance SET x = 100` (same result if replayed) |
| **Outbox + dedup table** | `INSERT INTO processed_events (id) ON CONFLICT DO NOTHING` |
| **Transactional consume-produce** | Kafka transactions for Kafka-to-Kafka pipelines |

```java
@Transactional
public void handleOrder(OrderEvent event) {
    if (processedEventRepo.existsById(event.getEventId())) {
        return; // already handled — skip duplicate
    }
    orderService.process(event);
    processedEventRepo.save(new ProcessedEvent(event.getEventId()));
}
```

```sql
CREATE TABLE processed_events (
    event_id VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMP
);
```

```text
Duplicate flow:
  Msg event-99 arrives (1st time) → process → save event-99 → OK
  Msg event-99 arrives (retry)    → exists in table → skip → OK (no duplicate charge)
```

**Interview Point:**

> Duplicates are normal in Kafka. Producer idempotence prevents broker-side dupes from retries. Consumer side needs dedup key / idempotent handler / transactional pipeline.

</details>

---

# 24. Idempotent consumer?

<details>
<summary>Show Answer</summary>

**Answer:**

An **idempotent consumer** processes the same message **multiple times** but the **outcome is the same** — no duplicate side effects.

**Requirements:**

1. **Idempotent operation** — repeating has same effect as once.
2. **Deduplication** — track processed message IDs.
3. **Commit offset after** successful idempotent write.

```java
@Service
public class IdempotentOrderHandler {

    @Transactional
    public void handle(OrderEvent event) {
        // 1. Dedup check
        if (dedupRepo.exists(event.getEventId())) {
            log.info("Duplicate skipped: {}", event.getEventId());
            return;
        }

        // 2. Idempotent business logic
        accountRepo.upsertBalance(event.getUserId(), event.getAmount());

        // 3. Record processed
        dedupRepo.markProcessed(event.getEventId());
    }
}
```

| Pattern | Idempotent? |
|---------|-------------|
| `balance += 100` | **No** — replay adds again |
| `balance = 500` (set to value) | **Yes** — same result |
| `INSERT` without constraint | **No** |
| `INSERT ... ON CONFLICT DO NOTHING` | **Yes** |
| External API charge | **No** — need idempotency key header |

**With external systems:**

```java
paymentGateway.charge(
    event.getOrderId(),           // idempotency key
    event.getAmount()
); // Stripe/PayPal return same result for same key
```

**Interview Point:**

> Idempotent consumer = safe replays. Dedup table + idempotent writes + commit after success. Never use non-idempotent ops like `+=` without dedup.

</details>

---

# 25. Dead Letter Queue?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Dead Letter Queue (DLQ)** is a separate topic where **failed messages** are sent after retries are exhausted — so they don't block the main consumer or get lost.

```text
orders-topic → Consumer (retry 3x) → still fails?
                      ↓
              orders-topic.DLQ  (poison message stored)
                      ↓
              Manual review / fix / replay tool
```

**Spring Kafka DLQ pattern:**

```java
@RetryableTopic(
    attempts = "4",
    backoff = @Backoff(delay = 1000, multiplier = 2),
    dltStrategy = DltStrategy.FAIL_ON_ERROR,
    include = {ValidationException.class}
)
@KafkaListener(topics = "orders", groupId = "order-processor")
public void process(OrderEvent event) {
    orderService.validateAndProcess(event);
}
// After retries → message goes to orders-topic-dlt
```

```java
// DLT listener — log, alert, or manual handling
@DltHandler
public void handleDlt(OrderEvent event, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
    log.error("DLQ: {} error: {}", event, error);
    alertService.notify("Poison message in DLQ", event);
}
```

| Component | Role |
|-----------|------|
| **Retry topic** | Temporary delay before retry (`orders-retry-0`) |
| **DLT topic** | Final destination for poison pills |
| **Headers** | Original topic, partition, offset, exception, stack trace |
| **Replay** | Fix bug → re-publish from DLT to main topic |

**Production checklist:**

```text
✓ DLQ topic with retention (don't lose poison messages)
✓ Monitoring/alerting on DLQ lag
✓ Dashboard for DLQ message inspection
✓ Replay procedure documented
✓ Separate consumer group for DLT analysis
```

**Interview Point:**

> DLQ = parking lot for failed messages. Retry with backoff first; then DLT. Monitor DLQ, alert on growth, support manual replay after fix.

</details>

---
