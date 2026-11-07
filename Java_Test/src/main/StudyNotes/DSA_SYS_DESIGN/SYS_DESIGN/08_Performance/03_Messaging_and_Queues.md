# 3. Messaging and Queues

## Simple idea

A **queue** lets one service drop off work and move on. Another service does the work later.

```text
User clicks "buy"
   → API saves the order and returns "we got it"
   → a message sits in the queue
   → a worker charges, reserves stock, sends email
```

The user does not wait for the email. If the worker is down, the message waits.

This is the performance side of events. The style itself is in [event-driven architecture](../04_Architectural_Patterns/04_Event_Driven_Architecture.md).

---

## The pieces

| Piece | Job |
|-------|-----|
| **Message** | The packet: an order id, a payload |
| **Producer** | Sends it |
| **Broker** | Stores it until someone takes it |
| **Queue or topic** | The named channel |
| **Consumer** | Does the work |
| **Ack** | "I finished. You can delete it." |

```text
Producer →  [ queue ]  → Consumer
                ↑
         if no ack, try again
```

---

## When a queue helps

- The extra work is not needed to answer the user (email, resize, export, analytics).
- Traffic is bursty and you want to smooth it.
- A downstream system is slow or rate-limited (a payments API, a carrier).
- You want to add a new consumer without changing the producer.

Do not put the only copy of a payment result "somewhere in a queue" and return success to the user before you know the write is durable. The user path still needs a real commit for the things you promised.

---

## Delivery guarantees

| Guarantee | Behaviour | You get | You risk |
|-----------|-----------|---------|----------|
| **At-most-once** | Send once, no retry | Fast, simple | Lost messages |
| **At-least-once** | Retry until ack | Very unlikely to lose it | Duplicates |
| **Exactly-once** | One effect, even with retries | No double charge | Hard, and only inside a limited setup |

Most brokers default to **at-least-once**. Your consumer must be **idempotent**: doing the message twice is safe.

"Exactly-once" on a slide is easy to over-read.

- Kafka can do **exactly-once processing inside Kafka** (idempotent producer plus transactions) if the consumer writes its result back through Kafka.
- A side effect **outside** Kafka (send an email, call a bank) is not magically once. The practical pattern for money is **at-least-once plus a unique id** (this `paymentId` was already applied). That is how real billing stays correct.
- The interview PDF ties exactly-once to bank transfers. Say the business rule instead: one business effect per id, even if the message is delivered twice.

| Need | Practical choice |
|------|------------------|
| Metrics you can drop | At-most-once |
| Email, order pipeline | At-least-once + idempotent consumer |
| Money movement | At-least-once + unique transaction id stored before the side effect |

---

## RabbitMQ vs Kafka

| | RabbitMQ | Kafka |
|--|----------|--------|
| Model | Queue. Broker pushes to a consumer | Log. Consumers pull and track an offset |
| Order | FIFO on **one** queue if a single consumer is active | Order **per partition**, not across the whole topic |
| After processing | Message is removed (once acked) | Kept for a retention time. Many consumers can re-read |
| Strength | Routing, task queues, per-message ack | Huge throughput, replay, event streams |
| Typical delivery | At-least-once with manual ack. At-most-once if you ack on receive | At-least-once. Exactly-once only with the Kafka transaction setup |

Use **RabbitMQ** (or SQS, or another task queue) for jobs: "process this order", "send this SMS".

Use **Kafka** when many systems must read the same history, or the volume is an event stream (clicks, logs). You already used Kafka this way in [big data](../07_Storage_Database/06_Big_Data.md).

---

## Order processing

```text
Checkout API
  → commit order as PENDING in SQL
  → publish OrderPlaced
        → inventory worker   reserve stock
        → payment worker     charge (idempotent on order id)
        → email worker       send confirmation
```

The queue gives you:

- a fast HTTP response after the order row exists
- independent scaling of payment and email
- retries when the card network blips

