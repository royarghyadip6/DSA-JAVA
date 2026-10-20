# 4. Event-Driven Architecture

## Simple idea

In Event-Driven Architecture (EDA), a service announces **what happened** instead of directly calling every interested service.

```text
Synchronous:
Order → Payment → Inventory → Notification
Order waits and knows every service.

Event-driven:
Order → "OrderPlaced" → Broker
                         ├─ Payment
                         ├─ Inventory
                         ├─ Notification
                         └─ Analytics
```

An event is normally a past-tense fact: `OrderPlaced`, `PaymentCompleted`.

---

## Why use events?

- producer does not block while every consumer works
- producer and consumers are loosely coupled
- consumers scale independently
- add Analytics later without changing Order Service
- broker buffers traffic spikes
- useful for real-time processing and complex workflows

Cost: data across services is not updated at exactly the same moment.

---

## Synchronous vs asynchronous

| Synchronous request/response | Asynchronous event |
|------------------------------|--------------------|
| Caller waits | Producer continues |
| Direct dependency | Producer knows only the event/broker |
| Immediate result | Eventual result |
| Easier tracing | Harder distributed tracing |
| Example: check payment result | Example: send confirmation email |

Use synchronous calls for an answer needed **now**. Use events for reactions that can happen independently.

---

## Key components

| Component | Job |
|-----------|-----|
| Producer | Creates an event |
| Broker | Routes and often buffers/stores events |
| Consumer | Reacts to an event |
| Event store/log | Keeps history for audit or replay |

Examples: Kafka, RabbitMQ, AWS EventBridge, SNS/SQS.

---

## Pub-sub vs event streaming

| Pub-sub | Event streaming |
|---------|-----------------|
| Distribute messages to subscribers | Append events to an ordered log |
| Often delivery-focused | Retention and replay are central |
| RabbitMQ, SNS, Redis Pub/Sub | Kafka, Kinesis |
| Notifications/tasks | Analytics, audit, data pipelines |

Important nuance: “pub-sub messages are always transient” is too broad. RabbitMQ queues can be durable, and EventBridge can archive/replay when configured. The key distinction is that a streaming log makes **retention, ordered consumption, offsets, and replay** core concepts.

---

## E-commerce example

```text
OrderPlaced
  ├─ Payment Service charges
  ├─ Inventory reserves stock
  ├─ Notification sends confirmation
  ├─ Fraud Service checks risk
  └─ Analytics records sale
```

Benefits: services fail and scale independently; a new consumer does not require an Order deployment.

But payment and inventory can temporarily disagree. That is **eventual consistency**.

---

## Main challenges and fixes

### Eventual consistency

Data copies converge later, not immediately.

Use:

- Saga workflows and compensating actions
- status fields such as `PENDING_PAYMENT`
- event sourcing where full event history is valuable
- user-facing language: “Order is being confirmed”

### Duplicate events

Most real brokers give **at-least-once** delivery, so duplicates can occur.

Make consumers **idempotent**:

```text
event_id = e-123
If e-123 is already in processed_events → ignore
Otherwise update business data + record e-123 in one transaction
```

Avoid “increment balance blindly.” Set state based on an event ID/business key.

### Ordering

Global ordering is expensive and usually unnecessary.

- partition by aggregate key: all events for `order-42` enter one Kafka partition
- sequence/version number per entity
- discard stale events
- a global sequence service only when the business truly requires global order

Kafka guarantees order **within a partition**, not across the whole topic.

### Failed messages

Retry temporary failures with exponential backoff.

After the retry limit, place the event in a **Dead-Letter Queue (DLQ)**:

- prevents one poison event from blocking the queue
- lets engineers inspect and fix it
- supports controlled replay later

### Debugging

Carry correlation/trace IDs in events, centralize logs, expose consumer lag, and trace producer → broker → consumer.

---

## Schema evolution

Events remain in queues/logs while code changes. A breaking field removal can crash old consumers.

- prefer additive changes
- maintain backward compatibility
- include `schema_version`
- use Schema Registry with Avro/Protobuf/JSON Schema
- deprecate before removing
- transform old events when needed

---