If inventory fails, retry. If the message is poison (bad payload, always crashes), move it to a **dead-letter queue** so it does not block the line.

---

## Idempotent consumers

At-least-once means the same message can arrive twice.

```text
if this orderId is already PAID:
    ack and return
else:
    charge once
    mark PAID
    ack
```

Store processed ids in the same database as the business write, in one transaction when you can. Redis is fine for a short dedupe window. A unique key in PostgreSQL is stronger for payments.

---

## Under heavy load

| Problem | What to do |
|---------|------------|
| Queue grows, lag climbs | Add consumers. Speed up the handler. Watch lag, not only CPU |
| Duplicates from retries | Idempotency |
| One bad message retries forever | Dead-letter queue after N attempts |
| Broker or consumers run out of memory | Limit queue size, apply backpressure, slow the producer |
| A downstream API has a rate limit | Let the queue absorb the burst and process at the allowed pace |

Alert on **queue depth** and **age of the oldest message**, not only on "the process is up".

---

## Safety

Authenticate producers and consumers. Encrypt the connection. Do not put secrets in the message body if the broker is widely readable. These ideas return in the security section.

---

## Interview lines

- A queue decouples "accept the request" from "finish the slow work".
- At-least-once is the default. Make the consumer idempotent.
- Exactly-once inside Kafka is real and limited. Money still needs a unique id.
- RabbitMQ is a task queue. Kafka is a retained log.
- Protect the system with dead-letter queues, lag alerts, and backpressure.

---

## Interview Q&A

### 1. Why asynchronous messaging?

<details>
<summary>Show Answer</summary>

**Answer:**

The producer does not wait for the consumer. That shortens the user response, lets consumers scale on their own, and keeps the message if the worker is down. You can add a new consumer without rewriting the producer. Example: return "order placed", and send the email from a worker.

</details>

### 2. RabbitMQ vs Kafka?

<details>
<summary>Show Answer</summary>

**Answer:**

RabbitMQ is a queue: messages are pushed, acked, and removed. It fits task routing and low-latency jobs (orders, notifications). Order is per queue, and only while one consumer owns that queue.

Kafka is a log: consumers pull, messages stay for a retention window, and order is per partition. It fits high-volume events, replay, and analytics.

RabbitMQ with manual ack is at-least-once. Kafka is at-least-once unless you turn on its transactional exactly-once path.

</details>

### 3. At-most-once, at-least-once, exactly-once?

<details>
<summary>Show Answer</summary>

**Answer:**

- **At-most-once** — no retry. Fast. Messages can disappear. Fine for disposable telemetry.
- **At-least-once** — retry until ack. Duplicates happen. This is the usual default. Consumers must be idempotent. Fit: orders, email.
- **Exactly-once** — one effect. Kafka can guarantee this for processing that stays inside Kafka. A bank transfer or an email gateway is outside that bubble. Use at-least-once plus a unique business id so the second delivery does not charge again.

</details>

### 4. How do queues help scale and survive failure?

<details>
<summary>Show Answer</summary>

**Answer:**

The queue is a buffer. Producers stay fast. You add consumers when the backlog grows. If a consumer crashes, unacked messages stay for another worker. A sale spike sits in the queue instead of knocking down the core database with unbounded threads.

</details>

### 5. What goes wrong under heavy load?

<details>
<summary>Show Answer</summary>

**Answer:**

Backlog and growing delay, duplicate processing, and memory pressure. Mitigate by scaling consumers, speeding the handler, dead-lettering poison messages, and applying backpressure or rate limits so the producer cannot grow the queue without bound.

</details>

### 6. Design order processing with a queue.

<details>
<summary>Show Answer</summary>

**Answer:**

The API stores the order and publishes `OrderPlaced`. Separate consumers reserve stock, take payment, and send mail. Each can retry and scale alone. The user's HTTP call does not wait on the mail server. Use a dead-letter queue for messages that never succeed.

</details>

### 7. How do you make a consumer idempotent?

<details>
<summary>Show Answer</summary>

**Answer:**

Give every message a stable id (order id or event id). Before the side effect, check whether that id was already applied. Record "done" in the same transaction as the business write when you can. Ack only after that. Broker deduplication is a help, not a substitute for the check.

</details>

**Q&A recap:** queue the work the user does not need to watch. Assume the message can arrive twice. Make twice safe.

## Interview Q&A — 6 to 8 years

### S1. Exactly-once is on the slide. What do you promise instead?

<details>
<summary>Show Answer</summary>

**Answer:**

I promise the effect once, not the delivery once. The broker can redeliver. The consumer stores the message id (or the business key) in the same transaction as the side effect. A second delivery sees the id and skips. Kafka's transactional APIs cover a specific path (consume, produce, offset). They do not make an email provider or a bank exactly-once. I say "at-least-once plus idempotent effect."

**Corner cases:** I commit the database row and then fail before I commit the offset. Redelivery is fine only if the row insert is idempotent. The opposite order (offset first) loses the message. A dedupe table that grows forever. I TTL ids for longer than the retry horizon, not longer than the law allows me to keep them. Two consumers in different groups both send the email on purpose. That is two effects. I only dedupe inside the group that must be unique. A producer retry sends two ids for one user click. The id must be born at the client or in the outbox, not at the last network send.

</details>

### S2. A consumer falls behind. What do you check before you add threads?

<details>
<summary>Show Answer</summary>

**Answer:**

Lag in time, the slow handler (usually a downstream), poison messages, and partition count. Extra threads on one partition do not increase Kafka's parallelism. I add consumers up to the number of partitions, or I fix the handler. I cap concurrency so catch-up does not DDoS the database. I move poison messages to a dead-letter after N tries, with the error attached.

**Corner cases:** One hot key hashes to one partition and that partition is the whole lag. More consumers sit idle. I split the key. A retry of a poison message with no delay blocks the partition (head-of-line). I isolate retries. Consumer rebalance during a deploy resets progress and causes a duplicate burst. Idempotency covers that. A queue with no retention deletes the backlog (the "we never lose messages" incident). Lag alerts on message count miss a few huge messages. Alert on age.

</details>

### S3. How do you keep order only where it matters?

<details>
<summary>Show Answer</summary>

**Answer:**

I key the partition by the entity that needs order (order id, conversation id). I do not promise order across users. The consumer applies sequence numbers and ignores an older event that arrives after a newer one. If I need a total order of everything, I have designed a single partition and a single writer, and I say that ceiling out loud.

**Corner cases:** Retries that reorder a per-key stream if two produces are in flight. I use a single producer per key or I sequence in the database. A consumer that processes message 5 before message 4 because it acked out of order and then crashed. I ack only up to a contiguous prefix, or I make the handler commute. A repartition during a key change (user moves region) splits the stream. Compacted topics that drop the intermediate state a new consumer needed. I keep a snapshot.

</details>

### S4. RabbitMQ versus Kafka. What corner decides it?

<details>
<summary>Show Answer</summary>

**Answer:**

Kafka when many consumers read the same history, I want replay, and throughput is a log. A work queue (RabbitMQ or SQS) when each job should be taken by one worker, delayed, or routed to a dead letter with per-message ack. I do not pick Kafka to send one email, and I do not pick a work queue as a year-long event log. The corner case I name is replay: if I cannot re-read yesterday, I cannot rebuild a projection.

**Corner cases:** A work queue that acks on receive, so a worker crash loses the job (at-most-once by mistake). Prefetch so large that a crashed worker holds thousands of invisible messages. Kafka used as an RPC channel with a human waiting. Their timeout fires and the consumer still charges the card. Delay queues and priority: easy to say, easy to starve the low priority forever. I cap the high-priority share.

</details>

**Senior recap:** idempotent effect, lag in time not only count, order per key, and replay is the reason you picked a log.

Next: [04_Concurrency_and_Parallelism.md](04_Concurrency_and_Parallelism.md)