## Kafka vs RabbitMQ vs EventBridge

| | Kafka | RabbitMQ | AWS EventBridge |
|--|-------|----------|-----------------|
| Main strength | Durable event streaming/log | Message routing and task queues | Managed AWS/SaaS event integration |
| Replay | Core feature via offsets/retention | Possible patterns, but not its main log model | Archive/replay when configured |
| Ordering | Within a partition | Depends on queue/consumer setup | No broad strict global order guarantee |
| Scale use | High-volume streams/analytics | Commands, work queues, routing | Cloud-native integration/rules |

Choose based on delivery model, retention/replay, ordering, throughput, routing, and operations—not only brand popularity.

---

## CQRS and event sourcing (course mention)

- **CQRS:** separate write model from read model. Events can update optimized read views.
- **Event sourcing:** store events as the source of truth and rebuild current state by replay.

They are advanced patterns, not mandatory for every event-driven system.

---

## Good use cases

- audit/logging
- notifications
- IoT sensor ingestion
- stock/trading feeds
- e-commerce order workflow
- decoupling microservices

Avoid events when the caller needs an immediate, simple answer and asynchronous complexity adds no benefit.

---

## Interview Q&A

### 1. What is EDA, and how does it differ from request-response?

<details>
<summary>Show Answer</summary>

**Answer:** In EDA, producers publish events and consumers react asynchronously. They are decoupled and the producer does not wait. Request-response is a direct synchronous dependency where the caller waits for the callee.

</details>

### 2. Pub-sub vs event streaming?

<details>
<summary>Show Answer</summary>

**Answer:** Pub-sub focuses on delivering messages to subscribers. Streaming uses a retained ordered log so independent consumers can process and replay events using offsets. RabbitMQ/SNS are common pub-sub examples; Kafka/Kinesis are streaming examples.

</details>

### 3. What are the key components?

<details>
<summary>Show Answer</summary>

**Answer:** Producers, broker/event bus, consumers, and optionally a retained event store/log.

</details>

### 4. What are the challenges, and how do you handle eventual consistency?

<details>
<summary>Show Answer</summary>

**Answer:** Consistency delay, duplicates, ordering, failed consumers, and tracing. Use Sagas/compensations, idempotent consumers, partitioning, retries/DLQ, status models, and trace IDs.

</details>

### 5. How do you ensure event ordering?

<details>
<summary>Show Answer</summary>

**Answer:** Partition by business key so related events share one ordered partition, include an entity version/sequence, and reject stale events. Avoid costly global ordering unless required.

</details>

### 6. What is a DLQ?

<details>
<summary>Show Answer</summary>

**Answer:** A separate queue/topic for messages that still fail after controlled retries. It prevents infinite retry/queue blockage and allows inspection, repair, and replay.

</details>

### 7. Kafka vs RabbitMQ vs EventBridge?

<details>
<summary>Show Answer</summary>

**Answer:** Kafka is a high-volume retained stream with partition ordering and replay. RabbitMQ excels at message routing and task queues. EventBridge is a managed AWS event bus for routing AWS/SaaS events using rules.

</details>

### 8. How do you make processing idempotent?

<details>
<summary>Show Answer</summary>

**Answer:** Give every event a unique ID/business key, store processed IDs atomically with the business update, and make repeated state changes produce the same result. Do not rely solely on broker deduplication.

</details>

### 9. Give a real-world example.

<details>
<summary>Show Answer</summary>

**Answer:** After `OrderPlaced`, Payment, Inventory, Notification, Fraud, and Analytics consume independently. The producer stays fast, and a new consumer can be added without changing Order Service.

</details>

### 10. How do you handle schema evolution?

<details>
<summary>Show Answer</summary>

**Answer:** Add fields compatibly, version schemas, use a Schema Registry, deprecate fields gradually, and transform old versions if necessary. Consumers must continue reading events already retained by the broker.

</details>

**Q&A recap:** async and loose coupling require explicit plans for consistency, duplicates, ordering, retries, tracing, and schema change.

Back to [00_Section_Overview.md](00_Section_Overview.md). Next: [05_Web_Concepts](../05_Web_Concepts/README.md).
